package com.calculator.vault.hide.app.lock.photo.video.free

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Color
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.view.inspector.WindowInspector
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.core.content.getSystemService
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.media3.common.util.UnstableApi
import com.calculator.vault.core.common.session.LockGuard
import com.calculator.vault.core.domain.repository.AuthRepository
import com.calculator.vault.core.domain.repository.SettingsRepository
import com.calculator.vault.core.media.image.VaultImageLoader
import com.calculator.vault.core.media.player.AudioController
import com.calculator.vault.core.model.UserSettings
import com.calculator.vault.core.navigation.CalculatorRoute
import com.calculator.vault.core.navigation.EmailOtpRoute
import com.calculator.vault.core.navigation.ForgotPinRoute
import com.calculator.vault.core.navigation.Navigator
import com.calculator.vault.core.navigation.SplashRoute
import com.calculator.vault.hide.app.lock.photo.video.free.panic.ShakeDetector
import com.calculator.vault.hide.app.lock.photo.video.free.ui.VaultApp
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

/**
 * Single activity. Besides hosting Compose it owns the vault session rules:
 * - leaving the app (Home, Recents, screen off) locks the vault and shows Splash → Calculator;
 *   after the next unlock the exact vault back stack is restored,
 * - the vault never appears in Recents: the stored snapshot is disabled, and because the
 *   launcher's live tile renders the real window (even with FLAG_SECURE), a privacy cover is
 *   drawn the moment Recents opens or focus moves to system UI,
 * - "Prevent screenshot" and panic-mode shake detection.
 */
@OptIn(UnstableApi::class)
class MainActivity : AppCompatActivity() {

    private val navigator: Navigator by inject()
    private val auth: AuthRepository by inject()
    private val settingsRepository: SettingsRepository by inject()
    private val audio: AudioController by inject()
    private val images: VaultImageLoader by inject()

    private var settingsLoaded = false
    private var panicEnabled = false
    private var shakeDetector: ShakeDetector? = null

    /** Drawn over everything while system UI (Recents, the shade) can see the window. */
    private var privacyCover by mutableStateOf(false)

    // Recents keeps the activity resumed (only window focus is lost), so the lifecycle alone can't
    // tell it apart from the notification shade. Launchers announce it with this broadcast first.
    private val systemDialogsReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val reason = intent.getStringExtra(EXTRA_REASON)
            if ((reason == REASON_RECENT_APPS || reason == REASON_HOME_KEY) && auth.isUnlockedNow()) {
                privacyCover = true
                lockSession(showCalculatorDirectly = false)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        super.onCreate(savedInstanceState)
        splash.setKeepOnScreenCondition { !settingsLoaded }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // The vault never shows up as a Recents thumbnail.
            setRecentsScreenshotEnabled(false)
        }
        // A fresh process (or one restored after death) must never land inside the vault.
        if (!auth.isUnlockedNow() && navigator.isInsideVault) navigator.lockVault()

        lifecycleScope.launch {
            settingsRepository.settings.first()
            settingsLoaded = true
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                combine(auth.isUnlocked, settingsRepository.settings) { unlocked, s -> unlocked to s }.collect { (unlocked, s) ->
                    applyWindowSecurity(unlocked, s)
                    panicEnabled = s.panicModeEnabled
                    updateShakeDetection(unlocked)
                }
            }
        }

        val versionLabel = getString(R.string.app_version_label, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE)
        val pinChanged = getString(R.string.app_pin_changed)
        setContent {
            val settings by settingsRepository.settings.collectAsStateWithLifecycle(UserSettings())
            VaultApp(
                navigator = navigator,
                themeMode = settings.themeMode,
                appIcon = settings.appIcon,
                privacyCover = privacyCover,
                versionLabel = versionLabel,
                pinChangedMessage = pinChanged,
                onExit = ::finish,
            )
        }
    }

    // Only sending ACTION_CLOSE_SYSTEM_DIALOGS is restricted (API 31+); the system still delivers it.
    @Suppress("DEPRECATION")
    override fun onStart() {
        super.onStart()
        ContextCompat.registerReceiver(
            this,
            systemDialogsReceiver,
            IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS),
            ContextCompat.RECEIVER_NOT_EXPORTED,
        )
    }

    override fun onResume() {
        super.onResume()
        LockGuard.clear()
        updateShakeDetection(auth.isUnlockedNow())
    }

    override fun onPause() {
        shakeDetector?.stop()
        super.onPause()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            privacyCover = false
        } else if (auth.isUnlockedNow() && !ownWindowShowing()) {
            // Fallback for launchers that don't announce Recents, and covers the vault under the
            // notification shade or another app's window. Our own dialogs, sheets and menus are
            // windows that exist before they take focus, so they never trigger it.
            privacyCover = true
        }
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(systemDialogsReceiver)
        if (isChangingConfigurations) return
        if (LockGuard.consumeExternalLaunch()) return
        lockSession(showCalculatorDirectly = false)
    }

    private fun lockSession(showCalculatorDirectly: Boolean) {
        val current = navigator.currentRoute
        if (auth.isUnlockedNow()) {
            auth.lock()
            audio.stop()
            images.clearMemory()
            navigator.lockVault()
            if (showCalculatorDirectly) navigator.resetTo(CalculatorRoute)
        } else if (current is CalculatorRoute || current is ForgotPinRoute || current is EmailOtpRoute) {
            navigator.resetTo(SplashRoute)
        }
    }

    /**
     * FLAG_SECURE blocks screenshots and hides the Recents preview. It is always on when the
     * user asked to prevent screenshots, and on older Android versions (no
     * setRecentsScreenshotEnabled) while the vault is unlocked.
     */
    private fun applyWindowSecurity(unlocked: Boolean, settings: UserSettings) {
        val secure = settings.preventScreenshots || (unlocked && Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
        if (secure) window.addFlags(WindowManager.LayoutParams.FLAG_SECURE) else window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
    }

    /** True when one of our dialogs/sheets/menus is up. Before Q (no live Recents tile) always true. */
    private fun ownWindowShowing(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || otherGlobalWindowVisible()

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun otherGlobalWindowVisible(): Boolean =
        WindowInspector.getGlobalWindowViews().any { it !== window.decorView && it.windowVisibility == View.VISIBLE }

    private fun updateShakeDetection(unlocked: Boolean) {
        val wanted = panicEnabled && unlocked && lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)
        if (!wanted) {
            shakeDetector?.stop()
            return
        }
        val detector = shakeDetector ?: getSystemService<SensorManager>()?.let { sm ->
            ShakeDetector(sm) { runOnUiThread { lockSession(showCalculatorDirectly = true) } }
        }?.also { shakeDetector = it }
        detector?.start()
    }

    private companion object {
        const val EXTRA_REASON = "reason"
        const val REASON_RECENT_APPS = "recentapps"
        const val REASON_HOME_KEY = "homekey"
    }
}
