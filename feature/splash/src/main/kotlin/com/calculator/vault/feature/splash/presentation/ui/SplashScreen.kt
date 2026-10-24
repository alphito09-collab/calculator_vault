package com.calculator.vault.feature.splash.presentation.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.calculator.vault.core.designsystem.preview.ScreenPreviews
import com.calculator.vault.core.designsystem.preview.VaultPreview
import com.calculator.vault.core.designsystem.theme.VaultTheme
import com.calculator.vault.core.model.AppIconOption
import com.calculator.vault.core.ui.component.AppIconArt
import com.calculator.vault.core.ui.component.labelRes
import com.calculator.vault.core.ui.mvi.CollectEffects
import com.calculator.vault.feature.splash.domain.usecase.StartDestination
import com.calculator.vault.feature.splash.presentation.mvi.SplashEffect
import com.calculator.vault.feature.splash.presentation.components.GetStartedContent
import com.calculator.vault.feature.splash.presentation.mvi.SplashIntent
import com.calculator.vault.feature.splash.presentation.mvi.SplashMode
import com.calculator.vault.feature.splash.presentation.mvi.SplashState
import com.calculator.vault.feature.splash.presentation.mvi.SplashViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun SplashRoute(
    onNavigate: (StartDestination) -> Unit,
    onGetStarted: () -> Unit,
    viewModel: SplashViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    // Recents keeps the activity resumed, so also wait for window focus: the splash should play
    // when the user actually comes back, not while the app sits in the Recents carousel.
    val lifecycleState by LocalLifecycleOwner.current.lifecycle.currentStateFlow.collectAsState()
    val visible = lifecycleState.isAtLeast(Lifecycle.State.RESUMED) && LocalWindowInfo.current.isWindowFocused
    LaunchedEffect(visible) { if (visible) viewModel.onIntent(SplashIntent.Visible) }
    viewModel.effects.CollectEffects { effect ->
        when (effect) {
            is SplashEffect.Navigate -> onNavigate(effect.destination)
            SplashEffect.ShowLanguage -> onGetStarted()
        }
    }
    SplashScreen(state, onGetStarted = { viewModel.onIntent(SplashIntent.GetStarted) })
}

/** First install shows the "Get Started" welcome; every later launch the disguise's icon. */
@Composable
fun SplashScreen(state: SplashState, onGetStarted: () -> Unit) {
    when (state.mode) {
        SplashMode.LOADING -> Box(Modifier.fillMaxSize().background(VaultTheme.scheme.background))
        SplashMode.WELCOME -> GetStartedContent(state.icon ?: AppIconOption.DEFAULT, onGetStarted)
        SplashMode.BRAND -> BrandSplash(state.icon ?: AppIconOption.DEFAULT)
    }
}

/** The selected launcher disguise pops in, then its label fades up beneath it. */
@Composable
private fun BrandSplash(icon: AppIconOption) {
    // Previews don't run effects, so they start fully shown.
    val preview = LocalInspectionMode.current
    val scale = remember { Animatable(if (preview) 1f else 0.6f) }
    val alpha = remember { Animatable(if (preview) 1f else 0f) }
    val labelAlpha = remember { Animatable(if (preview) 1f else 0f) }
    LaunchedEffect(Unit) {
        launch { alpha.animateTo(1f, tween(260)) }
        launch { scale.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow)) }
        launch { labelAlpha.animateTo(1f, tween(400, delayMillis = 220)) }
    }
    Box(Modifier.fillMaxSize().background(VaultTheme.scheme.background), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            AppIconArt(
                icon = icon,
                size = 104.dp,
                modifier = Modifier.graphicsLayer {
                    scaleX = scale.value
                    scaleY = scale.value
                    this.alpha = alpha.value
                },
            )
            Spacer(Modifier.height(20.dp))
            Text(
                stringResource(icon.labelRes()),
                style = VaultTheme.typography.headlineSmall,
                color = VaultTheme.scheme.onBackground,
                modifier = Modifier.graphicsLayer {
                    this.alpha = labelAlpha.value
                    translationY = (1f - labelAlpha.value) * 24f
                },
            )
        }
    }
}

@ScreenPreviews
@Composable
private fun SplashPreview() = VaultPreview { SplashScreen(SplashState(SplashMode.BRAND, AppIconOption.DEFAULT), onGetStarted = {}) }

@ScreenPreviews
@Composable
private fun SplashWelcomePreview() = VaultPreview { SplashScreen(SplashState(SplashMode.WELCOME), onGetStarted = {}) }
