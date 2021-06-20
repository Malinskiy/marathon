package com.malinskiy.marathon.execution.progress.tracker

import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.execution.strategy.ExecutionStrategy
import com.malinskiy.marathon.test.Mocks
import com.malinskiy.marathon.test.StubDeviceProvider
import com.malinskiy.marathon.test.TestVendorConfiguration
import org.amshove.kluent.shouldEqualTo
import org.junit.jupiter.api.Test
import java.io.File
import com.malinskiy.marathon.test.Test as MarathonTest

class PoolProgressTrackerTest {
    val test = MarathonTest(
        pkg = "com.malinskiy.marathon",
        clazz = "SomeTest",
        method = "someMethod",
        metaProperties = emptyList()
    )

    private fun createConfiguration(executionStrategy: ExecutionStrategy): Configuration {
        return Configuration(
            name = "",
            outputDir = File(""),
            analyticsConfiguration = null,
            poolingStrategy = null,
            shardingStrategy = null,
            sortingStrategy = null,
            batchingStrategy = null,
            flakinessStrategy = null,
            retryStrategy = null,
            filteringConfiguration = null,
            ignoreFailures = null,
            isCodeCoverageEnabled = null,
            fallbackToScreenshots = null,
            executionStrategy = executionStrategy,
            uncompletedTestRetryQuota = null,
            testClassRegexes = null,
            includeSerialRegexes = null,
            excludeSerialRegexes = null,
            testBatchTimeoutMillis = null,
            testOutputTimeoutMillis = null,
            debug = false,
            screenRecordingPolicy = null,
            vendorConfiguration = TestVendorConfiguration(Mocks.TestParser.DEFAULT, StubDeviceProvider()),
            analyticsTracking = false,
            deviceInitializationTimeoutMillis = null
        )
    }

    @Test
    fun nonStrictMode_case1() {
        val tracker = PoolProgressTracker(createConfiguration(executionStrategy = ExecutionStrategy.ANY_SUCCESS))
        tracker.testStarted(test)
        tracker.testPassed(test)
        tracker.testFailed(test)
        tracker.aggregateResult().shouldEqualTo(true)
        tracker.testPassed(test)
        tracker.aggregateResult().shouldEqualTo(true)
    }

    @Test
    fun strictMode_case1() {
        val tracker = PoolProgressTracker(createConfiguration(executionStrategy = ExecutionStrategy.ALL_SUCCESS))
        tracker.testStarted(test)
        tracker.testPassed(test)
        tracker.testFailed(test)
        tracker.aggregateResult().shouldEqualTo(false)
        tracker.testPassed(test)
        tracker.aggregateResult().shouldEqualTo(false)
    }
}
