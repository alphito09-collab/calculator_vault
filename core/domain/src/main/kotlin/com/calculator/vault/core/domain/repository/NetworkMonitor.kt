package com.calculator.vault.core.domain.repository

import kotlinx.coroutines.flow.Flow

interface NetworkMonitor {
    val isOnline: Flow<Boolean>
    val isOnUnmeteredNetwork: Flow<Boolean>
}
