package com.malinskiy.marathon.report.junit

import com.malinskiy.marathon.analytics.internal.sub.ExecutionReport
import com.malinskiy.marathon.analytics.internal.sub.PoolSummary
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.TestResult
import com.malinskiy.marathon.execution.TestStatus
import com.malinskiy.marathon.io.FileType
import com.malinskiy.marathon.report.Reporter
import com.malinskiy.marathon.report.junit.model.Failure
import com.malinskiy.marathon.report.junit.model.JUnitReport
import com.malinskiy.marathon.report.junit.model.Rerun
import com.malinskiy.marathon.report.junit.model.Skipped
import com.malinskiy.marathon.report.junit.model.TestCase
import com.malinskiy.marathon.report.junit.model.TestSuite
import com.malinskiy.marathon.report.junit.serialize.JUnitReportSerializer
import java.io.File
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.*

private val FORMATTER = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
    timeZone = TimeZone.getTimeZone("UTC")
}

internal class JUnitReporter(private val outputDir: File) : Reporter {
    private val reportName = "marathon_junit_report"
    private val serializer: JUnitReportSerializer = JUnitReportSerializer()

    override fun generate(executionReport: ExecutionReport) {
        val junitReports = hashMapOf<DevicePoolId, JUnitReport>()
        executionReport.summary.pools.forEach { poolSummary ->
            val poolId = poolSummary.poolId
            val results = poolSummary.tests
            if (!results.isNullOrEmpty()) {
                val testSuite = TestSuite(
                    name = poolId.name,
                    tests = results.size,
                    failures = poolSummary.failed.size,
                    errors = 0,
                    skipped = poolSummary.ignored.size,
                    time = results.map { it.durationMillis() }.sum().toJUnitSeconds(),
                    timestamp = FORMATTER.format(results.maxBy { it.endTime }?.endTime),
                    testcase = createTestCases(poolSummary),
                )

                junitReports[poolId] = JUnitReport(listOf(testSuite))
            }
        }

        executionReport.summary.pools.map { it.poolId }.distinct()
            .forEach {
                val junitReport = junitReports[it]
                if (junitReport != null) {
                    val reportDirectory = Paths.get(
                        outputDir.absolutePath,
                        FileType.TEST.dir,
                        it.name
                    ).toFile()
                    reportDirectory.mkdirs()
                    val file = File(reportDirectory.absolutePath, reportName + "." + FileType.TEST.suffix)
                    file.createNewFile()

                    serializer.serialize(junitReport, file)
                }
            }
    }

    private fun createTestCases(poolSummary: PoolSummary): List<TestCase> {
        val result = mutableListOf<TestCase>()

        poolSummary.tests.forEach {
            val reruns = poolSummary.retries[it]
                ?.filter { event ->
                    if (event.testResult.status == TestStatus.INCOMPLETE) return@filter false

                    return@filter if (isFailure(it)) {
                        /**
                         * If final result is failure we should filter out success cases
                         * This happens when using strict mode and the test has to pass all retries or it will fail
                         */
                        !event.testResult.isSuccess
                    } else true
                }?.map { testEvent ->
                    Rerun(stackTrace = testEvent.testResult.stacktrace)
                } ?: emptyList()

            result.add(
                TestCase(
                    classname = it.test.pkg + "." + it.test.clazz,
                    name = it.test.method,
                    time = it.durationMillis().toJUnitSeconds(),
                    skipped = if (isSkipped(it)) Skipped(description = it.stacktrace ?: "") else null,
                    failure = if (isFailure(it)) Failure(description = it.stacktrace ?: "") else null,
                    flakyFailure = if (!isSkipped(it) && !isFailure(it)) reruns else emptyList(),
                    rerunFailure = if (isFailure(it)) reruns else emptyList(),
                )
            )
        }

        return result
    }

    private fun isSkipped(testResult: TestResult) =
        testResult.status == TestStatus.IGNORED || testResult.status == TestStatus.ASSUMPTION_FAILURE

    private fun isFailure(testResult: TestResult) =
        testResult.status == TestStatus.FAILURE || testResult.status == TestStatus.INCOMPLETE

    @Suppress("MagicNumber")
    private fun Long.toJUnitSeconds(): String = (this / 1000.0).toString()
}
