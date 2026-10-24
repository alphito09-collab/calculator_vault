package com.calculator.vault.core.model

data class IntruderAttempt(
    val id: String,
    val timestamp: Long,
    val failedAttempts: Int,
    val hasPhoto: Boolean,
)

data class IntruderStats(
    val failedThisWeek: Int = 0,
    val photosCaptured: Int = 0,
)
