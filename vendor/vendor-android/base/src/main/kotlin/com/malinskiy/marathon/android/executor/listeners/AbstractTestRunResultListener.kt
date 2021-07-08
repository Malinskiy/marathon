package com.malinskiy.marathon.android.executor.listeners

import com.malinskiy.marathon.android.model.TestIdentifier
import com.malinskiy.marathon.android.model.TestRunResultsAccumulator
import com.malinskiy.marathon.time.Timer

abstract class AbstractTestRunResultListener(timer: Timer) : NoOpTestRunListener() {
    protected val runResult = TestRunResultsAccumulator(timer)

    override suspend fun testRunStarted(runName: String, testCount: Int) {
        runResult.testRunStarted(runName, testCount)
    }

    override suspend fun testStarted(test: TestIdentifier) {
        runResult.testStarted(test)
    }

    override suspend fun testFailed(test: TestIdentifier, trace: String) {
        runResult.testFailed(test, trace)
    }

    override suspend fun testAssumptionFailure(test: TestIdentifier, trace: String) {
        runResult.testAssumptionFailure(test, trace)
    }

    override suspend fun testIgnored(test: TestIdentifier) {
        runResult.testIgnored(test)
    }

    override suspend fun testEnded(test: TestIdentifier, testMetrics: Map<String, String>) {
        runResult.testEnded(test, testMetrics)
    }

    override suspend fun testRunFailed(errorMessage: String) {
        runResult.testRunFailed(errorMessage)
    }

    override suspend fun testRunStopped(elapsedTime: Long) {
        runResult.testRunStopped(elapsedTime)
    }

    override suspend fun testRunEnded(elapsedTime: Long, runMetrics: Map<String, String>) {
        runResult.testRunEnded(elapsedTime, runMetrics)
    }
}
