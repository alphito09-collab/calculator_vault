package com.calculator.vault.core.data.work

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.calculator.vault.core.data.storage.StorageAccess
import com.calculator.vault.core.data.storage.VaultPaths
import com.calculator.vault.core.database.dao.BackupFileDao
import com.calculator.vault.core.database.entity.BackupFileEntity
import com.calculator.vault.core.datastore.VaultPreferences
import com.calculator.vault.core.model.BackupFailure
import com.calculator.vault.core.network.drive.DriveApi
import com.calculator.vault.core.network.drive.DriveAuth
import com.calculator.vault.core.network.drive.DriveToken
import io.ktor.client.plugins.ResponseException
import kotlinx.coroutines.CancellationException
import org.koin.android.annotation.KoinWorker
import timber.log.Timber
import java.io.File
import java.io.IOException

/**
 * Uploads the (already encrypted) vault folder to Drive's appDataFolder. Only new or changed
 * files are sent; files deleted locally are removed remotely.
 */
@KoinWorker
class CloudBackupWorker(
    context: Context,
    params: WorkerParameters,
    private val auth: DriveAuth,
    private val api: DriveApi,
    private val paths: VaultPaths,
    private val access: StorageAccess,
    private val manifest: BackupFileDao,
    private val prefs: VaultPreferences,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val token = try {
            when (val t = auth.token()) {
                is DriveToken.Granted -> t.accessToken
                is DriveToken.NeedsConsent -> return failure(BackupFailure.AUTH_REQUIRED, 0f)
            }
        } catch (error: Exception) {
            if (error is CancellationException) throw error
            Timber.w(error, "Drive authorization failed")
            return failure(BackupFailure.AUTH_REQUIRED, 0f)
        }

        if (inputData.getBoolean(KEY_FOREGROUND, false)) {
            runCatching { setForeground(foregroundInfo(0)) }
        }

        val root = if (access.hasAllFilesAccess()) paths.sharedRoot else paths.fallbackRoot
        val local = localFiles(root)
        var progress = 0f
        return try {
            val remote = api.listAppData(token).associateBy { it.appProperties?.get(PATH_KEY) ?: it.name }
            val known = manifest.all().associateBy { it.relativePath }
            val pending = local.filter { (path, file) ->
                val entry = known[path]
                entry == null || entry.sizeBytes != file.length() || entry.modifiedAt != file.lastModified() ||
                    remote[path]?.id != entry.driveFileId
            }
            pending.entries.forEachIndexed { index, (path, file) ->
                val oldId = remote[path]?.id
                val newId = api.upload(token, file, file.name, path)
                if (oldId != null && oldId != newId) runCatching { api.delete(token, oldId) }
                manifest.upsert(BackupFileEntity(path, file.length(), file.lastModified(), newId, System.currentTimeMillis()))
                progress = (index + 1f) / pending.size
                setProgress(workDataOf(KEY_PROGRESS to progress))
                if (inputData.getBoolean(KEY_FOREGROUND, false)) runCatching { setForeground(foregroundInfo((progress * 100).toInt())) }
            }
            // Items deleted from the vault are deleted from the backup as well.
            remote.filterKeys { it !in local }.forEach { (path, file) ->
                runCatching { api.delete(token, file.id) }
                manifest.delete(path)
            }
            prefs.update { setLastBackup(System.currentTimeMillis()) }
            Result.success()
        } catch (error: IOException) {
            Timber.w(error, "Backup interrupted")
            if (runAttemptCount < MAX_RETRIES) Result.retry() else failure(BackupFailure.NO_CONNECTION, progress)
        } catch (error: ResponseException) {
            Timber.w(error, "Drive rejected the backup")
            val reason = if (error.response.status.value == 403) BackupFailure.QUOTA_EXCEEDED else BackupFailure.UNKNOWN
            failure(reason, progress)
        }
    }

    private fun localFiles(root: File): Map<String, File> {
        if (!root.exists()) return emptyMap()
        return root.walkTopDown()
            .filter { it.isFile && !it.name.endsWith(".tmp") && it.name != ".nomedia" }
            .associateBy { it.relativeTo(root).invariantSeparatorsPath }
    }

    private fun failure(reason: BackupFailure, progress: Float) =
        Result.failure(workDataOf(KEY_FAILURE to reason.name, KEY_PROGRESS to progress))

    private fun foregroundInfo(percent: Int): ForegroundInfo {
        val manager = applicationContext.getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && manager.getNotificationChannel(CHANNEL_ID) == null) {
            manager.createNotificationChannel(NotificationChannel(CHANNEL_ID, "Backup", NotificationManager.IMPORTANCE_LOW))
        }
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_upload)
            .setContentTitle("Backing up")
            .setProgress(100, percent, percent == 0)
            .setOngoing(true)
            .setSilent(true)
            .build()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ForegroundInfo(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            ForegroundInfo(NOTIFICATION_ID, notification)
        }
    }

    companion object {
        const val KEY_PROGRESS = "progress"
        const val KEY_FAILURE = "failure"
        const val KEY_FOREGROUND = "foreground"
        const val PATH_KEY = "path"
        private const val MAX_RETRIES = 3
        private const val CHANNEL_ID = "cloud_backup"
        private const val NOTIFICATION_ID = 4101
    }
}
