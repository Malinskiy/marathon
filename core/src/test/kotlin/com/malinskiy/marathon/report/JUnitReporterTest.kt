package com.malinskiy.marathon.report

import com.malinskiy.marathon.analytics.internal.sub.DeviceConnectedEvent
import com.malinskiy.marathon.analytics.internal.sub.ExecutionReport
import com.malinskiy.marathon.analytics.internal.sub.TestEvent
import com.malinskiy.marathon.device.DeviceFeature
import com.malinskiy.marathon.device.DeviceInfo
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.device.NetworkState
import com.malinskiy.marathon.device.OperatingSystem
import com.malinskiy.marathon.execution.AnalyticsConfiguration
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.execution.TestParser
import com.malinskiy.marathon.execution.TestResult
import com.malinskiy.marathon.execution.TestStatus
import com.malinskiy.marathon.log.MarathonLogConfigurator
import com.malinskiy.marathon.report.junit.JUnitReporter
import com.malinskiy.marathon.report.junit.JUnitWriter
import com.malinskiy.marathon.test.assert.shouldBeEqualToAsXML
import com.malinskiy.marathon.vendor.VendorConfiguration
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Files
import java.time.Instant
import com.malinskiy.marathon.test.Test as MarathonTest

class JUnitReporterTest {

    @Test
    fun `only Passed Tests and without retries should generate correct report`() {
        val device = getDevice()
        val report = ExecutionReport(
            deviceProviderPreparingEvent = emptyList(),
            devicePreparingEvents = emptyList(),
            deviceConnectedEvents = listOf(
                DeviceConnectedEvent(Instant.now(), DevicePoolId("myPool"), device)
            ),
            testEvents = listOf(
                createTestEvent(device, "test1", TestStatus.PASSED),
                createTestEvent(device, "test2", TestStatus.PASSED),
                createTestEvent(device, "test3", TestStatus.PASSED)
            )
        )
        val configuration = getConfiguration()
        println(configuration.outputDir)
        val jUnitWriter = JUnitWriter(configuration.outputDir)
        val junitReport = JUnitReporter(jUnitWriter)
        junitReport.generate(report)
        File(configuration.outputDir.absolutePath + "/tests/myPool/xxyyzz/marathon_junit_report.xml")
            .shouldBeEqualToAsXML(File(javaClass.getResource("/output/tests/myPool/xxyyzz/marathon_junit_report_passed_tests.xml").file))
    }

    @Test
    fun `only Failed Tests and without retries should generate correct report`() {
        val device = getDevice()
        val report = ExecutionReport(
            deviceProviderPreparingEvent = emptyList(),
            devicePreparingEvents = emptyList(),
            deviceConnectedEvents = listOf(
                DeviceConnectedEvent(Instant.now(), DevicePoolId("myPool"), device)
            ),
            testEvents = listOf(
                createTestEvent(device, "test1", TestStatus.FAILURE),
                createTestEvent(device, "test2", TestStatus.INCOMPLETE),
                createTestEvent(device, "test3", TestStatus.FAILURE)
            )
        )
        val configuration = getConfiguration()
        println(configuration.outputDir)
        val jUnitWriter = JUnitWriter(configuration.outputDir)
        val junitReport = JUnitReporter(jUnitWriter)
        junitReport.generate(report)
        File(configuration.outputDir.absolutePath + "/tests/myPool/xxyyzz/marathon_junit_report.xml")
            .shouldBeEqualToAsXML(File(javaClass.getResource("/output/tests/myPool/xxyyzz/marathon_junit_report_failed_tests.xml").file))
    }

    @Test
    fun `with FAILURE, IGNORED, INCOMPLETE and ASSUMPTION_FAILURE Tests with stack trace should generate correct report`() {
        val device = getDevice()
        val stackTrace = "Exception in thread \"main\" java.lang.RuntimeException: A test exception\n" +
            "  at com.example.stacktrace.StackTraceExample.methodB(StackTraceExample.java:13)\n" +
            "  at com.example.stacktrace.StackTraceExample.methodA(StackTraceExample.java:9)\n" +
            "  at com.example.stacktrace.StackTraceExample.main(StackTraceExample.java:5)"
        val report = ExecutionReport(
            deviceProviderPreparingEvent = emptyList(),
            devicePreparingEvents = emptyList(),
            deviceConnectedEvents = listOf(
                DeviceConnectedEvent(Instant.now(), DevicePoolId("myPool"), device)
            ),
            testEvents = listOf(
                createTestEvent(device, "test1", TestStatus.FAILURE, stackTrace),
                createTestEvent(device, "test2", TestStatus.IGNORED),
                createTestEvent(device, "test2", TestStatus.INCOMPLETE),
                createTestEvent(device, "test3", TestStatus.ASSUMPTION_FAILURE, stackTrace)
            )
        )
        val configuration = getConfiguration()
        println(configuration.outputDir)
        val jUnitWriter = JUnitWriter(configuration.outputDir)
        val junitReport = JUnitReporter(jUnitWriter)
        junitReport.generate(report)
        File(configuration.outputDir.absolutePath + "/tests/myPool/xxyyzz/marathon_junit_report.xml")
            .shouldBeEqualToAsXML(File(javaClass.getResource("/output/tests/myPool/xxyyzz/marathon_junit_report_failed_tests_with_stacktrace.xml").file))
    }

