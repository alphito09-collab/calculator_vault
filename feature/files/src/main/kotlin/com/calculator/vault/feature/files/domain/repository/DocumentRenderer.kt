package com.calculator.vault.feature.files.domain.repository

import android.graphics.Bitmap
import java.io.File

/** Renders pages of a decrypted PDF on demand. One instance per open document. */
interface DocumentRenderer {
    suspend fun open(file: File): Int
    suspend fun renderPage(index: Int, targetWidth: Int): Bitmap?
    fun close()
}
