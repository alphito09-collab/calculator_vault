package com.calculator.vault.feature.home.presentation.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.calculator.vault.core.designsystem.component.InfoBanner
import com.calculator.vault.core.designsystem.component.SectionLabel
import com.calculator.vault.core.designsystem.component.VaultTopBar
import com.calculator.vault.core.designsystem.component.brandTitle
import com.calculator.vault.core.designsystem.icon.VaultIcons
import com.calculator.vault.core.designsystem.preview.ScreenPreviews
import com.calculator.vault.core.designsystem.preview.VaultPreview
import com.calculator.vault.core.designsystem.theme.VaultTheme
import com.calculator.vault.core.model.MediaType
import com.calculator.vault.core.model.VaultCounts
import com.calculator.vault.core.ui.component.ResetCodeBanner
import com.calculator.vault.feature.home.R
import com.calculator.vault.feature.home.domain.usecase.HomeBanner
import com.calculator.vault.feature.home.presentation.components.CategoryCard
import com.calculator.vault.feature.home.presentation.components.ToolTile
import com.calculator.vault.feature.home.presentation.mvi.HomeIntent
import com.calculator.vault.feature.home.presentation.mvi.HomeState
import com.calculator.vault.feature.home.presentation.mvi.HomeTool

/** The Home tab: title, promo banner, Tools and Files. */
@Composable
fun HomeContent(state: HomeState, onIntent: (HomeIntent) -> Unit, bottomPadding: PaddingValues) {
    Column(Modifier.fillMaxSize()) {
        VaultTopBar(brandTitle(stringResource(R.string.home_title_first), stringResource(R.string.home_title_accent)), navigationIcon = null)
        BoxWithConstraints(Modifier.fillMaxSize()) {
            val columns = if (maxWidth >= 600.dp) 4 else 2
            Column(
                Modifier
                    .align(Alignment.TopCenter)
                    .widthIn(max = 900.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(top = 8.dp)
                    .padding(bottomPadding),
            ) {
                AnimatedContent(
                    targetState = state.banner,
                    transitionSpec = { (fadeIn() + expandVertically()) togetherWith (fadeOut() + shrinkVertically()) },
                    label = "homeBanner",
                ) { banner ->
                    if (banner != null) HomeBannerCard(banner, onIntent, Modifier.padding(bottom = 20.dp))
                    else Spacer(Modifier.fillMaxWidth())
                }
                SectionLabel(stringResource(R.string.home_tools))
                Grid(columns, toolItems(state, onIntent))
                Spacer(Modifier.height(20.dp))
                SectionLabel(stringResource(R.string.home_files))
                Grid(columns, categoryItems(state.counts, onIntent))
            }
        }
    }
}

@Composable
private fun HomeBannerCard(banner: HomeBanner, onIntent: (HomeIntent) -> Unit, modifier: Modifier) {
    val onClick = { onIntent(HomeIntent.BannerClicked) }
    val onDismiss = { onIntent(HomeIntent.BannerDismissed) }
    when (banner) {
        HomeBanner.ADD_RECOVERY -> InfoBanner(
            title = stringResource(R.string.home_banner_recovery_title),
            message = stringResource(R.string.home_banner_recovery_message),
            icon = VaultIcons.AddCircle,
            onClick = onClick,
            onDismiss = onDismiss,
            modifier = modifier,
        )
        HomeBanner.ENABLE_BACKUP -> InfoBanner(
            title = stringResource(R.string.home_banner_backup_title),
            message = stringResource(R.string.home_banner_backup_message),
            icon = VaultIcons.Cloud,
            onClick = onClick,
            onDismiss = onDismiss,
            modifier = modifier,
        )
        HomeBanner.RESET_CODE -> ResetCodeBanner(onDismiss = onDismiss, modifier = modifier)
    }
}

@Composable
private fun Grid(columns: Int, items: List<@Composable (Modifier) -> Unit>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        items.chunked(columns).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { item -> item(Modifier.weight(1f)) }
                repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun toolItems(state: HomeState, onIntent: (HomeIntent) -> Unit): List<@Composable (Modifier) -> Unit> {
    val c = VaultTheme.colors
    return listOf(
        Triple(HomeTool.APP_LOCK, VaultIcons.Lock, R.string.home_tool_app_lock) to c.lockAccent,
        Triple(HomeTool.THEMES, VaultIcons.Brush, R.string.home_tool_themes) to c.themeAccent,
        Triple(HomeTool.CLOUD_BACKUP, VaultIcons.CloudUpload, R.string.home_tool_cloud_backup) to c.backupAccent,
        Triple(HomeTool.INTRUDER, VaultIcons.ShieldCheck, R.string.home_tool_intruder) to c.intruderAccent,
    ).map { (tool, tint) ->
        { modifier: Modifier ->
            ToolTile(tool.second, stringResource(tool.third), tint, { onIntent(HomeIntent.OpenTool(tool.first)) }, modifier)
        }
    }
}

@Composable
private fun categoryItems(counts: VaultCounts, onIntent: (HomeIntent) -> Unit): List<@Composable (Modifier) -> Unit> {
    val c = VaultTheme.colors
    return listOf(
        Triple(MediaType.PHOTO, VaultIcons.Photo, R.string.home_photos) to c.photoAccent,
        Triple(MediaType.VIDEO, VaultIcons.Video, R.string.home_videos) to c.videoAccent,
        Triple(MediaType.AUDIO, VaultIcons.Music, R.string.home_audio) to c.audioAccent,
        Triple(MediaType.FILE, VaultIcons.File, R.string.home_files_category) to c.fileAccent,
    ).map { (category, accent) ->
        { modifier: Modifier ->
            val count = counts[category.first]
            CategoryCard(
                icon = category.second,
                title = stringResource(category.third),
                subtitle = pluralStringResource(R.plurals.home_items, count, count),
                accent = accent,
                onClick = { onIntent(HomeIntent.OpenCategory(category.first)) },
                modifier = modifier,
            )
        }
    }
}

@ScreenPreviews
@Composable
private fun HomeContentPreview() = VaultPreview {
    HomeContent(
        HomeState(counts = VaultCounts(128, 24, 9, 17), banner = HomeBanner.ENABLE_BACKUP),
        onIntent = {},
        bottomPadding = PaddingValues(bottom = 140.dp),
    )
}