    @Test
    fun `with multiple execution of the same test from FAILURE to PASSED should generate correct report`() {
        val device = getDevice()
        val stackTrace = "Exception in thread \"main\" java.lang.RuntimeException: A test exception\n" +
            "  at com.example.stacktrace.StackTraceExample.methodB(StackTraceExample.java:13)\n" +
            "  at com.example.stacktrace.StackTraceExample.methodA(StackTraceExample.java:9)\n" +
            "  at com.example.stacktrace.StackTraceExample.main(StackTraceExample.java:5)"
        val report = ExecutionReport(
            deviceProviderPreparingEvent = emptyList(),
            devicePreparingEvents = emptyList(),
            deviceConnectedEvents = listOf(
                DeviceConnectedEvent(Instant.now(), DevicePoolId("myPool"), device)
            ),
            testEvents = listOf(
                createTestEvent(device, "test1", TestStatus.FAILURE, stackTrace, false),
                createTestEvent(device, "test1", TestStatus.PASSED, final = true)
            )
        )
        val configuration = getConfiguration()
        println(configuration.outputDir)
        val jUnitWriter = JUnitWriter(configuration.outputDir)
        val junitReport = JUnitReporter(jUnitWriter)
        junitReport.generate(report)
        File(configuration.outputDir.absolutePath + "/tests/myPool/xxyyzz/marathon_junit_report.xml")
            .shouldBeEqualToAsXML(File(javaClass.getResource("/output/tests/myPool/xxyyzz/marathon_junit_report_failed_to_passed_test.xml").file))
    }

    @Test
    fun `with multiple execution of the same test from PASSED to FAILURE should generate correct report`() {
        val device = getDevice()
        val stackTrace = "Exception in thread \"main\" java.lang.RuntimeException: A test exception\n" +
            "  at com.example.stacktrace.StackTraceExample.methodB(StackTraceExample.java:13)\n" +
            "  at com.example.stacktrace.StackTraceExample.methodA(StackTraceExample.java:9)\n" +
            "  at com.example.stacktrace.StackTraceExample.main(StackTraceExample.java:5)"
        val report = ExecutionReport(
            deviceProviderPreparingEvent = emptyList(),
            devicePreparingEvents = emptyList(),
            deviceConnectedEvents = listOf(
                DeviceConnectedEvent(Instant.now(), DevicePoolId("myPool"), device)
            ),
            testEvents = listOf(
                createTestEvent(device, "test1", TestStatus.PASSED, final = false),
                createTestEvent(device, "test1", TestStatus.FAILURE, stackTrace)
            )
        )
        val configuration = getConfiguration()
        println(configuration.outputDir)
        val jUnitWriter = JUnitWriter(configuration.outputDir)
        val junitReport = JUnitReporter(jUnitWriter)
        junitReport.generate(report)
        File(configuration.outputDir.absolutePath + "/tests/myPool/xxyyzz/marathon_junit_report.xml")
            .shouldBeEqualToAsXML(File(javaClass.getResource("/output/tests/myPool/xxyyzz/marathon_junit_report_passed_to_failed_test.xml").file))
    }
}

fun createTestEvent(
    deviceInfo: DeviceInfo,
    methodName: String,
    status: TestStatus,
    stackTrace: String? = null,
    final: Boolean = true
): TestEvent {
    return TestEvent(
        Instant.now(),
        DevicePoolId("myPool"),
        deviceInfo,
        TestResult(
            MarathonTest("com", "example", methodName, emptyList()),
            deviceInfo,
            "stub-batch",
            status,
            1541675929849,
            1541675941768,
            stackTrace
        ),
        final
    )
}

fun getConfiguration() =
    Configuration(
        name = "",
        outputDir = Files.createTempDirectory("test-run").toFile(),
        analyticsConfiguration = AnalyticsConfiguration.DisabledAnalytics,
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
        debug = null,
        screenRecordingPolicy = null,
        vendorConfiguration = object : VendorConfiguration {
            override fun testParser(): TestParser? = null
            override fun deviceProvider(): DeviceProvider? = null
            override fun logConfigurator(): MarathonLogConfigurator? = null
        },
        analyticsTracking = false,
        deviceInitializationTimeoutMillis = null
    )

fun getDevice() =
    DeviceInfo(
        operatingSystem = OperatingSystem("23"),
        serialNumber = "xxyyzz",
        model = "Android SDK built for x86",
        manufacturer = "unknown",
        networkState = NetworkState.CONNECTED,
        deviceFeatures = listOf(DeviceFeature.SCREENSHOT, DeviceFeature.VIDEO),
        healthy = true
    )
