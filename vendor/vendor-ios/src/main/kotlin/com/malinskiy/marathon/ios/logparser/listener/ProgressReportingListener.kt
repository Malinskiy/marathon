package com.malinskiy.marathon.ios.logparser.listener

import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.toDeviceInfo
import com.malinskiy.marathon.execution.TestBatchResults
import com.malinskiy.marathon.execution.TestBatchResults.RunCompletionReason.RUN_END
import com.malinskiy.marathon.execution.TestResult
import com.malinskiy.marathon.execution.TestStatus
import com.malinskiy.marathon.execution.progress.ProgressReporter
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.TestBatch
import com.malinskiy.marathon.test.toSafeTestName
import com.malinskiy.marathon.time.Timer
import kotlinx.coroutines.CompletableDeferred

class ProgressReportingListener(
    private val device: Device,
    private val poolId: DevicePoolId,
    private val testBatch: TestBatch,
    private val deferredResults: CompletableDeferred<TestBatchResults>,
    private val progressReporter: ProgressReporter,
    private val testLogListener: TestLogListener,
    private val timer: Timer
) : TestRunListener {

    private val success: MutableList<TestResult> = mutableListOf()
    private val failure: MutableList<TestResult> = mutableListOf()

    override fun batchFinished() {
        val received = (success + failure)
        val receivedTestNames = received.map { it.test.toSafeTestName() }.toHashSet()

        val uncompleted = testBatch.tests.filter {
            !receivedTestNames.contains(it.toSafeTestName())
        }.createUncompletedTestResults(received)

        deferredResults.complete(TestBatchResults(device, success, failure, uncompleted, RUN_END))
    }

    private fun List<Test>.createUncompletedTestResults(received: Collection<TestResult>): Collection<TestResult> {
        val lastCompletedTestEndTime = received.maxBy { it.endTime }?.endTime ?: timer.currentTimeMillis()
        return map {
            TestResult(
                it,
                device.toDeviceInfo(),
                testBatchId = testBatch.id,
                TestStatus.FAILURE,
                lastCompletedTestEndTime,
                lastCompletedTestEndTime,
                testLogListener.getLastLog()
            )
        }
    }

    override fun testFailed(test: Test, startTime: Long, endTime: Long) {
        progressReporter.testFailed(poolId, device.toDeviceInfo(), test)
        failure.add(
            TestResult(
                test,
                device.toDeviceInfo(),
                testBatch.id,
                TestStatus.FAILURE,
                startTime,
                endTime,
                testLogListener.getLastLog()
            )
        )
    }

    override fun testPassed(test: Test, startTime: Long, endTime: Long) {
        progressReporter.testPassed(poolId, device.toDeviceInfo(), test)
        success.add(
            TestResult(
                test,
                device.toDeviceInfo(),
                testBatch.id,
                TestStatus.PASSED,
                startTime,
                endTime,
                testLogListener.getLastLog()
            )
        )
    }

    override fun testStarted(test: Test) {
        progressReporter.testStarted(poolId, device.toDeviceInfo(), test)
    }
}
