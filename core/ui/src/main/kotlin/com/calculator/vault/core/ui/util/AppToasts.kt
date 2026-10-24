package com.calculator.vault.core.ui.util

import com.calculator.vault.core.designsystem.component.ToastKind
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

data class AppToast(val message: String, val kind: ToastKind = ToastKind.Success)

/**
 * App-wide toast channel for confirmations that outlive the screen that triggered them
 * (e.g. "4 photos hidden" after the importer closes). Rendered once at the app root.
 */
object AppToasts {
    private val _events = MutableSharedFlow<AppToast>(extraBufferCapacity = 8, onBufferOverflow = BufferOverflow.DROP_OLDEST)
    val events: SharedFlow<AppToast> = _events.asSharedFlow()

    fun show(message: String, kind: ToastKind = ToastKind.Success) {
        _events.tryEmit(AppToast(message, kind))
    }
}
