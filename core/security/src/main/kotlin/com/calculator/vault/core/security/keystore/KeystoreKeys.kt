package com.calculator.vault.core.security.keystore

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyPermanentlyInvalidatedException
import android.security.keystore.KeyProperties
import org.koin.core.annotation.Single
import timber.log.Timber
import java.security.GeneralSecurityException
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Android Keystore keys. They never leave the device and are deleted on uninstall, so they
 * only protect *convenience* copies of the master keys (fingerprint unlock, email recovery).
 */
@Single
class KeystoreKeys {
    private val keyStore: KeyStore by lazy { KeyStore.getInstance(PROVIDER).apply { load(null) } }

    // region Device key (no user authentication) — email recovery on this install
    fun deviceWrap(plaintext: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreate(DEVICE_ALIAS, requireBiometric = false))
        return cipher.iv + cipher.doFinal(plaintext)
    }

    fun deviceUnwrap(wrapped: ByteArray): ByteArray? = try {
        val key = keyStore.getKey(DEVICE_ALIAS, null) as? SecretKey ?: return null
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(TAG_BITS, wrapped, 0, IV_BYTES))
        cipher.doFinal(wrapped, IV_BYTES, wrapped.size - IV_BYTES)
    } catch (error: GeneralSecurityException) {
        Timber.w(error, "Device key unwrap failed")
        null
    }
    // endregion

    // region Biometric key — must be unlocked by BiometricPrompt (CryptoObject)
    fun biometricEncryptCipher(): Cipher? = try {
        deleteKey(BIOMETRIC_ALIAS)
        Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.ENCRYPT_MODE, getOrCreate(BIOMETRIC_ALIAS, requireBiometric = true))
        }
    } catch (error: GeneralSecurityException) {
        Timber.w(error, "Biometric key unavailable")
        null
    }

    fun biometricDecryptCipher(iv: ByteArray): Cipher? = try {
        val key = keyStore.getKey(BIOMETRIC_ALIAS, null) as? SecretKey ?: return null
        Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.DECRYPT_MODE, key, GCMParameterSpec(TAG_BITS, iv))
        }
    } catch (invalidated: KeyPermanentlyInvalidatedException) {
        // A new fingerprint was enrolled: the key is gone for good.
        Timber.i(invalidated, "Biometric key invalidated")
        deleteKey(BIOMETRIC_ALIAS)
        null
    } catch (error: GeneralSecurityException) {
        Timber.w(error, "Biometric cipher init failed")
        null
    }

    fun deleteBiometricKey() = deleteKey(BIOMETRIC_ALIAS)
    // endregion

    private fun getOrCreate(alias: String, requireBiometric: Boolean): SecretKey {
        (keyStore.getKey(alias, null) as? SecretKey)?.let { return it }
        val spec = KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
            .setKeySize(256)
            .apply {
                if (requireBiometric) {
                    setUserAuthenticationRequired(true)
                    setInvalidatedByBiometricEnrollment(true)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        setUserAuthenticationParameters(0, KeyProperties.AUTH_BIOMETRIC_STRONG)
                    }
                }
            }
            .build()
        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, PROVIDER).run {
            init(spec)
            generateKey()
        }
    }

    private fun deleteKey(alias: String) {
        runCatching { if (keyStore.containsAlias(alias)) keyStore.deleteEntry(alias) }
    }

    private companion object {
        const val PROVIDER = "AndroidKeyStore"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val DEVICE_ALIAS = "cv_device_wrap_v1"
        const val BIOMETRIC_ALIAS = "cv_biometric_wrap_v1"
        const val IV_BYTES = 12
        const val TAG_BITS = 128
    }
}
