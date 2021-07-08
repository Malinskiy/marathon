package com.malinskiy.marathon.android.executor.listeners

import com.malinskiy.marathon.android.model.TestIdentifier

interface AndroidTestRunListener {
    suspend fun beforeTestRun() {}

    suspend fun testRunStarted(runName: String, testCount: Int) {}

    suspend fun testStarted(test: TestIdentifier) {}

    suspend fun testFailed(test: TestIdentifier, trace: String) {}

    suspend fun testAssumptionFailure(test: TestIdentifier, trace: String) {}

    suspend fun testIgnored(test: TestIdentifier) {}

    suspend fun testEnded(test: TestIdentifier, testMetrics: Map<String, String>) {}

    suspend fun testRunFailed(errorMessage: String) {}

    suspend fun testRunStopped(elapsedTime: Long) {}

    suspend fun testRunEnded(elapsedTime: Long, runMetrics: Map<String, String>) {}

    suspend fun afterTestRun() {}
}
