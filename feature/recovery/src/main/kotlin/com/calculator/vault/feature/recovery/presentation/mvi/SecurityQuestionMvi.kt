package com.calculator.vault.feature.recovery.presentation.mvi

import androidx.lifecycle.viewModelScope
import com.calculator.vault.core.model.SecurityQuestion
import com.calculator.vault.core.ui.mvi.MviViewModel
import com.calculator.vault.core.ui.mvi.UiEffect
import com.calculator.vault.core.ui.mvi.UiIntent
import com.calculator.vault.core.ui.mvi.UiState
import com.calculator.vault.feature.recovery.domain.usecase.SaveSecurityAnswerUseCase
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

data class SecurityQuestionState(
    val question: SecurityQuestion = SecurityQuestion.FIRST_PET,
    val answer: String = "",
    val pickerOpen: Boolean = false,
    val saving: Boolean = false,
    val failed: Boolean = false,
) : UiState {
    val canSave: Boolean get() = answer.trim().length >= 2 && !saving
}

sealed interface SecurityQuestionIntent : UiIntent {
    data object OpenPicker : SecurityQuestionIntent
    data object ClosePicker : SecurityQuestionIntent
    data class QuestionPicked(val question: SecurityQuestion) : SecurityQuestionIntent
    data class AnswerChanged(val value: String) : SecurityQuestionIntent
    data object Save : SecurityQuestionIntent
    data object Back : SecurityQuestionIntent
}

sealed interface SecurityQuestionEffect : UiEffect {
    data object Saved : SecurityQuestionEffect
    data object Close : SecurityQuestionEffect
}

@KoinViewModel
class SecurityQuestionViewModel(
    private val saveAnswer: SaveSecurityAnswerUseCase,
) : MviViewModel<SecurityQuestionState, SecurityQuestionIntent, SecurityQuestionEffect>(SecurityQuestionState()) {

    override fun handleIntent(intent: SecurityQuestionIntent) {
        when (intent) {
            SecurityQuestionIntent.OpenPicker -> setState { copy(pickerOpen = true) }
            SecurityQuestionIntent.ClosePicker -> setState { copy(pickerOpen = false) }
            is SecurityQuestionIntent.QuestionPicked -> setState { copy(question = intent.question, pickerOpen = false) }
            is SecurityQuestionIntent.AnswerChanged -> setState { copy(answer = intent.value, failed = false) }
            SecurityQuestionIntent.Back -> sendEffect(SecurityQuestionEffect.Close)
            SecurityQuestionIntent.Save -> {
                if (!currentState.canSave) return
                setState { copy(saving = true) }
                viewModelScope.launch {
                    saveAnswer(currentState.question, currentState.answer)
                        .onSuccess { sendEffect(SecurityQuestionEffect.Saved) }
                        .onFailure { setState { copy(saving = false, failed = true) } }
                }
            }
        }
    }
}
