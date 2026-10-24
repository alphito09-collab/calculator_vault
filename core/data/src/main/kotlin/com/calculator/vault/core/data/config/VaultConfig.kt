package com.calculator.vault.core.data.config

import kotlinx.serialization.Serializable

/**
 * Everything needed to re-open the vault with a user secret. Contains only *wrapped* keys,
 * so it is safe to keep in shared storage next to the encrypted files.
 */
@Serializable
data class VaultConfig(
    val version: Int = 1,
    val vaultId: String,
    val createdAt: Long,
    val kdfIterations: Int,
    val pin: WrappedSecret,
    val pattern: WrappedSecret? = null,
    val answer: WrappedSecret? = null,
    val securityQuestion: String? = null,
    val recoveryEmail: String? = null,
    /** Base64 public hybrid keyset, used to encrypt intruder photos while locked. */
    val publicKeyset: String,
    val updatedAt: Long = createdAt,
) {
    val associatedData: ByteArray get() = "cv-master:$vaultId".toByteArray()
}

@Serializable
data class WrappedSecret(val salt: String, val wrapped: String)
