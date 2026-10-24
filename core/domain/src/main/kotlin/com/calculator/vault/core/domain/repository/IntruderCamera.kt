package com.calculator.vault.core.domain.repository

/** Silently captures a front-camera JPEG when someone fails to unlock the vault. */
interface IntruderCamera {
    fun hasPermission(): Boolean
    suspend fun captureSelfie(): ByteArray?
}
