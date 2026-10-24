package com.calculator.vault.core.common.dispatchers

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/** Injectable dispatchers so blocking work never lands on the main thread and tests can swap them. */
class VaultDispatchers(
    val io: CoroutineDispatcher,
    val default: CoroutineDispatcher,
    val main: CoroutineDispatcher,
) {
    companion object {
        val Default = VaultDispatchers(Dispatchers.IO, Dispatchers.Default, Dispatchers.Main.immediate)

        /** Runs everything on one dispatcher, e.g. a test dispatcher. */
        fun single(dispatcher: CoroutineDispatcher) = VaultDispatchers(dispatcher, dispatcher, dispatcher)
    }
}
