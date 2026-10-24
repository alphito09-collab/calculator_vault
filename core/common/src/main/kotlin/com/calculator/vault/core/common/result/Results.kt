package com.calculator.vault.core.common.result

import kotlinx.coroutines.CancellationException

/** Like [runCatching] but never swallows coroutine cancellation. */
inline fun <T> runSuspendCatching(block: () -> T): Result<T> = try {
    Result.success(block())
} catch (cancellation: CancellationException) {
    throw cancellation
} catch (error: Throwable) {
    Result.failure(error)
}
