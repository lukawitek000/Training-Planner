package com.lukasz.witkowski.training.planner.shared.utils

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.coroutines.cancellation.CancellationException

@OptIn(ExperimentalContracts::class)
inline fun <T> runCatchingCancellable(block: () -> T): Result<T> {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }
    return try {
        Result.success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: InterruptedException) {
        throw e
    } catch (e: Exception) {
        Result.failure(e)
    }
}

@OptIn(ExperimentalContracts::class)
inline fun <T, E> runCatchingCancellable(
    mapError: (Throwable) -> E,
    block: () -> T,
): AppResult<T, E> {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }
    return try {
        AppResult.Success(block())
    } catch (e: CancellationException) {
        throw e
    } catch (e: InterruptedException) {
        throw e
    } catch (e: Throwable) {
        AppResult.Error(mapError(e))
    }
}
