package com.calculator.vault.feature.splash.presentation.mvi

import androidx.lifecycle.viewModelScope
import com.calculator.vault.core.domain.repository.SettingsRepository
import com.calculator.vault.core.ui.mvi.MviViewModel
import com.calculator.vault.feature.splash.domain.usecase.AcceptWelcomeUseCase
import com.calculator.vault.feature.splash.domain.usecase.GetStartDestinationUseCase
import com.calculator.vault.feature.splash.domain.usecase.StartDestination
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class SplashViewModel(
    settings: SettingsRepository,
    getStartDestination: GetStartDestinationUseCase,
    private val acceptWelcome: AcceptWelcomeUseCase,
) : MviViewModel<SplashState, SplashIntent, SplashEffect>(SplashState()) {

    private val destination = viewModelScope.async { getStartDestination() }
    private var started = false

    init {
        viewModelScope.launch {
            val icon = settings.settings.first().appIcon
            val mode = if (destination.await() == StartDestination.WELCOME) SplashMode.WELCOME else SplashMode.BRAND
            setState { copy(mode = mode, icon = icon) }
        }
    }

    override fun handleIntent(intent: SplashIntent) {
        when (intent) {
            // The vault resets to the splash while backgrounded; only start the timer once the
            // user can see it, so returning to the app always shows splash → calculator. The
            // first-install welcome waits for "Get Started" instead.
            SplashIntent.Visible -> if (!started) {
                started = true
                viewModelScope.launch {
                    val target = destination.await()
                    if (target == StartDestination.WELCOME) return@launch
                    delay(MIN_SPLASH_MS)
                    sendEffect(SplashEffect.Navigate(target))
                }
            }
            SplashIntent.GetStarted -> viewModelScope.launch {
                acceptWelcome()
                sendEffect(SplashEffect.ShowLanguage)
            }
        }
    }

    private companion object {
        const val MIN_SPLASH_MS = 900L
    }
}
