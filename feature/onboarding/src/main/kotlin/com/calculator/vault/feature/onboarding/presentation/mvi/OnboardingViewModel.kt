package com.calculator.vault.feature.onboarding.presentation.mvi

import androidx.lifecycle.viewModelScope
import com.calculator.vault.core.ui.mvi.MviViewModel
import com.calculator.vault.feature.onboarding.domain.usecase.CompleteOnboardingUseCase
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class OnboardingViewModel(
    private val completeOnboarding: CompleteOnboardingUseCase,
) : MviViewModel<OnboardingState, OnboardingIntent, OnboardingEffect>(OnboardingState()) {

    /** Guards against a double tap creating two PIN screens. */
    private var finishing = false

    override fun handleIntent(intent: OnboardingIntent) {
        when (intent) {
            is OnboardingIntent.PageSettled -> setState { copy(page = intent.page) }
            OnboardingIntent.Continue ->
                if (currentState.isLastPage) finish()
                else sendEffect(OnboardingEffect.ScrollTo(currentState.page + 1))
            OnboardingIntent.Skip -> finish()
        }
    }

    private fun finish() {
        if (finishing) return
        finishing = true
        viewModelScope.launch {
            completeOnboarding()
            sendEffect(OnboardingEffect.Finished)
        }
    }
}
