package com.calculator.vault.feature.splash.presentation.mvi

import com.calculator.vault.core.model.AppIconOption
import com.calculator.vault.core.ui.mvi.UiEffect
import com.calculator.vault.core.ui.mvi.UiIntent
import com.calculator.vault.core.ui.mvi.UiState
import com.calculator.vault.feature.splash.domain.usecase.StartDestination

/** [LOADING] until the start destination is known; [WELCOME] is the first-install "Get Started". */
enum class SplashMode { LOADING, BRAND, WELCOME }

data class SplashState(val mode: SplashMode = SplashMode.LOADING, val icon: AppIconOption? = null) : UiState

sealed interface SplashIntent : UiIntent {
    /** The splash is actually on screen (not just composed while the app is in the background). */
    data object Visible : SplashIntent
    data object GetStarted : SplashIntent
}

sealed interface SplashEffect : UiEffect {
    data class Navigate(val destination: StartDestination) : SplashEffect

    /** After "Get Started": language comes next, with the welcome kept underneath for back. */
    data object ShowLanguage : SplashEffect
}
