package com.calculator.vault.core.data.repository

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.calculator.vault.core.common.dispatchers.VaultDispatchers
import com.calculator.vault.core.common.result.runSuspendCatching
import com.calculator.vault.core.data.storage.StorageAccess
import com.calculator.vault.core.data.storage.VaultPaths
import com.calculator.vault.core.data.work.CloudBackupWorker
import com.calculator.vault.core.database.dao.BackupFileDao
import com.calculator.vault.core.datastore.VaultPreferences
import com.calculator.vault.core.datastore.VaultPreferences.BoolKey
import com.calculator.vault.core.domain.repository.CloudBackupRepository
import com.calculator.vault.core.domain.repository.VaultRepository
import com.calculator.vault.core.model.BackupFailure
import com.calculator.vault.core.model.BackupNetwork
import com.calculator.vault.core.model.BackupSettings
import com.calculator.vault.core.model.BackupStatus
import com.calculator.vault.core.model.GoogleAccount
import com.calculator.vault.core.model.StorageQuota
import com.calculator.vault.core.network.drive.DriveApi
import com.calculator.vault.core.network.drive.DriveAuth
import com.calculator.vault.core.network.drive.DriveToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single
import java.io.File
import java.util.concurrent.TimeUnit

@Single(binds = [CloudBackupRepository::class])
class CloudBackupRepositoryImpl(
    context: Context,
    private val prefs: VaultPreferences,
    private val auth: DriveAuth,
    private val api: DriveApi,
    private val paths: VaultPaths,
    private val access: StorageAccess,
    private val manifest: BackupFileDao,
    private val vault: VaultRepository,
    private val dispatchers: VaultDispatchers,
) : CloudBackupRepository {

    private val workManager = WorkManager.getInstance(context)

    override val settings: Flow<BackupSettings> = prefs.backup

    override val status: Flow<BackupStatus> = combine(
        prefs.backup,
        workManager.getWorkInfosForUniqueWorkFlow(MANUAL_WORK),
    ) { settings, infos ->
        val info = infos.firstOrNull()
        when {
            settings.accountEmail == null -> BackupStatus.SignedOut
            info?.state == WorkInfo.State.RUNNING ->
                BackupStatus.Running(info.progress.getFloat(CloudBackupWorker.KEY_PROGRESS, 0f))
            info?.state == WorkInfo.State.ENQUEUED && info.runAttemptCount > 0 ->
                BackupStatus.Interrupted(info.progress.getFloat(CloudBackupWorker.KEY_PROGRESS, 0f), BackupFailure.NO_CONNECTION)
            info?.state == WorkInfo.State.FAILED -> {
                val reason = info.outputData.getString(CloudBackupWorker.KEY_FAILURE)
                    ?.let { name -> BackupFailure.entries.firstOrNull { it.name == name } } ?: BackupFailure.UNKNOWN
                BackupStatus.Interrupted(info.outputData.getFloat(CloudBackupWorker.KEY_PROGRESS, 0f), reason)
            }
            else -> BackupStatus.Idle(settings.lastBackupAt)
        }
    }

    override suspend fun onSignedIn(account: GoogleAccount) =
        prefs.update { setBackupAccount(account.email, account.displayName) }

    override suspend fun signOut() {
        workManager.cancelUniqueWork(MANUAL_WORK)
        workManager.cancelUniqueWork(PERIODIC_WORK)
        manifest.clear()
        prefs.update {
            setBackupAccount(null, null)
            set(BoolKey.BACKUP_AUTO, false)
        }
    }

    override suspend fun quota(): Result<StorageQuota> = runSuspendCatching {
        val token = (auth.token() as? DriveToken.Granted)?.accessToken ?: error("Drive access not granted")
        val (used, total) = api.quota(token)
        StorageQuota(used, total)
    }

    override suspend fun backupNow() {
        val request = OneTimeWorkRequestBuilder<CloudBackupWorker>()
            .setConstraints(constraints(prefs.backup.first().network))
            .setInputData(workDataOf(CloudBackupWorker.KEY_FOREGROUND to true))
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
            .build()
        workManager.enqueueUniqueWork(MANUAL_WORK, ExistingWorkPolicy.REPLACE, request)
    }

    override suspend fun restore(): Result<Int> = withContext(dispatchers.io) {
        runSuspendCatching {
            val token = (auth.token() as? DriveToken.Granted)?.accessToken ?: error("Drive access not granted")
            val root = if (access.hasAllFilesAccess()) paths.sharedRoot else paths.fallbackRoot
            var restored = 0
            api.listAppData(token).forEach { remote ->
                val path = remote.appProperties?.get(CloudBackupWorker.PATH_KEY) ?: return@forEach
                if (path == VaultPaths.CONFIG_FILE) return@forEach
                val target = File(root, path)
                if (!target.exists()) {
                    api.download(token, remote.id, target)
                    restored++
                }
            }
            vault.rebuildIndexIfNeeded()
            restored
        }
    }

    override suspend fun setAutoBackup(enabled: Boolean) {
        prefs.update { set(BoolKey.BACKUP_AUTO, enabled) }
        schedulePeriodic(enabled, prefs.backup.first().network)
    }

    override suspend fun setNetwork(network: BackupNetwork) {
        prefs.update { backupNetwork = network }
        val settings = prefs.backup.first()
        schedulePeriodic(settings.autoBackup, network)
    }

    private fun schedulePeriodic(enabled: Boolean, network: BackupNetwork) {
        if (!enabled) {
            workManager.cancelUniqueWork(PERIODIC_WORK)
            return
        }
        val request = PeriodicWorkRequestBuilder<CloudBackupWorker>(1, TimeUnit.DAYS)
            .setConstraints(constraints(network).let { Constraints.Builder(it).setRequiresBatteryNotLow(true).build() })
            .build()
        workManager.enqueueUniquePeriodicWork(PERIODIC_WORK, ExistingPeriodicWorkPolicy.UPDATE, request)
    }

    private fun constraints(network: BackupNetwork) = Constraints.Builder()
        .setRequiredNetworkType(if (network == BackupNetwork.WIFI_ONLY) NetworkType.UNMETERED else NetworkType.CONNECTED)
        .build()

    private companion object {
        const val MANUAL_WORK = "cloud_backup_now"
        const val PERIODIC_WORK = "cloud_backup_daily"
    }
}
