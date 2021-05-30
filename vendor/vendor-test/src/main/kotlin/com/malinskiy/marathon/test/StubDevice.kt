package com.malinskiy.marathon.test

import com.malinskiy.marathon.device.Device
import com.malinskiy.marathon.device.DeviceFeature
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.NetworkState
import com.malinskiy.marathon.device.OperatingSystem
import com.malinskiy.marathon.device.toDeviceInfo
import com.malinskiy.marathon.exceptions.TestBatchExecutionException
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.execution.TestBatchResults
import com.malinskiy.marathon.execution.TestBatchResults.RunCompletionReason
import com.malinskiy.marathon.execution.TestResult
import com.malinskiy.marathon.execution.TestStatus
import com.malinskiy.marathon.execution.progress.ProgressReporter
import com.malinskiy.marathon.log.MarathonLogging
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.delay

class StubDevice(
    private val prepareTimeMillis: Long = 5000L,
    private val testTimeMillis: Long = 5000L,
    override val operatingSystem: OperatingSystem = OperatingSystem("25"),
    override val model: String = "test",
    override val manufacturer: String = "test",
    override val networkState: NetworkState = NetworkState.CONNECTED,
    override val deviceFeatures: Collection<DeviceFeature> = listOf(),
    override val abi: String = "test",
    override val serialNumber: String = "serial-1",
    override val healthy: Boolean = true,
    val crashWithTestBatchException: Boolean = false
) : Device {

    val logger = MarathonLogging.logger(StubDevice::class.java.simpleName)

    lateinit var executionResults: Map<Test, Array<TestStatus>>
    var executionIndexMap: MutableMap<Test, Int> = mutableMapOf()
    var timeCounter: Long = 0

    override suspend fun execute(
        configuration: Configuration,
        devicePoolId: DevicePoolId,
        testBatch: TestBatch,
        deferred: CompletableDeferred<TestBatchResults>,
        progressReporter: ProgressReporter
    ) {
        delay(testTimeMillis)

        if (crashWithTestBatchException) {
            throw TestBatchExecutionException("user requested the device to crash via crashWithTestBatchException parameter")
        }

        val results = testBatch.tests.map {
            val i = executionIndexMap.getOrDefault(it, 0)
            val result = executionResults[it]!![i]
            executionIndexMap[it] = i + 1
            val testResult = TestResult(it, toDeviceInfo(), testBatch.id, result, timeCounter, timeCounter + 1, null)
            timeCounter += 1
            testResult
        }

        deferred.complete(
            TestBatchResults(
                this,
                results.filter { it.status == TestStatus.PASSED },
                results.filter { it.status == TestStatus.FAILURE },
                results.filter { it.status == TestStatus.INCOMPLETE },
                RunCompletionReason.RUN_END
            )
        )
    }

    override suspend fun prepare(configuration: Configuration) {
        logger.debug { "Preparing" }
        delay(prepareTimeMillis)
    }

    override fun dispose() {
        logger.debug { "Disposing" }
    }
}
