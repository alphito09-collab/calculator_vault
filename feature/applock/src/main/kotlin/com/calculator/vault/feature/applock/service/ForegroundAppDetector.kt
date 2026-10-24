package com.calculator.vault.feature.applock.service

import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.os.Build

/** Reads the most recent app that came to the foreground from usage events. */
internal class ForegroundAppDetector(private val usage: UsageStatsManager) {
    private var lastQuery = System.currentTimeMillis() - LOOKBACK_MS
    private var current: String? = null

    fun poll(): String? {
        val now = System.currentTimeMillis()
        val events = usage.queryEvents(lastQuery, now)
        lastQuery = now - OVERLAP_MS
        val event = UsageEvents.Event()
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            val resumed = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                event.eventType == UsageEvents.Event.ACTIVITY_RESUMED
            } else {
                @Suppress("DEPRECATION")
                event.eventType == UsageEvents.Event.MOVE_TO_FOREGROUND
            }
            if (resumed) current = event.packageName
        }
        return current
    }

    private companion object {
        const val LOOKBACK_MS = 10_000L
        const val OVERLAP_MS = 500L
    }
}
