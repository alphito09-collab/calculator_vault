package com.calculator.vault.core.data.repository

import com.calculator.vault.core.common.result.runSuspendCatching
import com.calculator.vault.core.data.config.VaultConfigStore
import com.calculator.vault.core.datastore.DeviceState
import com.calculator.vault.core.datastore.VaultPreferences
import com.calculator.vault.core.domain.repository.RecoveryRepository
import com.calculator.vault.core.model.RecoveryMethod
import com.calculator.vault.core.model.RecoveryOptions
import com.calculator.vault.core.model.SecurityQuestion
import com.calculator.vault.core.security.crypto.MasterKeys
import com.calculator.vault.core.security.crypto.SecretWrapping
import com.calculator.vault.core.security.keystore.KeystoreKeys
import com.calculator.vault.core.security.session.SessionKeys
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import org.koin.core.annotation.Single

@Single(binds = [RecoveryRepository::class])
class RecoveryRepositoryImpl(
    private val configStore: VaultConfigStore,
    private val keyring: VaultKeyring,
    private val session: SessionKeys,
    private val keystore: KeystoreKeys,
    private val deviceState: DeviceState,
    private val preferences: VaultPreferences,
    private val auth: AuthRepositoryImpl,
) : RecoveryRepository {

    override val options: Flow<RecoveryOptions> =
        combine(configStore.config, auth.isBiometricUnlockEnabled) { config, fingerprint ->
            RecoveryOptions(
                fingerprintEnabled = fingerprint,
                hasPattern = config?.pattern != null,
                email = config?.recoveryEmail,
                securityQuestion = config?.securityQuestion?.let { name ->
                    SecurityQuestion.entries.firstOrNull { it.name == name }
                },
            )
        }

    override suspend fun availableMethods(): List<RecoveryMethod> {
        val config = configStore.current() ?: return emptyList()
        return buildList {
            if (auth.isBiometricUnlockEnabled.first() && auth.biometricUnlockCipher() != null) add(RecoveryMethod.FINGERPRINT)
            if (config.pattern != null) add(RecoveryMethod.PATTERN)
            if (config.recoveryEmail != null && deviceState.deviceWrapped() != null) add(RecoveryMethod.EMAIL)
            if (config.answer != null) add(RecoveryMethod.SECURITY_QUESTION)
        }
    }

    override suspend fun setPattern(pattern: List<Int>): Result<Unit> = runSuspendCatching {
        require(pattern.size >= MIN_PATTERN) { "Pattern too short" }
        val config = checkNotNull(configStore.current())
        val wrapped = keyring.wrap(
            config,
            config.vaultId,
            SecretWrapping.patternSecret(pattern),
            session.require().keys.serialize(),
            config.kdfIterations,
        )
        configStore.save(config.copy(pattern = wrapped))
    }

    override suspend fun clearPattern(): Result<Unit> = runSuspendCatching {
        configStore.current()?.let { configStore.save(it.copy(pattern = null)) }
    }

    override suspend fun unlockWithPattern(pattern: List<Int>): Result<Unit> = runSuspendCatching {
        val config = checkNotNull(configStore.current())
        val wrapped = checkNotNull(config.pattern) { "No pattern set" }
        val keys = checkNotNull(keyring.unwrap(config, wrapped, SecretWrapping.patternSecret(pattern))) { "Wrong pattern" }
        auth.open(keys)
    }

    override suspend fun verifyPattern(pattern: List<Int>): Boolean {
        val config = configStore.current() ?: return false
        val wrapped = config.pattern ?: return false
        return keyring.unwrap(config, wrapped, SecretWrapping.patternSecret(pattern)) != null
    }

    override suspend fun setSecurityAnswer(question: SecurityQuestion, answer: String): Result<Unit> = runSuspendCatching {
        val normalized = SecretWrapping.normalizeAnswer(answer)
        require(normalized.length >= 2) { "Answer too short" }
        val config = checkNotNull(configStore.current())
        val wrapped = keyring.wrap(config, config.vaultId, normalized, session.require().keys.serialize(), config.kdfIterations)
        configStore.save(config.copy(answer = wrapped, securityQuestion = question.name))
    }

    override suspend fun unlockWithSecurityAnswer(answer: String): Result<Unit> = runSuspendCatching {
        val config = checkNotNull(configStore.current())
        val wrapped = checkNotNull(config.answer) { "No security question set" }
        val keys = checkNotNull(keyring.unwrap(config, wrapped, SecretWrapping.normalizeAnswer(answer))) { "Wrong answer" }
        auth.open(keys)
    }

    override suspend fun setRecoveryEmail(email: String): Result<Unit> = runSuspendCatching {
        val config = checkNotNull(configStore.current())
        configStore.save(config.copy(recoveryEmail = email.trim()))
    }

    override suspend fun unlockWithVerifiedEmail(): Result<Unit> = unlockWithDeviceKey()

    override suspend fun unlockWithResetCode(): Result<Unit> = unlockWithDeviceKey()

    private suspend fun unlockWithDeviceKey(): Result<Unit> = runSuspendCatching {
        val wrapped = checkNotNull(deviceState.deviceWrapped()) { "No device-bound key on this install" }
        val serialized = checkNotNull(keystore.deviceUnwrap(wrapped)) { "Device key unavailable" }
        auth.open(MasterKeys.deserialize(serialized))
    }

    override suspend fun setFingerprintRecovery(enabled: Boolean) {
        if (!enabled) auth.disableBiometricUnlock()
        else preferences.update { set(VaultPreferences.BoolKey.FINGERPRINT_UNLOCK, true) }
    }

    private companion object {
        const val MIN_PATTERN = 4
    }
}
