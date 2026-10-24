package com.calculator.vault.feature.applock.overlay

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.calculator.vault.core.designsystem.theme.CalculatorVaultTheme
import com.calculator.vault.core.model.ThemeMode
import com.calculator.vault.core.ui.biometric.BiometricResult
import com.calculator.vault.core.ui.biometric.rememberBiometricPrompter
import com.calculator.vault.core.ui.mvi.CollectEffects
import com.calculator.vault.core.ui.util.SystemBarsAppearance
import com.calculator.vault.feature.applock.R
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

/**
 * Shown over a locked app. Lives in its own task and never appears in Recents; leaving it
 * without unlocking sends the user to the home screen.
 */
class LockOverlayActivity : AppCompatActivity() {

    private val packageToUnlock: String by lazy { intent.getStringExtra(EXTRA_PACKAGE).orEmpty() }
    private val viewModel: LockOverlayViewModel by viewModel { parametersOf(packageToUnlock) }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_SECURE)
        val label = runCatching {
            packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageToUnlock, 0)).toString()
        }.getOrDefault(packageToUnlock)
        setContent {
            val state by viewModel.state.collectAsStateWithLifecycle()
            val dark = when (state.themeMode) {
                ThemeMode.SYSTEM -> isSystemInDarkTheme()
                ThemeMode.LIGHT -> false
                ThemeMode.DARK -> true
            }
            CalculatorVaultTheme(darkTheme = dark) {
                SystemBarsAppearance(dark)
                val prompter = rememberBiometricPrompter()
                val title = getString(R.string.applock_unlock_title, label)
                val usePin = getString(R.string.applock_use_pin)
                viewModel.effects.CollectEffects { effect ->
                    when (effect) {
                        LockOverlayEffect.Unlocked -> finish()
                        LockOverlayEffect.PromptFingerprint -> if (prompter.isAvailable()) {
                            prompter.authenticate(title, null, usePin) { result ->
                                if (result is BiometricResult.Success) viewModel.onIntent(LockOverlayIntent.FingerprintSucceeded)
                                else viewModel.onIntent(LockOverlayIntent.UsePin)
                            }
                        } else {
                            viewModel.onIntent(LockOverlayIntent.UsePin)
                        }
                    }
                }
                BackHandler { goHome() }
                if (state.ready) LockOverlayScreen(state, label, viewModel::onIntent)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.getStringExtra(EXTRA_PACKAGE) != packageToUnlock) {
            setIntent(intent)
            recreate()
        }
    }

    private fun goHome() {
        startActivity(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        finish()
    }

    companion object {
        private const val EXTRA_PACKAGE = "package"

        fun intent(context: Context, packageName: String) = Intent(context, LockOverlayActivity::class.java)
            .putExtra(EXTRA_PACKAGE, packageName)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
    }
}
