package com.calculator.vault.core.model

data class InstalledApp(
    val packageName: String,
    val label: String,
    val isLocked: Boolean,
)
