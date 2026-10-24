package com.calculator.vault.core.domain.repository

import com.calculator.vault.core.model.BackupNetwork
import com.calculator.vault.core.model.BackupSettings
import com.calculator.vault.core.model.BackupStatus
import com.calculator.vault.core.model.GoogleAccount
import com.calculator.vault.core.model.StorageQuota
import kotlinx.coroutines.flow.Flow

/** Google Drive (appDataFolder) backup of the already-encrypted vault folder. */
interface CloudBackupRepository {
    val settings: Flow<BackupSettings>
    val status: Flow<BackupStatus>

    suspend fun onSignedIn(account: GoogleAccount)
    suspend fun signOut()
    suspend fun quota(): Result<StorageQuota>

    suspend fun backupNow()
    suspend fun restore(): Result<Int>

    suspend fun setAutoBackup(enabled: Boolean)
    suspend fun setNetwork(network: BackupNetwork)
}
