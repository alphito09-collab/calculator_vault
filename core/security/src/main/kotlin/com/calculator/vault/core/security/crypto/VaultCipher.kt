package com.calculator.vault.core.security.crypto

import com.google.crypto.tink.Aead
import com.google.crypto.tink.HybridDecrypt
import com.google.crypto.tink.RegistryConfiguration
import com.google.crypto.tink.StreamingAead
import java.io.InputStream
import java.io.OutputStream
import java.nio.channels.SeekableByteChannel

/** Ready-to-use primitives built from [MasterKeys]. Thread-safe. */
class VaultCipher(val keys: MasterKeys) {
    private val streaming: StreamingAead =
        keys.streaming.getPrimitive(RegistryConfiguration.get(), StreamingAead::class.java)
    private val aead: Aead = keys.aead.getPrimitive(RegistryConfiguration.get(), Aead::class.java)
    private val hybrid: HybridDecrypt =
        keys.hybridPrivate.getPrimitive(RegistryConfiguration.get(), HybridDecrypt::class.java)

    fun encryptingStream(target: OutputStream, associatedData: ByteArray): OutputStream =
        streaming.newEncryptingStream(target, associatedData)

    fun decryptingStream(source: InputStream, associatedData: ByteArray): InputStream =
        streaming.newDecryptingStream(source, associatedData)

    /** Random-access decryption, used by the video/audio player and PDF renderer. */
    fun decryptingChannel(source: SeekableByteChannel, associatedData: ByteArray): SeekableByteChannel =
        streaming.newSeekableDecryptingChannel(source, associatedData)

    fun seal(plaintext: ByteArray, associatedData: ByteArray): ByteArray = aead.encrypt(plaintext, associatedData)

    fun open(ciphertext: ByteArray, associatedData: ByteArray): ByteArray = aead.decrypt(ciphertext, associatedData)

    fun openSealedForVault(ciphertext: ByteArray, context: ByteArray): ByteArray = hybrid.decrypt(ciphertext, context)
}
