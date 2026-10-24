package com.calculator.vault.core.media.player

import android.net.Uri
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.BaseDataSource
import androidx.media3.datasource.DataSource
import androidx.media3.datasource.DataSpec
import com.calculator.vault.core.data.storage.VaultFileStore
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.SeekableByteChannel

/** Streams `vault://item/<id>` by decrypting segments on demand, so seeking stays instant. */
@UnstableApi
class EncryptedDataSource(private val store: VaultFileStore) : BaseDataSource(false) {
    private var channel: SeekableByteChannel? = null
    private var uri: Uri? = null
    private var bytesRemaining = 0L

    override fun open(dataSpec: DataSpec): Long {
        uri = dataSpec.uri
        transferInitializing(dataSpec)
        val id = itemId(dataSpec.uri)
        val ch = try {
            store.openContentChannel(id)
        } catch (error: Exception) {
            throw IOException("Cannot open vault item $id", error)
        }
        channel = ch
        ch.position(dataSpec.position)
        val size = ch.size()
        bytesRemaining = if (dataSpec.length != C.LENGTH_UNSET.toLong()) dataSpec.length else size - dataSpec.position
        if (bytesRemaining < 0) throw IOException("Position beyond end of item")
        transferStarted(dataSpec)
        return bytesRemaining
    }

    override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
        if (length == 0) return 0
        if (bytesRemaining == 0L) return C.RESULT_END_OF_INPUT
        val toRead = minOf(length.toLong(), bytesRemaining).toInt()
        val read = channel?.read(ByteBuffer.wrap(buffer, offset, toRead)) ?: return C.RESULT_END_OF_INPUT
        if (read == -1) return C.RESULT_END_OF_INPUT
        bytesRemaining -= read
        bytesTransferred(read)
        return read
    }

    override fun getUri(): Uri? = uri

    override fun close() {
        try {
            channel?.close()
        } finally {
            channel = null
            if (uri != null) {
                uri = null
                transferEnded()
            }
        }
    }

    class Factory(private val store: VaultFileStore) : DataSource.Factory {
        override fun createDataSource(): DataSource = EncryptedDataSource(store)
    }

    companion object {
        const val SCHEME = "vault"

        fun uriFor(itemId: String): Uri = Uri.Builder().scheme(SCHEME).authority("item").appendPath(itemId).build()

        fun itemId(uri: Uri): String = uri.lastPathSegment ?: error("Invalid vault uri $uri")
    }
}
