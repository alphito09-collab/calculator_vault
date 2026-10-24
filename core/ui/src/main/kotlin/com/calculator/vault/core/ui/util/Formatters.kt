package com.calculator.vault.core.ui.util

import android.content.Context
import android.text.format.Formatter
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import com.calculator.vault.core.model.MediaType
import com.calculator.vault.core.model.SpecialAlbums
import com.calculator.vault.core.ui.R
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.TimeUnit

fun formatBytes(context: Context, bytes: Long): String = Formatter.formatShortFileSize(context, bytes)

/** 3:58, 1:02:03 */
fun formatDuration(ms: Long): String {
    val totalSeconds = TimeUnit.MILLISECONDS.toSeconds(ms.coerceAtLeast(0))
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    val s = totalSeconds % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%d:%02d".format(m, s)
}

/** "Today", "Yesterday", or "17 sep". */
@Composable
@ReadOnlyComposable
fun formatSectionDate(date: LocalDate): String {
    val today = LocalDate.now()
    return when (date) {
        today -> stringResource(R.string.ui_today)
        today.minusDays(1) -> stringResource(R.string.ui_yesterday)
        else -> {
            val locale = LocalConfiguration.current.locales[0] ?: Locale.getDefault()
            val pattern = if (date.year == today.year) "d MMM" else "d MMM yyyy"
            date.format(DateTimeFormatter.ofPattern(pattern, locale)).lowercase(locale)
        }
    }
}

/** "Today, 14:22" / "Yesterday, 21:07" / "Jul 23, 2020 - 17:30". */
@Composable
@ReadOnlyComposable
fun formatTimestamp(epochMs: Long): String {
    val zone = ZoneId.systemDefault()
    val dateTime = Instant.ofEpochMilli(epochMs).atZone(zone)
    val locale = LocalConfiguration.current.locales[0] ?: Locale.getDefault()
    val time = dateTime.format(DateTimeFormatter.ofPattern("HH:mm", locale))
    val today = LocalDate.now(zone)
    return when (dateTime.toLocalDate()) {
        today -> stringResource(R.string.ui_today_at, time)
        today.minusDays(1) -> stringResource(R.string.ui_yesterday_at, time)
        else -> dateTime.format(DateTimeFormatter.ofPattern("MMM d, yyyy - HH:mm", locale))
    }
}

@Composable
@ReadOnlyComposable
fun formatBytes(bytes: Long): String = formatBytes(LocalContext.current, bytes)

/** Plural label of a content type: "photos", "videos", "audio", "files". */
@Composable
@ReadOnlyComposable
fun typeLabel(type: MediaType, count: Int = 2): String = when (type) {
    MediaType.PHOTO -> if (count == 1) stringResource(R.string.ui_type_photo) else stringResource(R.string.ui_type_photos)
    MediaType.VIDEO -> if (count == 1) stringResource(R.string.ui_type_video) else stringResource(R.string.ui_type_videos)
    MediaType.AUDIO -> stringResource(R.string.ui_type_audio)
    MediaType.FILE -> if (count == 1) stringResource(R.string.ui_type_file) else stringResource(R.string.ui_type_files)
}

@Composable
@ReadOnlyComposable
fun typeTitle(type: MediaType): String = when (type) {
    MediaType.PHOTO -> stringResource(R.string.ui_title_photos)
    MediaType.VIDEO -> stringResource(R.string.ui_title_videos)
    MediaType.AUDIO -> stringResource(R.string.ui_title_audio)
    MediaType.FILE -> stringResource(R.string.ui_title_files)
}

/** Display name for an album key, translating [SpecialAlbums] placeholders. */
@Composable
@ReadOnlyComposable
fun albumLabel(album: String): String = when (album) {
    SpecialAlbums.STORAGE_ROOT -> stringResource(R.string.ui_album_storage_root)
    SpecialAlbums.DEVICE_STORAGE -> stringResource(R.string.ui_album_device_storage)
    SpecialAlbums.OTHER -> stringResource(R.string.ui_album_other)
    else -> album
}
