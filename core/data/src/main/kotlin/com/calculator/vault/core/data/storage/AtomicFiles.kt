package com.calculator.vault.core.data.storage

import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

/**
 * Writes to `<file>.tmp` and renames, so a crash never leaves a half-written file behind.
 *
 * [block] may close the stream it is given (Tink's encrypting streams close their sink); the real
 * file stays open until it has been synced, and is closed exactly once here.
 */
internal inline fun File.writeAtomically(block: (OutputStream) -> Unit) {
    parentFile?.mkdirs()
    val tmp = File(parentFile, "$name.tmp")
    try {
        FileOutputStream(tmp).use { out ->
            block(NonClosingOutputStream(out))
            out.flush()
            out.fd.sync()
        }
        if (exists()) delete()
        check(tmp.renameTo(this)) { "Rename failed for $this" }
    } catch (error: Throwable) {
        tmp.delete()
        throw error
    }
}

/** Forwards writes in bulk (unlike FilterOutputStream) and turns close() into flush(). */
@PublishedApi
internal class NonClosingOutputStream(private val sink: OutputStream) : OutputStream() {
    override fun write(b: Int) = sink.write(b)
    override fun write(b: ByteArray, off: Int, len: Int) = sink.write(b, off, len)
    override fun flush() = sink.flush()
    override fun close() = sink.flush()
}
