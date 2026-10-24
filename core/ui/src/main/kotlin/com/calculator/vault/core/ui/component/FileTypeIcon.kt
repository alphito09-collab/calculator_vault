package com.calculator.vault.core.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.calculator.vault.core.designsystem.icon.VaultIcons
import com.calculator.vault.core.designsystem.preview.ThemePreviews
import com.calculator.vault.core.designsystem.preview.VaultPreview
import com.calculator.vault.core.designsystem.theme.VaultTheme
import com.calculator.vault.core.model.FileKind

/**
 * A page with a folded corner in the file type's colour, its glyph (lines, grid, slide, zip…) and
 * a badge with the real extension, so "Tax_2025.pdf" reads as a PDF at a glance.
 */
@Composable
fun FileTypeIcon(fileName: String, mimeType: String?, modifier: Modifier = Modifier, size: Dp = 44.dp) {
    val kind = remember(fileName, mimeType) { FileKind.of(fileName, mimeType) }
    val label = remember(fileName, kind) { FileKind.badgeLabel(fileName, kind) }
    val color = kind.color()
    val glyph = kind.glyph()?.let { painterResource(it) }
    val dark = VaultTheme.colors.isDark
    val measurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val sizePx = with(density) { size.toPx() }
    // Shrink the badge text until the longest label ("XLSX", "BOOK") fits.
    val labelLayout = remember(label, sizePx, measurer) {
        var fontPx = sizePx * 0.2f
        var layout = measurer.measure(label, BadgeStyle.copy(fontSize = with(density) { fontPx.toSp() }), maxLines = 1, softWrap = false)
        while (layout.size.width > sizePx * 0.62f && fontPx > sizePx * 0.1f) {
            fontPx *= 0.9f
            layout = measurer.measure(label, BadgeStyle.copy(fontSize = with(density) { fontPx.toSp() }), maxLines = 1, softWrap = false)
        }
        layout
    }
    Canvas(modifier.size(size)) {
        val s = this.size.minDimension
        val pageWidth = s * 0.7f
        val pageHeight = s * 0.88f
        val left = (s - pageWidth) / 2f + s * 0.04f
        val top = (s - pageHeight) / 2f
        val right = left + pageWidth
        val bottom = top + pageHeight
        val radius = s * 0.08f
        val fold = pageWidth * 0.3f
        val outline = Stroke(width = s * 0.028f, join = StrokeJoin.Round)

        val page = Path().apply {
            moveTo(left + radius, top)
            lineTo(right - fold, top)
            lineTo(right, top + fold)
            lineTo(right, bottom - radius)
            quadraticTo(right, bottom, right - radius, bottom)
            lineTo(left + radius, bottom)
            quadraticTo(left, bottom, left, bottom - radius)
            lineTo(left, top + radius)
            quadraticTo(left, top, left + radius, top)
            close()
        }
        drawPath(page, color.copy(alpha = if (dark) 0.2f else 0.1f))
        drawPath(page, color.copy(alpha = if (dark) 0.6f else 0.42f), style = outline)

        val foldRadius = fold * 0.25f
        val corner = Path().apply {
            moveTo(right - fold, top)
            lineTo(right - fold, top + fold - foldRadius)
            quadraticTo(right - fold, top + fold, right - fold + foldRadius, top + fold)
            lineTo(right, top + fold)
            close()
        }
        drawPath(corner, color.copy(alpha = if (dark) 0.55f else 0.35f))
        drawPath(corner, color.copy(alpha = if (dark) 0.6f else 0.42f), style = outline)

        if (glyph != null) {
            val glyphSize = s * 0.32f
            translate((left + right - fold * 0.35f) / 2f - glyphSize / 2f, top + pageHeight * 0.33f - glyphSize / 2f) {
                with(glyph) { draw(Size(glyphSize, glyphSize), colorFilter = ColorFilter.tint(color)) }
            }
        }

        // The badge pokes out past the page's left edge, like a label stuck on a document.
        val badgeHeight = s * 0.27f
        val badgeTop = top + pageHeight * 0.6f
        val badgeLeft = left - s * 0.08f
        val badgeWidth = right - s * 0.03f - badgeLeft
        drawRoundRect(color, Offset(badgeLeft, badgeTop), Size(badgeWidth, badgeHeight), CornerRadius(s * 0.06f))
        drawText(
            labelLayout,
            color = Color.White,
            topLeft = Offset(
                badgeLeft + (badgeWidth - labelLayout.size.width) / 2f,
                badgeTop + (badgeHeight - labelLayout.size.height) / 2f,
            ),
        )
    }
}

private val BadgeStyle = TextStyle(fontWeight = FontWeight.Bold, color = Color.White)

@Composable
@ReadOnlyComposable
private fun FileKind.color(): Color = when (this) {
    FileKind.PDF -> Color(0xFFE5383B)
    FileKind.WORD -> Color(0xFF2F6FDE)
    FileKind.SPREADSHEET -> Color(0xFF1E8449)
    FileKind.PRESENTATION -> Color(0xFFE2622B)
    FileKind.TEXT -> Color(0xFF64748B)
    FileKind.CODE -> Color(0xFF0F8F86)
    FileKind.ARCHIVE -> Color(0xFFC98A0B)
    FileKind.APK -> Color(0xFF5E9A2C)
    FileKind.EBOOK -> Color(0xFF8D5A3B)
    FileKind.IMAGE -> Color(0xFF7B61FF)
    FileKind.AUDIO -> Color(0xFFEC4899)
    FileKind.VIDEO -> Color(0xFFB13BD6)
    FileKind.CONTACT -> Color(0xFF4F46E5)
    FileKind.OTHER -> if (VaultTheme.colors.isDark) Color(0xFF9AA1AD) else Color(0xFF78808C)
}

@DrawableRes
private fun FileKind.glyph(): Int? = when (this) {
    FileKind.PDF -> VaultIcons.GlyphText
    FileKind.WORD, FileKind.TEXT -> VaultIcons.GlyphLines
    FileKind.SPREADSHEET -> VaultIcons.GlyphSheet
    FileKind.PRESENTATION -> VaultIcons.GlyphSlides
    FileKind.CODE -> VaultIcons.GlyphCode
    FileKind.ARCHIVE -> VaultIcons.GlyphArchive
    FileKind.APK -> VaultIcons.GlyphPackage
    FileKind.EBOOK -> VaultIcons.GlyphBook
    FileKind.IMAGE -> VaultIcons.GlyphImage
    FileKind.AUDIO -> VaultIcons.GlyphMusic
    FileKind.VIDEO -> VaultIcons.Play
    FileKind.CONTACT -> VaultIcons.GlyphContact
    FileKind.OTHER -> null
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@ThemePreviews
@Composable
private fun FileTypeIconPreview() = VaultPreview {
    val names = listOf(
        "Tax_2025.pdf", "Report.docx", "Budget.xlsx", "Pitch.pptx", "notes.txt", "data.json", "backup.zip",
        "app.apk", "novel.epub", "scan.jpg", "memo.mp3", "clip.mp4", "friends.vcf", "unknown.bin",
    )
    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        FlowRow(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            names.forEach { FileTypeIcon(it, null) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            FileTypeIcon("Lease agreement.pdf", null, size = 88.dp)
            FileTypeIcon("Budget.xlsx", null, size = 88.dp)
        }
    }
}
