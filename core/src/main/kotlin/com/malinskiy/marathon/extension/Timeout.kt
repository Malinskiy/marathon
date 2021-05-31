package com.malinskiy.marathon.extension

import kotlinx.coroutines.CoroutineScope
import java.time.Duration

suspend fun <T> withTimeoutOrNull(timeout: Duration, block: suspend CoroutineScope.() -> T): T? =
    kotlinx.coroutines.withTimeoutOrNull(timeout.toMillis(), block)

suspend fun <T> withTimeout(timeout: Duration, block: suspend CoroutineScope.() -> T): T =
    kotlinx.coroutines.withTimeout(timeout.toMillis(), block)
