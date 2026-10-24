package com.calculator.vault.feature.applock.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.usage.UsageStatsManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.calculator.vault.core.domain.repository.AppLockRepository
import com.calculator.vault.core.model.AppLockSettings
import com.calculator.vault.feature.applock.R
import com.calculator.vault.feature.applock.overlay.LockOverlayActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

/**
 * Watches which app is in the foreground and shows [LockOverlayActivity] over locked apps.
 * Runs as a foreground service (special use: app locking) only while App Lock is on.
 */
class AppLockService : LifecycleService() {

    private val repository: AppLockRepository by inject()
    @Volatile private var settings = AppLockSettings()
    private var lastPackage: String? = null

    private val screenOff = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // Locking the screen re-locks every app immediately.
            repository.relockAll()
            lastPackage = null
        }
    }

    override fun onCreate() {
        super.onCreate()
        startForegroundCompat()
        ContextCompat.registerReceiver(this, screenOff, IntentFilter(Intent.ACTION_SCREEN_OFF), ContextCompat.RECEIVER_NOT_EXPORTED)
        lifecycleScope.launch { repository.settings.collect { settings = it } }
        lifecycleScope.launch(Dispatchers.Default) { monitor() }
    }

    private suspend fun monitor() {
        val usage = getSystemService(UsageStatsManager::class.java) ?: return stopSelf()
        val detector = ForegroundAppDetector(usage)
        while (lifecycleScope.coroutineContext.isActive) {
            val current = runCatching { detector.poll() }.getOrNull()
            if (current != null && current != lastPackage) {
                lastPackage = current
                onForeground(current)
            }
            delay(POLL_MS)
        }
    }

    private fun onForeground(packageName: String) {
        val s = settings
        if (!s.enabled || packageName == this.packageName) return
        if (packageName !in s.lockedPackages) return
        if (repository.isTemporarilyUnlocked(packageName, s.lockAfter)) return
        startActivity(LockOverlayActivity.intent(this, packageName))
    }

    private fun startForegroundCompat() {
        val manager = getSystemService(NotificationManager::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && manager.getNotificationChannel(CHANNEL) == null) {
            manager.createNotificationChannel(NotificationChannel(CHANNEL, getString(R.string.applock_channel), NotificationManager.IMPORTANCE_MIN))
        }
        val notification: Notification = NotificationCompat.Builder(this, CHANNEL)
            .setSmallIcon(R.drawable.ic_applock_notification)
            .setContentTitle(getString(R.string.applock_notification_title))
            .setContentText(getString(R.string.applock_notification_text))
            .setOngoing(true)
            .setSilent(true)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .build()
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE else 0
        ServiceCompat.startForeground(this, NOTIFICATION_ID, notification, type)
    }

    override fun onDestroy() {
        runCatching { unregisterReceiver(screenOff) }
        super.onDestroy()
    }

    companion object {
        private const val CHANNEL = "app_lock"
        private const val NOTIFICATION_ID = 4201
        private const val POLL_MS = 400L

        fun start(context: Context) {
            runCatching { ContextCompat.startForegroundService(context, Intent(context, AppLockService::class.java)) }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, AppLockService::class.java))
        }
    }
}
