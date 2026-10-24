package com.calculator.vault.feature.onboarding.presentation.ui

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.calculator.vault.core.designsystem.component.VaultButton
import com.calculator.vault.core.designsystem.component.VaultTextButton
import com.calculator.vault.core.designsystem.component.isLandscape
import com.calculator.vault.core.designsystem.preview.ScreenPreviews
import com.calculator.vault.core.designsystem.preview.VaultPreview
import com.calculator.vault.core.designsystem.theme.VaultTheme
import com.calculator.vault.core.ui.mvi.CollectEffects
import com.calculator.vault.feature.onboarding.R
import com.calculator.vault.feature.onboarding.presentation.components.OnboardingIllustration
import com.calculator.vault.feature.onboarding.presentation.components.PagerIndicator
import com.calculator.vault.feature.onboarding.presentation.mvi.OnboardingEffect
import com.calculator.vault.feature.onboarding.presentation.mvi.OnboardingIntent
import com.calculator.vault.feature.onboarding.presentation.mvi.OnboardingState
import com.calculator.vault.feature.onboarding.presentation.mvi.OnboardingViewModel
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import kotlin.math.absoluteValue

internal data class OnboardingPage(@DrawableRes val image: Int, @StringRes val title: Int)

internal val Pages = listOf(
    OnboardingPage(R.drawable.img_onboarding_1, R.string.onboarding_title_1),
    OnboardingPage(R.drawable.img_onboarding_2, R.string.onboarding_title_2),
    OnboardingPage(R.drawable.img_onboarding_3, R.string.onboarding_title_3),
)

@Composable
fun OnboardingRoute(onFinished: () -> Unit, viewModel: OnboardingViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState { Pages.size }
    val scope = rememberCoroutineScope()
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }.collect { viewModel.onIntent(OnboardingIntent.PageSettled(it)) }
    }
    viewModel.effects.CollectEffects { effect ->
        when (effect) {
            is OnboardingEffect.ScrollTo -> scope.launch { pagerState.animateScrollToPage(effect.page) }
            OnboardingEffect.Finished -> onFinished()
        }
    }
    OnboardingScreen(state, pagerState, viewModel::onIntent)
}

@Composable
fun OnboardingScreen(state: OnboardingState, pagerState: PagerState, onIntent: (OnboardingIntent) -> Unit) {
    val landscape = isLandscape()
    Box(Modifier.fillMaxSize().background(VaultTheme.scheme.background)) {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize(), beyondViewportPageCount = 1) { index ->
            val offset = (pagerState.currentPage - index) + pagerState.currentPageOffsetFraction
            val page = Pages[index]
            if (landscape) {
                Row(Modifier.fillMaxSize()) {
                    OnboardingIllustration(page.image, offset, Modifier.weight(1f).fillMaxHeight(), fadeBottom = false)
                    Box(Modifier.weight(1f).fillMaxHeight().padding(horizontal = 32.dp), contentAlignment = Alignment.CenterStart) {
                        PageTitle(page.title, offset, Modifier.padding(bottom = 140.dp))
                    }
                }
            } else {
                Column(Modifier.fillMaxSize()) {
                    OnboardingIllustration(page.image, offset, Modifier.fillMaxWidth().weight(1f))
                    PageTitle(page.title, offset, Modifier.padding(horizontal = 24.dp).padding(bottom = 180.dp))
                }
            }
        }
        // Skip lives on the first page only and fades out as the user swipes past it.
        val skipAlpha by remember(pagerState) {
            derivedStateOf { 1f - (pagerState.currentPage + pagerState.currentPageOffsetFraction).coerceIn(0f, 1f) }
        }
        if (skipAlpha > 0f) {
            VaultTextButton(
                text = stringResource(R.string.onboarding_skip),
                onClick = { onIntent(OnboardingIntent.Skip) },
                color = VaultTheme.colors.skip,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(top = 12.dp, end = 12.dp)
                    .graphicsLayer { alpha = skipAlpha },
            )
        }
        Column(
            Modifier
                .align(if (landscape) Alignment.BottomEnd else Alignment.BottomCenter)
                .fillMaxWidth(if (landscape) 0.5f else 1f)
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            PagerIndicator(count = state.pageCount, current = pagerState.currentPage)
            Spacer(Modifier.height(24.dp))
            VaultButton(
                text = stringResource(R.string.onboarding_continue),
                onClick = { onIntent(OnboardingIntent.Continue) },
                modifier = Modifier.widthIn(max = 520.dp),
            )
        }
    }
}

@Composable
private fun PageTitle(@StringRes title: Int, offset: Float, modifier: Modifier = Modifier) {
    Text(
        text = stringResource(title),
        style = VaultTheme.typography.displaySmall,
        color = VaultTheme.scheme.onBackground,
        modifier = modifier
            .widthIn(max = 520.dp)
            .graphicsLayer {
                alpha = 1f - offset.absoluteValue.coerceAtMost(1f)
                translationX = offset * 120f
            },
    )
}

@ScreenPreviews
@Composable
private fun OnboardingPreview() = VaultPreview {
    OnboardingScreen(OnboardingState(), rememberPagerState { Pages.size }, onIntent = {})
}
