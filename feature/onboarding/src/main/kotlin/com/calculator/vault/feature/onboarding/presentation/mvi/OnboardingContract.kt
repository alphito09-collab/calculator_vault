package com.calculator.vault.feature.onboarding.presentation.mvi

import com.calculator.vault.core.ui.mvi.UiEffect
import com.calculator.vault.core.ui.mvi.UiIntent
import com.calculator.vault.core.ui.mvi.UiState

data class OnboardingState(val page: Int = 0, val pageCount: Int = 3) : UiState {
    val isLastPage: Boolean get() = page == pageCount - 1
}

sealed interface OnboardingIntent : UiIntent {
    data class PageSettled(val page: Int) : OnboardingIntent
    data object Continue : OnboardingIntent
    data object Skip : OnboardingIntent
}

sealed interface OnboardingEffect : UiEffect {
    data class ScrollTo(val page: Int) : OnboardingEffect

    /** Onboarding is done (last page or Skip): create the PIN next. */
    data object Finished : OnboardingEffect
}
