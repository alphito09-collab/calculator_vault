package com.calculator.vault.core.security.crypto

import java.security.GeneralSecurityException
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

/**
 * Wraps the serialized [MasterKeys] with keys derived from user secrets (PIN, pattern, answer).
 * These wrappings are stored in shared storage, which is why the vault survives reinstall:
 * the same PIN re-derives the same key-encryption key.
 */
object SecretWrapping {
    const val DEFAULT_ITERATIONS = 120_000
    private const val KEY_BITS = 256
    private const val IV_BYTES = 12
    private const val TAG_BITS = 128
    private val random = SecureRandom()

    fun newSalt(): ByteArray = ByteArray(16).also(random::nextBytes)

    fun deriveKey(secret: String, salt: ByteArray, iterations: Int = DEFAULT_ITERATIONS): ByteArray {
        val spec = PBEKeySpec(secret.toCharArray(), salt, iterations, KEY_BITS)
        return try {
            SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).encoded
        } finally {
            spec.clearPassword()
        }
    }

    /** AES-256-GCM; output is `iv || ciphertext+tag`. */
    fun wrap(kek: ByteArray, plaintext: ByteArray, associatedData: ByteArray): ByteArray {
        val iv = ByteArray(IV_BYTES).also(random::nextBytes)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, SecretKeySpec(kek, "AES"), GCMParameterSpec(TAG_BITS, iv))
        cipher.updateAAD(associatedData)
        return iv + cipher.doFinal(plaintext)
    }

    /** Returns null when [kek] is wrong (tag mismatch) instead of throwing. */
    fun unwrap(kek: ByteArray, wrapped: ByteArray, associatedData: ByteArray): ByteArray? = try {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(
            Cipher.DECRYPT_MODE,
            SecretKeySpec(kek, "AES"),
            GCMParameterSpec(TAG_BITS, wrapped, 0, IV_BYTES),
        )
        cipher.updateAAD(associatedData)
        cipher.doFinal(wrapped, IV_BYTES, wrapped.size - IV_BYTES)
    } catch (_: GeneralSecurityException) {
        null
    }

    /** Canonical form so "  Leo " and "leo" unlock the same answer. */
    fun normalizeAnswer(answer: String): String = answer.trim().lowercase().replace(Regex("\\s+"), " ")

    fun patternSecret(pattern: List<Int>): String = "pattern:" + pattern.joinToString("-")
}
