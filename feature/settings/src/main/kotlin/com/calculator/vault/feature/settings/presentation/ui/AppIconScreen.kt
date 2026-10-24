package com.calculator.vault.feature.settings.presentation.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.calculator.vault.core.designsystem.component.SectionLabel
import com.calculator.vault.core.designsystem.component.VaultButton
import com.calculator.vault.core.designsystem.component.VaultCard
import com.calculator.vault.core.designsystem.component.VaultDialog
import com.calculator.vault.core.designsystem.component.VaultTopBar
import com.calculator.vault.core.designsystem.preview.ScreenPreviews
import com.calculator.vault.core.designsystem.preview.VaultPreview
import com.calculator.vault.core.designsystem.theme.VaultTheme
import com.calculator.vault.core.model.AppIconCategory
import com.calculator.vault.core.model.AppIconOption
import com.calculator.vault.core.ui.component.AppIconArt
import com.calculator.vault.core.ui.component.VaultScreen
import com.calculator.vault.core.ui.component.labelRes
import com.calculator.vault.core.ui.component.titleRes
import com.calculator.vault.core.ui.mvi.CollectEffects
import com.calculator.vault.core.ui.util.AppToasts
import com.calculator.vault.feature.settings.R
import com.calculator.vault.feature.settings.presentation.mvi.AppIconEffect
import com.calculator.vault.feature.settings.presentation.mvi.AppIconIntent
import com.calculator.vault.feature.settings.presentation.mvi.AppIconState
import com.calculator.vault.feature.settings.presentation.mvi.AppIconViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppIconRoute(onClose: () -> Unit, viewModel: AppIconViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val changed = stringResource(R.string.settings_icon_changed)
    viewModel.effects.CollectEffects { effect ->
        when (effect) {
            AppIconEffect.Changed -> AppToasts.show(changed)
            AppIconEffect.Close -> onClose()
        }
    }
    AppIconScreen(state, viewModel::onIntent)
}

@Composable
fun AppIconScreen(state: AppIconState, onIntent: (AppIconIntent) -> Unit) {
    VaultScreen(
        topBar = { VaultTopBar(stringResource(R.string.settings_app_icon), onNavigationClick = { onIntent(AppIconIntent.Back) }) },
        bottomBar = { VaultButton(stringResource(R.string.settings_icon_change), { onIntent(AppIconIntent.Change) }, enabled = state.changed) },
    ) {
        Column(
            Modifier.align(Alignment.TopCenter).widthIn(max = 640.dp).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp).padding(top = 8.dp, bottom = 130.dp),
        ) {
            VaultCard(Modifier.fillMaxWidth()) {
                Column(Modifier.fillMaxWidth().padding(vertical = 24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    AnimatedContent(state.selected, transitionSpec = { (fadeIn() + scaleIn(initialScale = 0.8f)) togetherWith fadeOut() }, label = "iconPreview") { icon ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            AppIconArt(icon, size = 88.dp)
                            Spacer(Modifier.height(12.dp))
                            Text(stringResource(icon.labelRes()), style = VaultTheme.typography.titleMedium, color = VaultTheme.scheme.onSurface)
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        stringResource(if (state.changed) R.string.settings_icon_preview else R.string.settings_icon_current),
                        style = VaultTheme.typography.bodySmall,
                        color = VaultTheme.scheme.onSurfaceVariant,
                    )
                }
            }
            AppIconCategory.entries.forEach { category ->
                SectionLabel(stringResource(category.titleRes()), Modifier.padding(top = 20.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                    val options = AppIconOption.entries.filter { it.category == category }
                    options.forEach { option ->
                        IconChoice(option, option == state.selected, option == state.current, { onIntent(AppIconIntent.Select(option)) }, Modifier.weight(1f))
                    }
                    repeat(4 - options.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
    if (state.confirm) {
        VaultDialog(
            title = stringResource(R.string.settings_icon_confirm_title),
            message = stringResource(R.string.settings_icon_confirm_message, stringResource(state.selected.labelRes())),
            confirmText = stringResource(R.string.settings_icon_change),
            onConfirm = { onIntent(AppIconIntent.Confirm) },
            onDismissRequest = { onIntent(AppIconIntent.Dismiss) },
        )
    }
}

@Composable
private fun IconChoice(option: AppIconOption, selected: Boolean, current: Boolean, onClick: () -> Unit, modifier: Modifier) {
    val scale by animateFloatAsState(if (selected) 1.06f else 1f, label = "iconScale")
    val shape = RoundedCornerShape(20.dp)
    Box(
        modifier
            .clip(shape)
            .border(2.dp, if (selected) VaultTheme.scheme.primary else Color.Transparent, shape)
            .clickable(onClick = onClick)
            .padding(10.dp),
        contentAlignment = Alignment.Center,
    ) {
        AppIconArt(option, size = 56.dp, elevated = selected, modifier = Modifier.graphicsLayer { scaleX = scale; scaleY = scale })
        if (current) {
            Text(
                "•",
                color = VaultTheme.scheme.primary,
                style = VaultTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.TopEnd),
            )
        }
    }
}

@ScreenPreviews
@Composable
private fun AppIconPreview() = VaultPreview { AppIconScreen(AppIconState(current = AppIconOption.DEFAULT, selected = AppIconOption.MUSIC_1), onIntent = {}) }
