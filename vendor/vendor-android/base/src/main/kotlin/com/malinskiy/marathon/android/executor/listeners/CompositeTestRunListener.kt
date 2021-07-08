package com.malinskiy.marathon.android.executor.listeners

import com.malinskiy.marathon.android.model.TestIdentifier

class CompositeTestRunListener(private val listeners: List<AndroidTestRunListener>) : AndroidTestRunListener {
    private inline fun execute(f: (AndroidTestRunListener) -> Unit) {
        listeners.forEach(f)
    }

    override suspend fun beforeTestRun() {
        execute { it.beforeTestRun() }
    }

    override suspend fun testRunStarted(runName: String, testCount: Int) {
        execute { it.testRunStarted(runName, testCount) }
    }

    override suspend fun testStarted(test: TestIdentifier) {
        execute { it.testStarted(test) }
    }

    override suspend fun testAssumptionFailure(test: TestIdentifier, trace: String) {
        execute { it.testAssumptionFailure(test, trace) }
    }

    override suspend fun testRunStopped(elapsedTime: Long) {
        execute { it.testRunStopped(elapsedTime) }
    }

    override suspend fun testFailed(test: TestIdentifier, trace: String) {
        execute { it.testFailed(test, trace) }
    }

    override suspend fun testEnded(test: TestIdentifier, testMetrics: Map<String, String>) {
        execute { it.testEnded(test, testMetrics) }
    }

    override suspend fun testIgnored(test: TestIdentifier) {
        execute { it.testIgnored(test) }
    }

    override suspend fun testRunFailed(errorMessage: String) {
        execute { it.testRunFailed(errorMessage) }
    }

    override suspend fun testRunEnded(elapsedTime: Long, runMetrics: Map<String, String>) {
        execute { it.testRunEnded(elapsedTime, runMetrics) }
    }

    override suspend fun afterTestRun() {
        execute { it.afterTestRun() }
    }
}
