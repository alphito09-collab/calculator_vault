package com.calculator.vault.core.security.crypto

import com.google.crypto.tink.HybridEncrypt
import com.google.crypto.tink.RegistryConfiguration
import com.google.crypto.tink.TinkProtoKeysetFormat

/** Encrypts data *to* the vault without unlocking it (intruder selfies). */
class PublicKeyEncryptor(publicKeyset: ByteArray) {
    private val encrypt: HybridEncrypt = run {
        TinkBootstrap.ensure()
        TinkProtoKeysetFormat.parseKeysetWithoutSecret(publicKeyset)
            .getPrimitive(RegistryConfiguration.get(), HybridEncrypt::class.java)
    }

    fun seal(plaintext: ByteArray, context: ByteArray): ByteArray = encrypt.encrypt(plaintext, context)
}
