package com.calculator.vault.feature.splash.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import com.calculator.vault.core.common.VaultConstants
import com.calculator.vault.core.designsystem.component.VaultButton
import com.calculator.vault.core.designsystem.component.isLandscape
import com.calculator.vault.core.designsystem.preview.ScreenPreviews
import com.calculator.vault.core.designsystem.preview.VaultPreview
import com.calculator.vault.core.designsystem.theme.VaultTheme
import com.calculator.vault.core.model.AppIconOption
import com.calculator.vault.core.ui.component.AppIconArt
import com.calculator.vault.core.ui.util.ExternalIntents
import com.calculator.vault.feature.splash.R
import kotlinx.coroutines.launch

/**
 * The first-install splash: the app logo centred in the free space, then "Let's get started
 * with your Calculator Vault", the Get Started button and the legal links. The logo pops in like
 * the regular splash, the title rises after it and the actions follow.
 */
@Composable
fun GetStartedContent(icon: AppIconOption, onGetStarted: () -> Unit, modifier: Modifier = Modifier) {
    // Previews don't run effects, so they start fully shown.
    val shown = LocalInspectionMode.current
    val logoScale = remember { Animatable(if (shown) 1f else 0.6f) }
    val logoAlpha = remember { Animatable(if (shown) 1f else 0f) }
    val title = remember { Animatable(if (shown) 1f else 0f) }
    val actions = remember { Animatable(if (shown) 1f else 0f) }
    LaunchedEffect(Unit) {
        launch { logoAlpha.animateTo(1f, tween(260)) }
        launch { logoScale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)) }
        launch { title.animateTo(1f, tween(durationMillis = 520, delayMillis = 180)) }
        launch { actions.animateTo(1f, tween(durationMillis = 420, delayMillis = 420)) }
    }
    val logo = @Composable { boxModifier: Modifier ->
        Box(boxModifier, contentAlignment = Alignment.Center) {
            AppIconArt(
                icon = icon,
                size = 112.dp,
                modifier = Modifier.graphicsLayer {
                    scaleX = logoScale.value
                    scaleY = logoScale.value
                    alpha = logoAlpha.value
                },
            )
        }
    }
    val texts = @Composable { columnModifier: Modifier ->
        Column(columnModifier.widthIn(max = 520.dp).padding(horizontal = 24.dp, vertical = 20.dp)) {
            Text(
                stringResource(R.string.splash_get_started_title),
                style = VaultTheme.typography.displaySmall,
                color = VaultTheme.scheme.onBackground,
                modifier = Modifier.graphicsLayer {
                    alpha = title.value
                    translationY = (1f - title.value) * 48.dp.toPx()
                },
            )
            Spacer(Modifier.height(40.dp))
            Column(
                Modifier.graphicsLayer {
                    alpha = actions.value
                    translationY = (1f - actions.value) * 24.dp.toPx()
                },
            ) {
                VaultButton(stringResource(R.string.splash_get_started), onGetStarted)
                Spacer(Modifier.height(12.dp))
                LegalText()
            }
        }
    }
    Box(modifier.fillMaxSize().background(VaultTheme.scheme.background).statusBarsPadding().navigationBarsPadding()) {
        if (isLandscape()) {
            Row(Modifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                logo(Modifier.weight(1f).fillMaxHeight())
                Box(Modifier.weight(1f).fillMaxHeight().verticalScroll(rememberScrollState()), contentAlignment = Alignment.Center) {
                    texts(Modifier.fillMaxWidth())
                }
            }
        } else {
            Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                logo(Modifier.weight(1f).fillMaxWidth())
                texts(Modifier.fillMaxWidth())
            }
        }
    }
}

@Composable
private fun LegalText() {
    val context = LocalContext.current
    val linkStyle = TextLinkStyles(SpanStyle(color = VaultTheme.scheme.onSurfaceVariant, textDecoration = TextDecoration.Underline))
    val legal = buildAnnotatedString {
        append(stringResource(R.string.splash_legal_prefix))
        append(' ')
        withLink(LinkAnnotation.Clickable("privacy", linkStyle) { ExternalIntents.openUrl(context, VaultConstants.PRIVACY_POLICY_URL) }) {
            append(stringResource(R.string.splash_privacy))
        }
        append(' ')
        append(stringResource(R.string.splash_legal_and))
        append(' ')
        withLink(LinkAnnotation.Clickable("terms", linkStyle) { ExternalIntents.openUrl(context, VaultConstants.TERMS_URL) }) {
            append(stringResource(R.string.splash_terms))
        }
        append('.')
    }
    Text(
        legal,
        style = VaultTheme.text.caption,
        color = VaultTheme.scheme.onSurfaceVariant,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth(),
    )
}

@ScreenPreviews
@Composable
private fun GetStartedPreview() = VaultPreview { GetStartedContent(AppIconOption.DEFAULT, onGetStarted = {}) }
