package com.calculator.vault.core.data.repository

import com.calculator.vault.core.common.dispatchers.ApplicationScope
import com.calculator.vault.core.common.result.runSuspendCatching
import com.calculator.vault.core.data.config.VaultConfig
import com.calculator.vault.core.data.config.VaultConfigStore
import com.calculator.vault.core.data.repository.VaultKeyring.Companion.b64
import com.calculator.vault.core.data.storage.VaultPaths
import com.calculator.vault.core.datastore.DeviceState
import com.calculator.vault.core.datastore.VaultPreferences
import com.calculator.vault.core.domain.repository.AuthRepository
import com.calculator.vault.core.model.PinCheckResult
import com.calculator.vault.core.security.crypto.MasterKeys
import com.calculator.vault.core.security.crypto.SecretWrapping
import com.calculator.vault.core.security.keystore.KeystoreKeys
import com.calculator.vault.core.security.session.SessionKeys
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single
import timber.log.Timber
import java.util.UUID
import javax.crypto.Cipher

@Single(binds = [AuthRepository::class])
class AuthRepositoryImpl(
    private val configStore: VaultConfigStore,
    private val keyring: VaultKeyring,
    private val session: SessionKeys,
    private val keystore: KeystoreKeys,
    private val deviceState: DeviceState,
    private val preferences: VaultPreferences,
    private val paths: VaultPaths,
    private val maintenance: VaultMaintenance,
    private val scope: ApplicationScope,
) : AuthRepository {

    override val isVaultCreated: Flow<Boolean> = configStore.config.map { it != null }.distinctUntilChanged()

    override val isUnlocked: Flow<Boolean> = session.isUnlocked

    override fun isUnlockedNow(): Boolean = session.isUnlocked.value

    override suspend fun createVault(pin: String): Result<Unit> = runSuspendCatching {
        val keys = MasterKeys.generate()
        val serialized = keys.serialize()
        val vaultId = UUID.randomUUID().toString()
        val iterations = SecretWrapping.DEFAULT_ITERATIONS
        val config = VaultConfig(
            vaultId = vaultId,
            createdAt = System.currentTimeMillis(),
            kdfIterations = iterations,
            pin = keyring.wrap(null, vaultId, pin, serialized, iterations),
            publicKeyset = b64(keys.publicKeyset()),
        )
        configStore.save(config)
        refreshDeviceCopy(serialized)
        deviceState.setFailedStreak(0)
        session.open(keys)
        afterUnlock()
    }

    override suspend fun unlockWithPin(pin: String): PinCheckResult {
        val config = configStore.current() ?: return PinCheckResult.NotAPin
        val keys = keyring.unwrap(config, config.pin, pin)
        if (keys == null) {
            val streak = deviceState.failedStreak() + 1
            deviceState.setFailedStreak(streak)
            return PinCheckResult.Wrong(streak)
        }
        open(keys)
        return PinCheckResult.Success
    }

    override suspend fun verifyPin(pin: String): Boolean {
        val config = configStore.current() ?: return false
        return keyring.unwrap(config, config.pin, pin) != null
    }

    override suspend fun changePin(currentPin: String, newPin: String): Result<Unit> = runSuspendCatching {
        val config = checkNotNull(configStore.current()) { "No vault" }
        val keys = checkNotNull(keyring.unwrap(config, config.pin, currentPin)) { "Wrong PIN" }
        rewrapPin(config, keys.serialize(), newPin)
    }

    override suspend fun resetPin(newPin: String): Result<Unit> = runSuspendCatching {
        val config = checkNotNull(configStore.current()) { "No vault" }
        rewrapPin(config, session.require().keys.serialize(), newPin)
    }

    private suspend fun rewrapPin(config: VaultConfig, serialized: ByteArray, newPin: String) {
        configStore.save(config.copy(pin = keyring.wrap(config, config.vaultId, newPin, serialized, config.kdfIterations)))
    }

    override fun lock() {
        session.lock()
        scope.launch { runCatching { paths.wipeDecryptedCache() } }
    }

    /** Opens the session; shared with recovery flows. */
    internal suspend fun open(keys: MasterKeys) {
        deviceState.setFailedStreak(0)
        session.open(keys)
        refreshDeviceCopy(keys.serialize())
        afterUnlock()
    }

    private fun afterUnlock() {
        scope.launch { maintenance.runAfterUnlock() }
    }

    private suspend fun refreshDeviceCopy(serialized: ByteArray) {
        runCatching { deviceState.setDeviceWrapped(keystore.deviceWrap(serialized)) }
            .onFailure { Timber.w(it, "Device-bound key copy failed") }
    }

    // region Biometrics
    override val isBiometricUnlockEnabled: Flow<Boolean> =
        combine(preferences.settings, deviceState.hasBiometricKey) { settings, hasKey ->
            settings.fingerprintUnlockEnabled && hasKey
        }.distinctUntilChanged()

    override fun biometricEnrollCipher(): Cipher? = keystore.biometricEncryptCipher()

    override suspend fun enableBiometricUnlock(authenticatedCipher: Cipher): Result<Unit> = runSuspendCatching {
        val wrapped = authenticatedCipher.doFinal(session.require().keys.serialize())
        deviceState.setBiometricWrapped(wrapped, authenticatedCipher.iv)
        preferences.update { set(VaultPreferences.BoolKey.FINGERPRINT_UNLOCK, true) }
    }

    override suspend fun biometricUnlockCipher(): Cipher? {
        val stored = deviceState.biometricWrapped() ?: return null
        return keystore.biometricDecryptCipher(stored.second)
    }

    override suspend fun unlockWithBiometric(authenticatedCipher: Cipher): Result<Unit> = runSuspendCatching {
        val (wrapped, _) = checkNotNull(deviceState.biometricWrapped()) { "Fingerprint not set up" }
        open(MasterKeys.deserialize(authenticatedCipher.doFinal(wrapped)))
    }

    override suspend fun disableBiometricUnlock() {
        keystore.deleteBiometricKey()
        deviceState.setBiometricWrapped(null, null)
        preferences.update { set(VaultPreferences.BoolKey.FINGERPRINT_UNLOCK, false) }
    }
    // endregion
}
