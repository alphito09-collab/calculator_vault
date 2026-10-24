package com.calculator.vault.feature.audios.presentation.ui

import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.media3.common.util.UnstableApi
import com.calculator.vault.core.designsystem.component.FilterChipRow
import com.calculator.vault.core.designsystem.component.VaultCard
import com.calculator.vault.core.designsystem.component.VaultDropdownMenu
import com.calculator.vault.core.designsystem.component.VaultMenuItem
import com.calculator.vault.core.designsystem.component.VaultSlider
import com.calculator.vault.core.designsystem.component.VaultSwitch
import com.calculator.vault.core.designsystem.component.VaultTopBar
import com.calculator.vault.core.designsystem.preview.ScreenPreviews
import com.calculator.vault.core.designsystem.preview.VaultPreview
import com.calculator.vault.core.designsystem.theme.VaultRadius
import com.calculator.vault.core.designsystem.theme.VaultTheme
import com.calculator.vault.core.media.player.EqBand
import com.calculator.vault.core.media.player.EqualizerState
import com.calculator.vault.core.media.player.ReverbPreset
import com.calculator.vault.core.ui.component.VaultScreen
import com.calculator.vault.core.ui.mvi.CollectEffects
import com.calculator.vault.feature.audios.R
import com.calculator.vault.feature.audios.presentation.components.VerticalSlider
import com.calculator.vault.feature.audios.presentation.mvi.EqualizerEffect
import com.calculator.vault.feature.audios.presentation.mvi.EqualizerIntent
import com.calculator.vault.feature.audios.presentation.mvi.EqualizerScreenState
import com.calculator.vault.feature.audios.presentation.mvi.EqualizerViewModel
import org.koin.androidx.compose.koinViewModel

@OptIn(UnstableApi::class)
@Composable
fun EqualizerRoute(onClose: () -> Unit, viewModel: EqualizerViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    viewModel.effects.CollectEffects { if (it == EqualizerEffect.Close) onClose() }
    EqualizerScreen(state, viewModel::onIntent)
}

@Composable
fun EqualizerScreen(state: EqualizerScreenState, onIntent: (EqualizerIntent) -> Unit) {
    val eq = state.eq
    VaultScreen(
        topBar = {
            VaultTopBar(stringResource(R.string.audios_equalizer), onNavigationClick = { onIntent(EqualizerIntent.Back) }) {
                VaultSwitch(eq.enabled, { onIntent(EqualizerIntent.Enabled(it)) }, enabled = eq.available, modifier = Modifier.padding(end = 12.dp))
            }
        },
    ) {
        Column(Modifier.align(Alignment.TopCenter).widthIn(max = 640.dp).verticalScroll(rememberScrollState()).padding(bottom = 32.dp)) {
            if (!eq.available) {
                Text(stringResource(R.string.audios_eq_unavailable), style = VaultTheme.typography.bodyMedium, color = VaultTheme.scheme.onSurfaceVariant, modifier = Modifier.padding(20.dp))
                return@Column
            }
            FilterChipRow(
                options = eq.presets.indices.toList(),
                selected = eq.currentPreset,
                label = { eq.presets[it] },
                onSelect = { onIntent(EqualizerIntent.Preset(it)) },
                modifier = Modifier.padding(vertical = 8.dp),
            )
            VaultCard(Modifier.padding(horizontal = 20.dp, vertical = 12.dp).fillMaxWidth()) {
                Row(Modifier.fillMaxWidth().padding(vertical = 20.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                    eq.bands.forEachIndexed { index, band ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("%.1f".format(band.levelDb), style = VaultTheme.text.caption, color = VaultTheme.scheme.onSurfaceVariant)
                            Box(Modifier.height(220.dp).padding(vertical = 8.dp)) {
                                VerticalSlider(band.levelDb, band.minDb, band.maxDb, { onIntent(EqualizerIntent.Band(index, it)) }, enabled = eq.enabled)
                            }
                            Text(formatHz(band.centerHz), style = VaultTheme.text.caption, color = VaultTheme.scheme.onSurfaceVariant)
                        }
                    }
                }
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(stringResource(R.string.audios_reverb), style = VaultTheme.typography.headlineSmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Medium), color = VaultTheme.scheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                Box {
                    Text(
                        state.eq.reverb.label(),
                        style = VaultTheme.text.rowTitle,
                        color = androidx.compose.ui.graphics.Color.White,
                        modifier = Modifier
                            .clip(VaultRadius.Pill)
                            .background(if (eq.enabled) VaultTheme.scheme.primary else VaultTheme.colors.disabledContainer)
                            .clickable(enabled = eq.enabled) { onIntent(EqualizerIntent.OpenReverb) }
                            .padding(horizontal = 28.dp, vertical = 14.dp),
                    )
                    VaultDropdownMenu(state.reverbMenuOpen, { onIntent(EqualizerIntent.CloseReverb) }) {
                        ReverbPreset.entries.forEach { preset -> VaultMenuItem(preset.label(), { onIntent(EqualizerIntent.Reverb(preset)) }) }
                    }
                }
            }
            LabeledSlider(stringResource(R.string.audios_bass), eq.bassBoost, eq.enabled) { onIntent(EqualizerIntent.Bass(it)) }
            LabeledSlider(stringResource(R.string.audios_virtualizer), eq.virtualizer, eq.enabled) { onIntent(EqualizerIntent.Virtualizer(it)) }
        }
    }
}

@Composable
private fun LabeledSlider(label: String, value: Float, enabled: Boolean, onChange: (Float) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = VaultTheme.text.rowTitle, color = VaultTheme.scheme.onSurfaceVariant, modifier = Modifier.width(110.dp))
        VaultSlider(value, onChange, Modifier.weight(1f), enabled = enabled)
    }
}

@Composable
private fun ReverbPreset.label(): String = stringResource(
    when (this) {
        ReverbPreset.NONE -> R.string.audios_reverb_none
        ReverbPreset.SMALL_ROOM -> R.string.audios_reverb_small_room
        ReverbPreset.MEDIUM_ROOM -> R.string.audios_reverb_medium_room
        ReverbPreset.LARGE_ROOM -> R.string.audios_reverb_large_room
        ReverbPreset.MEDIUM_HALL -> R.string.audios_reverb_medium_hall
        ReverbPreset.LARGE_HALL -> R.string.audios_reverb_large_hall
        ReverbPreset.PLATE -> R.string.audios_reverb_plate
    },
)

private fun formatHz(hz: Int): String = if (hz >= 1000) "${hz / 1000} kHz" else "$hz Hz"

@ScreenPreviews
@Composable
private fun EqualizerPreview() = VaultPreview {
    EqualizerScreen(
        EqualizerScreenState(
            EqualizerState(
                available = true,
                enabled = true,
                presets = listOf("Flat", "Classical", "Dance", "Folk", "Heavy Metal"),
                currentPreset = 0,
                bands = listOf(60, 230, 910, 4000, 14000).mapIndexed { i, hz -> EqBand(hz, if (i == 3) 6f else 0f, -15f, 15f) },
                bassBoost = 0.45f,
                virtualizer = 0.45f,
            ),
        ),
        onIntent = {},
    )
}
