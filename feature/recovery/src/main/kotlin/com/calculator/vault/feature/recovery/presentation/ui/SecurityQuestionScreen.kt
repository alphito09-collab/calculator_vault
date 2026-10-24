package com.calculator.vault.feature.recovery.presentation.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.calculator.vault.core.designsystem.component.RowTrailing
import com.calculator.vault.core.designsystem.component.SettingsRow
import com.calculator.vault.core.designsystem.component.VaultBottomSheet
import com.calculator.vault.core.designsystem.component.VaultButton
import com.calculator.vault.core.designsystem.component.VaultCard
import com.calculator.vault.core.designsystem.component.VaultTextField
import com.calculator.vault.core.designsystem.component.VaultTopBar
import com.calculator.vault.core.designsystem.icon.VaultIcons
import com.calculator.vault.core.designsystem.preview.ScreenPreviews
import com.calculator.vault.core.designsystem.preview.VaultPreview
import com.calculator.vault.core.designsystem.theme.VaultTheme
import com.calculator.vault.core.model.SecurityQuestion
import com.calculator.vault.core.ui.component.VaultScreen
import com.calculator.vault.core.ui.mvi.CollectEffects
import com.calculator.vault.feature.recovery.R
import com.calculator.vault.feature.recovery.presentation.components.textRes
import com.calculator.vault.feature.recovery.presentation.mvi.SecurityQuestionEffect
import com.calculator.vault.feature.recovery.presentation.mvi.SecurityQuestionIntent
import com.calculator.vault.feature.recovery.presentation.mvi.SecurityQuestionState
import com.calculator.vault.feature.recovery.presentation.mvi.SecurityQuestionViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun SecurityQuestionRoute(onSaved: () -> Unit, onClose: () -> Unit, viewModel: SecurityQuestionViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    viewModel.effects.CollectEffects { effect ->
        when (effect) {
            SecurityQuestionEffect.Saved -> onSaved()
            SecurityQuestionEffect.Close -> onClose()
        }
    }
    SecurityQuestionScreen(state, viewModel::onIntent)
}

@Composable
fun SecurityQuestionScreen(state: SecurityQuestionState, onIntent: (SecurityQuestionIntent) -> Unit) {
    VaultScreen(
        topBar = { VaultTopBar(stringResource(R.string.recovery_title), onNavigationClick = { onIntent(SecurityQuestionIntent.Back) }) },
        bottomBar = {
            VaultButton(stringResource(R.string.recovery_continue), { onIntent(SecurityQuestionIntent.Save) }, enabled = state.canSave, loading = state.saving)
        },
    ) {
        Column(
            Modifier.align(Alignment.TopCenter).widthIn(max = 560.dp).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(stringResource(R.string.recovery_question_headline), style = VaultTheme.typography.headlineLarge, color = VaultTheme.scheme.onBackground)
            Spacer(Modifier.height(20.dp))
            QuestionSelector(state.question) { onIntent(SecurityQuestionIntent.OpenPicker) }
            VaultTextField(
                value = state.answer,
                onValueChange = { onIntent(SecurityQuestionIntent.AnswerChanged(it)) },
                placeholder = stringResource(R.string.recovery_answer_placeholder),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                isError = state.failed,
                errorText = stringResource(R.string.recovery_error_generic),
            )
            Text(stringResource(R.string.recovery_answer_note), style = VaultTheme.typography.bodySmall, color = VaultTheme.scheme.onSurfaceVariant)
        }
    }
    if (state.pickerOpen) {
        VaultBottomSheet(onDismissRequest = { onIntent(SecurityQuestionIntent.ClosePicker) }) {
            Text(stringResource(R.string.recovery_pick_question), style = VaultTheme.text.dialogTitle, color = VaultTheme.colors.dialogTitle)
            Spacer(Modifier.height(8.dp))
            SecurityQuestion.entries.forEach { question ->
                SettingsRow(
                    title = stringResource(question.textRes()),
                    trailing = RowTrailing.Radio(question == state.question),
                    onClick = { onIntent(SecurityQuestionIntent.QuestionPicked(question)) },
                )
            }
        }
    }
}

@Composable
fun QuestionSelector(question: SecurityQuestion, onClick: () -> Unit) {
    VaultCard(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(horizontal = 20.dp, vertical = 22.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(question.textRes()), style = VaultTheme.text.rowTitle, color = VaultTheme.scheme.onSurface, modifier = Modifier.weight(1f))
            Box(Modifier.size(20.dp), contentAlignment = Alignment.Center) {
                Icon(painterResource(VaultIcons.ChevronRight), null, tint = VaultTheme.colors.iconTileContent, modifier = Modifier.size(14.dp))
            }
        }
    }
}

@ScreenPreviews
@Composable
private fun SecurityQuestionPreview() = VaultPreview { SecurityQuestionScreen(SecurityQuestionState(answer = "Leo"), onIntent = {}) }
