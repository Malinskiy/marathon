package com.malinskiy.marathon.android.executor.listeners

import com.malinskiy.marathon.android.model.TestIdentifier

open class NoOpTestRunListener : AndroidTestRunListener {
    override suspend fun beforeTestRun() {}

    override suspend fun testRunStarted(runName: String, testCount: Int) {}

    override suspend fun testStarted(test: TestIdentifier) {}

    override suspend fun testFailed(test: TestIdentifier, trace: String) {}

    override suspend fun testAssumptionFailure(test: TestIdentifier, trace: String) {}

    override suspend fun testIgnored(test: TestIdentifier) {}

    override suspend fun testEnded(test: TestIdentifier, testMetrics: Map<String, String>) {}

    override suspend fun testRunFailed(errorMessage: String) {}

    override suspend fun testRunStopped(elapsedTime: Long) {}

    override suspend fun testRunEnded(elapsedTime: Long, runMetrics: Map<String, String>) {}
}
