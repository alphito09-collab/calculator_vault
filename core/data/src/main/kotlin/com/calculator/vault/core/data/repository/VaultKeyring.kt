package com.calculator.vault.core.data.repository

import com.calculator.vault.core.common.dispatchers.VaultDispatchers
import com.calculator.vault.core.data.config.VaultConfig
import com.calculator.vault.core.data.config.WrappedSecret
import com.calculator.vault.core.security.crypto.MasterKeys
import com.calculator.vault.core.security.crypto.SecretWrapping
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single
import java.util.Base64

/** Derives key-encryption keys off the main thread and (un)wraps the master keys. */
@Single
class VaultKeyring(private val dispatchers: VaultDispatchers) {

    suspend fun wrap(config: VaultConfig?, vaultId: String, secret: String, masterKeys: ByteArray, iterations: Int): WrappedSecret =
        withContext(dispatchers.default) {
            val salt = SecretWrapping.newSalt()
            val kek = SecretWrapping.deriveKey(secret, salt, iterations)
            val ad = config?.associatedData ?: "cv-master:$vaultId".toByteArray()
            WrappedSecret(b64(salt), b64(SecretWrapping.wrap(kek, masterKeys, ad)))
        }

    /** Returns the decrypted master keys or null when [secret] is wrong. */
    suspend fun unwrap(config: VaultConfig, wrapped: WrappedSecret, secret: String): MasterKeys? =
        withContext(dispatchers.default) {
            val kek = SecretWrapping.deriveKey(secret, unb64(wrapped.salt), config.kdfIterations)
            SecretWrapping.unwrap(kek, unb64(wrapped.wrapped), config.associatedData)?.let(MasterKeys::deserialize)
        }

    companion object {
        fun b64(bytes: ByteArray): String = Base64.getEncoder().encodeToString(bytes)
        fun unb64(text: String): ByteArray = Base64.getDecoder().decode(text)
    }
}
