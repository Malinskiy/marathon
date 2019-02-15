package com.malinskiy.marathon.report.debug.timeline

import com.malinskiy.marathon.analytics.tracker.local.RawTestResultTracker
import com.malinskiy.marathon.execution.TestStatus
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.report.PoolSummary
import com.malinskiy.marathon.report.Summary
import com.malinskiy.marathon.test.Test

class TimelineSummarySerializer(private val rawTestResultTracker: RawTestResultTracker) {
    val logger = MarathonLogging.logger(TimelineSummarySerializer::class.java.simpleName)

    private fun parseData(runs: List<RawTestResultTracker.RawTestRun>): List<Data> {
        return runs.map { this.convertToData(it) }
                .sortedBy { it.startDate }
    }

    private fun convertToData(testResult: RawTestResultTracker.RawTestRun): Data {
        val preparedTestName = "${testResult.clazz}.${testResult.method}"
        val testMetric = getTestMetric(testResult)
        return createData(testResult, testResult.status, preparedTestName, testMetric)
    }

    data class TestMetric(val expectedValue: Double, val variance: Double)

    private fun createData(execution: RawTestResultTracker.RawTestRun, status: TestStatus, preparedTestName: String, testMetric: TestMetric): Data {
        return Data(preparedTestName,
                status.toStatus(),
                execution.timestamp,
                execution.timestamp + execution.duration,
                testMetric.expectedValue, testMetric.variance)
    }

    private fun getTestMetric(execution: RawTestResultTracker.RawTestRun): TestMetric {
        return TestMetric(0.0, 0.0)
    }

    private fun calculateExecutionStats(data: List<Data>): ExecutionStats {
        return ExecutionStats(calculateIdle(data), calculateAverageExecutionTime(data))
    }

    private fun calculateAverageExecutionTime(data: List<Data>): Long {
        return data.map { this.calculateDuration(it) }.average().toLong()
    }

    private fun calculateDuration(a: Data): Long {
        return a.endDate - a.startDate
    }

    private fun calculateIdle(data: List<Data>): Long {
        return data.windowed(2, 1).fold(0L, { acc, list ->
            acc + (list[1].startDate - list[0].endDate)
        })
    }

    private fun extractIdentifiers(summary: PoolSummary): List<Test> {
        return summary.tests
                .filter { it.status == TestStatus.PASSED }
                .map { result -> result.test }
    }

    private fun passedTestCount(summary: Summary): Int {
        return summary.pools
                .flatMap { this.extractIdentifiers(it) }
                .distinct()
                .count()
    }

    private fun aggregateExecutionStats(list: List<Measure>): ExecutionStats {
        val summaryIdle = list
                .map { it.executionStats.idleTimeMillis }
                .sum()
        val avgTestExecutionTime = list
                .map { it.executionStats.averageTestExecutionTimeMillis }
                .average()
                .toLong()
        return ExecutionStats(summaryIdle, avgTestExecutionTime)
    }

    fun parse(summary: Summary): ExecutionResult {
        val passedTestCount = rawTestResultTracker.testResults.count { it.success }
        val failedTests = rawTestResultTracker.testResults.count { !it.success }
        val ignoredTests = rawTestResultTracker.testResults.count { it.ignored }

        val measures = rawTestResultTracker.testResults.groupBy { it.deviceSerial }
                .map {
                    val data = parseData(it.value)
                    Measure(it.key, calculateExecutionStats(data), data)
                }

        val executionStats = aggregateExecutionStats(measures)
        return ExecutionResult(passedTestCount, failedTests, ignoredTests, executionStats, measures)
    }

    private fun TestStatus.toStatus() = when(this) {
        TestStatus.FAILURE -> Status.FAILURE
        TestStatus.PASSED -> Status.PASSED
        TestStatus.IGNORED -> Status.IGNORED
        TestStatus.INCOMPLETE -> Status.INCOMPLETE
        TestStatus.ASSUMPTION_FAILURE -> Status.ASSUMPTION_FAILURE
    }
}
