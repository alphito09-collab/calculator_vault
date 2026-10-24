package com.calculator.vault.core.security.crypto

import com.google.crypto.tink.InsecureSecretKeyAccess
import com.google.crypto.tink.KeysetHandle
import com.google.crypto.tink.TinkProtoKeysetFormat
import com.google.crypto.tink.aead.PredefinedAeadParameters
import com.google.crypto.tink.hybrid.PredefinedHybridParameters
import com.google.crypto.tink.streamingaead.PredefinedStreamingAeadParameters
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream

/**
 * The vault's key material. It never touches disk in plaintext: [serialize] output is always
 * wrapped by a PIN/pattern/answer-derived key or an Android Keystore key before storage.
 *
 * - streaming: AES256-GCM-HKDF (1 MB segments) for file contents, seekable for video playback.
 * - aead: AES256-GCM for metadata and thumbnails.
 * - hybrid: ECIES P-256 so intruder photos can be encrypted while the vault is locked.
 */
class MasterKeys private constructor(
    internal val streaming: KeysetHandle,
    internal val aead: KeysetHandle,
    internal val hybridPrivate: KeysetHandle,
) {
    fun serialize(): ByteArray {
        val out = ByteArrayOutputStream()
        DataOutputStream(out).use { data ->
            data.writeInt(FORMAT_VERSION)
            for (handle in listOf(streaming, aead, hybridPrivate)) {
                val bytes = TinkProtoKeysetFormat.serializeKeyset(handle, InsecureSecretKeyAccess.get())
                data.writeInt(bytes.size)
                data.write(bytes)
            }
        }
        return out.toByteArray()
    }

    /** Public half of the hybrid keyset; safe to store in plaintext. */
    fun publicKeyset(): ByteArray =
        TinkProtoKeysetFormat.serializeKeysetWithoutSecret(hybridPrivate.publicKeysetHandle)

    companion object {
        private const val FORMAT_VERSION = 1

        fun generate(): MasterKeys {
            TinkBootstrap.ensure()
            return MasterKeys(
                streaming = KeysetHandle.generateNew(PredefinedStreamingAeadParameters.AES256_GCM_HKDF_1MB),
                aead = KeysetHandle.generateNew(PredefinedAeadParameters.AES256_GCM),
                hybridPrivate = KeysetHandle.generateNew(PredefinedHybridParameters.ECIES_P256_HKDF_HMAC_SHA256_AES128_GCM),
            )
        }

        fun deserialize(bytes: ByteArray): MasterKeys {
            TinkBootstrap.ensure()
            DataInputStream(bytes.inputStream()).use { data ->
                require(data.readInt() == FORMAT_VERSION) { "Unsupported key format" }
                fun next(): KeysetHandle {
                    val raw = ByteArray(data.readInt())
                    data.readFully(raw)
                    return TinkProtoKeysetFormat.parseKeyset(raw, InsecureSecretKeyAccess.get())
                }
                return MasterKeys(next(), next(), next())
            }
        }
    }
}
