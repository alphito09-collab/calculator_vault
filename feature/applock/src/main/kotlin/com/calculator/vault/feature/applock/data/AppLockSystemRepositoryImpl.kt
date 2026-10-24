package com.calculator.vault.feature.applock.data

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Process
import android.provider.Settings
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.getSystemService
import com.calculator.vault.core.common.dispatchers.VaultDispatchers
import com.calculator.vault.core.model.InstalledApp
import com.calculator.vault.feature.applock.domain.repository.AppLockPermissions
import com.calculator.vault.feature.applock.domain.repository.AppLockSystemRepository
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Single

@Single(binds = [AppLockSystemRepository::class])
class AppLockSystemRepositoryImpl(
    private val context: Context,
    private val dispatchers: VaultDispatchers,
) : AppLockSystemRepository {

    override suspend fun installedApps(locked: Set<String>): List<InstalledApp> = withContext(dispatchers.io) {
        val pm = context.packageManager
        val launcher = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        pm.queryIntentActivities(launcher, 0)
            .asSequence()
            .map { it.activityInfo.packageName to it.loadLabel(pm).toString() }
            .filter { (pkg, _) -> pkg != context.packageName }
            .distinctBy { it.first }
            .map { (pkg, label) -> InstalledApp(pkg, label, pkg in locked) }
            .sortedWith(compareByDescending<InstalledApp> { it.isLocked }.thenBy { it.label.lowercase() })
            .toList()
    }

    override fun permissions() = AppLockPermissions(
        usageAccess = hasUsageAccess(context),
        overlay = Settings.canDrawOverlays(context),
        notifications = NotificationManagerCompat.from(context).areNotificationsEnabled(),
    )

    companion object {
        fun hasUsageAccess(context: Context): Boolean {
            val ops = context.getSystemService<AppOpsManager>() ?: return false
            val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ops.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
            } else {
                @Suppress("DEPRECATION")
                ops.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
            }
            return mode == AppOpsManager.MODE_ALLOWED
        }
    }
}
