package com.malinskiy.marathon.android.executor.listeners

import com.malinskiy.marathon.android.AndroidDevice
import com.malinskiy.marathon.android.model.TestIdentifier
import com.malinskiy.marathon.log.MarathonLogging

class DebugTestRunListener(private val device: AndroidDevice) : AndroidTestRunListener {

    private val logger = MarathonLogging.logger("DebugTestRunListener")

    override suspend fun beforeTestRun() {
        logger.info { "beforeTestRun ${device.serialNumber}" }
    }

    override suspend fun testRunStarted(runName: String, testCount: Int) {
        logger.info { "testRunStarted ${device.serialNumber}" }
    }

    override suspend fun testStarted(test: TestIdentifier) {
        logger.info { "testStarted ${device.serialNumber} test = $test" }
    }

    override suspend fun testAssumptionFailure(test: TestIdentifier, trace: String) {
        logger.info { "testAssumptionFailure ${device.serialNumber} test = $test trace = $trace" }
    }

    override suspend fun testRunStopped(elapsedTime: Long) {
        logger.info { "testRunStopped ${device.serialNumber} elapsedTime = $elapsedTime" }
    }

    override suspend fun testFailed(test: TestIdentifier, trace: String) {
        logger.info { "testFailed ${device.serialNumber} test = $test trace = $trace" }
    }

    override suspend fun testEnded(test: TestIdentifier, testMetrics: Map<String, String>) {
        logger.info { "testEnded ${device.serialNumber} test = $test" }
    }

    override suspend fun testIgnored(test: TestIdentifier) {
        logger.info { "testIgnored ${device.serialNumber} test = $test" }
    }

    override suspend fun testRunFailed(errorMessage: String) {
        logger.info { "testRunFailed ${device.serialNumber} errorMessage = $errorMessage" }
    }

    override suspend fun testRunEnded(elapsedTime: Long, runMetrics: Map<String, String>) {
        logger.info { "testRunEnded elapsedTime $elapsedTime" }
    }
}
