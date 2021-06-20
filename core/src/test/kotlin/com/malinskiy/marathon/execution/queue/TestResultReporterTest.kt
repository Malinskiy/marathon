package com.malinskiy.marathon.execution.queue

import com.malinskiy.marathon.analytics.internal.pub.Track
import com.malinskiy.marathon.createDeviceInfo
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.execution.TestResult
import com.malinskiy.marathon.execution.TestShard
import com.malinskiy.marathon.execution.TestStatus
import com.malinskiy.marathon.execution.strategy.ExecutionStrategy
import com.malinskiy.marathon.generateTest
import com.malinskiy.marathon.test.Mocks
import com.malinskiy.marathon.test.StubDeviceProvider
import com.malinskiy.marathon.test.TestVendorConfiguration
import com.nhaarman.mockitokotlin2.inOrder
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.reset
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

class TestResultReporterTest {
    private val track = mock<Track>()

    @BeforeEach
    fun `setup mocks`() {
        reset(track)
    }

    private val defaultConfig = Configuration(
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
        executionStrategy = null,
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
    private val strictConfig = defaultConfig.copy(executionStrategy = ExecutionStrategy.ALL_SUCCESS)
    val test = generateTest()
    private val poolId = DevicePoolId("test")

    private fun filterDefault() = TestResultReporter(
        poolId,
        TestShard(listOf(test, test, test)),
        defaultConfig,
        track
    )

    private fun filterStrict() = TestResultReporter(
        poolId,
        TestShard(listOf(test, test, test)),
        strictConfig,
        track
    )

    private val deviceInfo = createDeviceInfo()

    @Test
    fun `default config, success - failure - failure, should report success`() {
        val filter = filterDefault()

        val r1 = TestResult(test, deviceInfo, "stub-batch", TestStatus.PASSED, 0, 1)
        val r2 = TestResult(test, deviceInfo, "stub-batch", TestStatus.FAILURE, 2, 3)
        val r3 = TestResult(test, deviceInfo, "stub-batch", TestStatus.FAILURE, 4, 5)

        filter.testFinished(deviceInfo, r1)
        filter.testFailed(deviceInfo, r2)
        filter.testFailed(deviceInfo, r3)

        inOrder(track) {
            verify(track).test(poolId, deviceInfo, r1, true)
            verify(track).test(poolId, deviceInfo, r2, false)
            verify(track).test(poolId, deviceInfo, r3, false)
            verifyNoMoreInteractions(track)
        }
    }

    @Test
    fun `default config, failure - failure - success, should report success`() {
        val filter = filterDefault()

        val r1 = TestResult(test, deviceInfo, "stub-batch", TestStatus.FAILURE, 0, 1)
        val r2 = TestResult(test, deviceInfo, "stub-batch", TestStatus.FAILURE, 2, 3)
        val r3 = TestResult(test, deviceInfo, "stub-batch", TestStatus.PASSED, 4, 5)

        filter.testFailed(deviceInfo, r1)
        filter.testFailed(deviceInfo, r2)
        filter.testFinished(deviceInfo, r3)

        inOrder(track) {
            verify(track).test(poolId, deviceInfo, r1, false)
            verify(track).test(poolId, deviceInfo, r2, false)
            verify(track).test(poolId, deviceInfo, r3, true)
            verifyNoMoreInteractions(track)
        }
    }

    @Test
    fun `strict config, success - failure - failure, should report failure`() {
        val filter = filterStrict()

        val r1 = TestResult(test, deviceInfo, "stub-batch", TestStatus.PASSED, 0, 1)
        val r2 = TestResult(test, deviceInfo, "stub-batch", TestStatus.FAILURE, 2, 3)
        val r3 = TestResult(test, deviceInfo, "stub-batch", TestStatus.FAILURE, 4, 5)

        filter.testFinished(deviceInfo, r1)
        filter.testFailed(deviceInfo, r2)
        filter.testFailed(deviceInfo, r3)

        inOrder(track) {
            verify(track).test(poolId, deviceInfo, r1, false)
            verify(track).test(poolId, deviceInfo, r2, true)
            verify(track).test(poolId, deviceInfo, r3, false)
            verifyNoMoreInteractions(track)
        }
    }

    @Test
    fun `strict config, failure - success - success, should report failure`() {
        val filter = filterStrict()

        val r1 = TestResult(test, deviceInfo, "stub-batch", TestStatus.FAILURE, 0, 1)
        val r2 = TestResult(test, deviceInfo, "stub-batch", TestStatus.PASSED, 2, 3)
        val r3 = TestResult(test, deviceInfo, "stub-batch", TestStatus.PASSED, 4, 5)

        filter.testFailed(deviceInfo, r1)
        filter.testFinished(deviceInfo, r2)
        filter.testFinished(deviceInfo, r3)

        inOrder(track) {
            verify(track).test(poolId, deviceInfo, r1, true)
            verify(track).test(poolId, deviceInfo, r2, false)
            verify(track).test(poolId, deviceInfo, r3, false)
            verifyNoMoreInteractions(track)
        }
    }
}
