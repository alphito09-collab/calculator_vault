package com.calculator.vault.feature.applock.service

import android.content.Context
import com.calculator.vault.core.common.dispatchers.ApplicationScope
import com.calculator.vault.core.domain.repository.AppLockRepository
import com.calculator.vault.feature.applock.data.AppLockSystemRepositoryImpl
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.koin.core.annotation.Single

/** Keeps [AppLockService] running exactly while App Lock is on, has apps and has usage access. */
@Single
class AppLockController(
    private val context: Context,
    private val repository: AppLockRepository,
    private val scope: ApplicationScope,
) {
    fun start() {
        scope.launch {
            repository.settings
                .map { it.enabled && it.lockedPackages.isNotEmpty() }
                .distinctUntilChanged()
                .collect { sync(it) }
        }
    }

    /** Call again after the user grants usage access. */
    fun refresh() {
        scope.launch {
            val s = repository.settings.first()
            sync(s.enabled && s.lockedPackages.isNotEmpty())
        }
    }

    private fun sync(shouldRun: Boolean) {
        if (shouldRun && AppLockSystemRepositoryImpl.hasUsageAccess(context)) AppLockService.start(context) else AppLockService.stop(context)
    }
}
