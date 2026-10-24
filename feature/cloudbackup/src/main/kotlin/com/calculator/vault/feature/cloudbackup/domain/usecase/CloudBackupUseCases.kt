package com.calculator.vault.feature.cloudbackup.domain.usecase

import com.calculator.vault.core.domain.repository.CloudBackupRepository
import com.calculator.vault.core.domain.repository.NetworkMonitor
import com.calculator.vault.core.model.BackupNetwork
import com.calculator.vault.core.model.BackupSettings
import com.calculator.vault.core.model.BackupStatus
import com.calculator.vault.core.model.GoogleAccount
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.koin.core.annotation.Factory

data class CloudBackupOverview(val settings: BackupSettings, val status: BackupStatus, val online: Boolean)

@Factory
class ObserveCloudBackupUseCase(private val backup: CloudBackupRepository, private val network: NetworkMonitor) {
    operator fun invoke(): Flow<CloudBackupOverview> =
        combine(backup.settings, backup.status, network.isOnline) { s, status, online -> CloudBackupOverview(s, status, online) }
}

@Factory
class CloudBackupActionsUseCase(private val backup: CloudBackupRepository) {
    suspend fun signedIn(account: GoogleAccount) = backup.onSignedIn(account)
    suspend fun signOut() = backup.signOut()
    suspend fun quota() = backup.quota()
    suspend fun backupNow() = backup.backupNow()
    suspend fun restore() = backup.restore()
    suspend fun setAuto(enabled: Boolean) = backup.setAutoBackup(enabled)
    suspend fun setNetwork(network: BackupNetwork) = backup.setNetwork(network)
}
