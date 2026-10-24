package com.calculator.vault.core.data.media

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.pdf.PdfRenderer
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.ParcelFileDescriptor
import android.util.Size
import androidx.exifinterface.media.ExifInterface
import com.calculator.vault.core.model.MediaType
import org.koin.core.annotation.Single
import timber.log.Timber
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.math.max

/** Builds small JPEG previews (≤ [MAX_EDGE] px) for grids. Never decodes full-size bitmaps. */
@Single
class ThumbnailFactory(private val context: Context) {

    fun fromUri(uri: Uri, type: MediaType, mimeType: String): ByteArray? = runCatching {
        val bitmap = when (type) {
            MediaType.PHOTO -> systemThumbnail(uri) ?: decodeImage(uri)
            MediaType.VIDEO -> systemThumbnail(uri) ?: videoFrame { setDataSource(context, uri) }
            MediaType.AUDIO -> embeddedArt { setDataSource(context, uri) }
            MediaType.FILE -> when {
                mimeType.startsWith("image/") -> decodeImage(uri)
                mimeType == "application/pdf" -> context.contentResolver.openFileDescriptor(uri, "r")?.let(::pdfFirstPage)
                else -> null
            }
        }
        bitmap?.toJpeg()
    }.onFailure { Timber.w(it, "Thumbnail failed for $uri") }.getOrNull()

    fun fromFile(file: File, type: MediaType, mimeType: String): ByteArray? = fromUri(Uri.fromFile(file), type, mimeType)
        ?: runCatching {
            when (type) {
                MediaType.VIDEO -> videoFrame { setDataSource(file.absolutePath) }
                MediaType.AUDIO -> embeddedArt { setDataSource(file.absolutePath) }
                else -> null
            }?.toJpeg()
        }.getOrNull()

    private fun systemThumbnail(uri: Uri): Bitmap? {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || uri.scheme != ContentResolver.SCHEME_CONTENT) return null
        return runCatching { context.contentResolver.loadThumbnail(uri, Size(MAX_EDGE, MAX_EDGE), null) }.getOrNull()
    }

    private fun decodeImage(uri: Uri): Bitmap? {
        val resolver = context.contentResolver
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth <= 0) return null
        var sample = 1
        while (max(bounds.outWidth, bounds.outHeight) / (sample * 2) >= MAX_EDGE) sample *= 2
        val bitmap = resolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, BitmapFactory.Options().apply { inSampleSize = sample })
        } ?: return null
        val rotation = resolver.openInputStream(uri)?.use { ExifInterface(it).rotationDegrees } ?: 0
        return if (rotation == 0) bitmap else bitmap.rotate(rotation)
    }

    private inline fun videoFrame(setSource: MediaMetadataRetriever.() -> Unit): Bitmap? =
        MediaMetadataRetriever().useRetriever { r ->
            r.setSource()
            r.getScaledFrameAtTime(1_000_000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC, MAX_EDGE, MAX_EDGE)
        }

    private inline fun embeddedArt(setSource: MediaMetadataRetriever.() -> Unit): Bitmap? =
        MediaMetadataRetriever().useRetriever { r ->
            r.setSource()
            r.embeddedPicture?.let { bytes ->
                val opts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size, opts)
                var sample = 1
                while (max(opts.outWidth, opts.outHeight) / (sample * 2) >= MAX_EDGE) sample *= 2
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size, BitmapFactory.Options().apply { inSampleSize = sample })
            }
        }

    private fun pdfFirstPage(fd: ParcelFileDescriptor): Bitmap? = fd.use { descriptor ->
        PdfRenderer(descriptor).use { renderer ->
            if (renderer.pageCount == 0) return null
            renderer.openPage(0).use { page ->
                val scale = MAX_EDGE.toFloat() / max(page.width, page.height)
                val bitmap = Bitmap.createBitmap(
                    (page.width * scale).toInt().coerceAtLeast(1),
                    (page.height * scale).toInt().coerceAtLeast(1),
                    Bitmap.Config.ARGB_8888,
                )
                bitmap.eraseColor(android.graphics.Color.WHITE)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                bitmap
            }
        }
    }

    private fun Bitmap.rotate(degrees: Int): Bitmap {
        val rotated = Bitmap.createBitmap(this, 0, 0, width, height, Matrix().apply { postRotate(degrees.toFloat()) }, true)
        if (rotated != this) recycle()
        return rotated
    }

    private fun Bitmap.toJpeg(): ByteArray {
        val scaled = if (max(width, height) > MAX_EDGE) {
            val factor = MAX_EDGE.toFloat() / max(width, height)
            Bitmap.createScaledBitmap(this, (width * factor).toInt(), (height * factor).toInt(), true)
        } else this
        return ByteArrayOutputStream().use { out ->
            scaled.compress(Bitmap.CompressFormat.JPEG, 82, out)
            if (scaled != this) scaled.recycle()
            recycle()
            out.toByteArray()
        }
    }

    private inline fun <T> MediaMetadataRetriever.useRetriever(block: (MediaMetadataRetriever) -> T): T? = try {
        block(this)
    } catch (error: Exception) {
        Timber.w(error, "MediaMetadataRetriever failed")
        null
    } finally {
        runCatching { release() }
    }

    companion object {
        const val MAX_EDGE = 384
    }
}
