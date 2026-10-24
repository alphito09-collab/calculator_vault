package com.calculator.vault.core.common.session

import android.os.SystemClock
import java.util.concurrent.atomic.AtomicLong

/**
 * The vault re-locks whenever the app leaves the foreground. Launching our own system
 * flows (file pickers, permission settings, share sheet, device admin) also stops the
 * activity, so callers announce those launches here and the next stop is ignored.
 */
object LockGuard {
    private const val GRACE_MS = 2 * 60_000L
    private val suppressedAt = AtomicLong(0L)

    /** Call right before starting an external activity that returns to the vault. */
    fun allowExternalLaunch() {
        suppressedAt.set(SystemClock.elapsedRealtime())
    }

    /** Returns true (once) when the current stop was caused by an announced external launch. */
    fun consumeExternalLaunch(): Boolean {
        val at = suppressedAt.getAndSet(0L)
        return at != 0L && SystemClock.elapsedRealtime() - at < GRACE_MS
    }

    fun clear() = suppressedAt.set(0L)
}
