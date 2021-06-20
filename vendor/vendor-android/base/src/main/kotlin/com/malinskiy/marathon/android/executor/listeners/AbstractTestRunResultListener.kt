package com.malinskiy.marathon.android.executor.listeners

import com.malinskiy.marathon.android.model.TestIdentifier
import com.malinskiy.marathon.android.model.TestRunResultsAccumulator
import com.malinskiy.marathon.execution.TestBatchResults
import com.malinskiy.marathon.execution.TestBatchResults.RunCompletionReason.RUN_END
import com.malinskiy.marathon.execution.TestBatchResults.RunCompletionReason.RUN_FAILED
import com.malinskiy.marathon.execution.TestBatchResults.RunCompletionReason.RUN_STOPPED
import com.malinskiy.marathon.time.Timer

abstract class AbstractTestRunResultListener(timer: Timer) : NoOpTestRunListener() {
    private val runResult = TestRunResultsAccumulator(timer)

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
        handleTestRunResults(runResult, RUN_FAILED)
    }

    override suspend fun testRunStopped(elapsedTime: Long) {
        runResult.testRunStopped(elapsedTime)
        handleTestRunResults(runResult, RUN_STOPPED)
    }

    override suspend fun testRunEnded(elapsedTime: Long, runMetrics: Map<String, String>) {
        runResult.testRunEnded(elapsedTime, runMetrics)
        handleTestRunResults(runResult, RUN_END)
    }

    abstract suspend fun handleTestRunResults(
        runResult: TestRunResultsAccumulator,
        reason: TestBatchResults.RunCompletionReason
    )
}
