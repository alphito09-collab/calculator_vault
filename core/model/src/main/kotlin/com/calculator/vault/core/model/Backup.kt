package com.calculator.vault.core.model

data class StorageQuota(val usedBytes: Long, val totalBytes: Long) {
    val freeBytes: Long get() = (totalBytes - usedBytes).coerceAtLeast(0)
    val fraction: Float get() = if (totalBytes <= 0) 0f else usedBytes.toFloat() / totalBytes
}

sealed interface BackupStatus {
    data object SignedOut : BackupStatus
    data class Idle(val lastBackupAt: Long?) : BackupStatus
    data class Running(val progress: Float) : BackupStatus
    data class Interrupted(val progress: Float, val reason: BackupFailure) : BackupStatus
}

enum class BackupFailure { NO_CONNECTION, AUTH_REQUIRED, QUOTA_EXCEEDED, UNKNOWN }

data class GoogleAccount(val email: String, val displayName: String?)
