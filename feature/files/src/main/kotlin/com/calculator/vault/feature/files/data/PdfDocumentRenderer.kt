package com.calculator.vault.feature.files.data

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.LruCache
import com.calculator.vault.feature.files.domain.repository.DocumentRenderer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.koin.core.annotation.Factory
import java.io.File
import kotlin.math.min

/**
 * [PdfRenderer] allows one open page at a time, so rendering is serialized behind a mutex.
 * Rendered pages are kept in a small LRU cache bounded by bytes.
 */
@Factory(binds = [DocumentRenderer::class])
class PdfDocumentRenderer : DocumentRenderer {
    private var descriptor: ParcelFileDescriptor? = null
    private var renderer: PdfRenderer? = null
    private val mutex = Mutex()
    private val cache = object : LruCache<String, Bitmap>(CACHE_BYTES) {
        override fun sizeOf(key: String, value: Bitmap) = value.byteCount
    }

    override suspend fun open(file: File): Int = withContext(Dispatchers.IO) {
        mutex.withLock {
            close()
            val fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            descriptor = fd
            PdfRenderer(fd).also { renderer = it }.pageCount
        }
    }

    override suspend fun renderPage(index: Int, targetWidth: Int): Bitmap? = withContext(Dispatchers.IO) {
        val width = min(targetWidth, MAX_WIDTH).coerceAtLeast(1)
        val key = "$index@$width"
        cache.get(key)?.let { return@withContext it }
        mutex.withLock {
            val pdf = renderer ?: return@withLock null
            if (index !in 0 until pdf.pageCount) return@withLock null
            pdf.openPage(index).use { page ->
                val height = (width.toFloat() / page.width * page.height).toInt().coerceAtLeast(1)
                Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).apply {
                    eraseColor(Color.WHITE)
                    page.render(this, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                }
            }.also { cache.put(key, it) }
        }
    }

    override fun close() {
        cache.evictAll()
        runCatching { renderer?.close() }
        runCatching { descriptor?.close() }
        renderer = null
        descriptor = null
    }

    private companion object {
        const val MAX_WIDTH = 1440
        const val CACHE_BYTES = 24 * 1024 * 1024
    }
}
