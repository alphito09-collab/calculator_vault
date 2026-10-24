package com.calculator.vault.core.common.dispatchers

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.koin.core.annotation.Single
import timber.log.Timber

/**
 * Process-lifetime scope for work that must outlive a screen (imports, backups, key wiping).
 * Failures are logged instead of crashing the process.
 */
@Single
class ApplicationScope(dispatchers: VaultDispatchers) : CoroutineScope by CoroutineScope(
    SupervisorJob() + dispatchers.default + CoroutineExceptionHandler { _, error ->
        Timber.e(error, "Uncaught error in application scope")
    },
)
