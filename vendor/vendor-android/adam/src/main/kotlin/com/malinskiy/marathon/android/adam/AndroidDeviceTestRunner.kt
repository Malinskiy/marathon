package com.malinskiy.marathon.android.adam

import com.malinskiy.adam.request.testrunner.InstrumentOptions
import com.malinskiy.adam.request.testrunner.TestAssumptionFailed
import com.malinskiy.adam.request.testrunner.TestEnded
import com.malinskiy.adam.request.testrunner.TestEvent
import com.malinskiy.adam.request.testrunner.TestFailed
import com.malinskiy.adam.request.testrunner.TestIgnored
import com.malinskiy.adam.request.testrunner.TestRunEnded
import com.malinskiy.adam.request.testrunner.TestRunFailed
import com.malinskiy.adam.request.testrunner.TestRunStartedEvent
import com.malinskiy.adam.request.testrunner.TestRunStopped
import com.malinskiy.adam.request.testrunner.TestRunnerRequest
import com.malinskiy.adam.request.testrunner.TestStarted
import com.malinskiy.marathon.android.AndroidConfiguration
import com.malinskiy.marathon.android.AndroidTestBundleIdentifier
import com.malinskiy.marathon.android.InstrumentationInfo
import com.malinskiy.marathon.android.executor.listeners.AndroidTestRunListener
import com.malinskiy.marathon.android.extension.isIgnored
import com.malinskiy.marathon.android.model.TestIdentifier
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.TestBatch
import com.malinskiy.marathon.test.toTestName
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.receiveOrNull
import kotlinx.coroutines.isActive
import kotlinx.coroutines.withTimeoutOrNull
import java.io.IOException

const val ERROR_STUCK = "Test got stuck. You can increase the timeout in settings if it's too strict"

class AndroidDeviceTestRunner(private val device: AdamAndroidDevice, private val bundleIdentifier: AndroidTestBundleIdentifier) {
    private val logger = MarathonLogging.logger("AndroidDeviceTestRunner")

    @OptIn(ExperimentalStdlibApi::class)
    suspend fun execute(
        configuration: Configuration,
        rawTestBatch: TestBatch,
        listener: AndroidTestRunListener
    ) {

        val ignoredTests = rawTestBatch.tests.filter { test -> test.isIgnored() }
        val testBatch = TestBatch(rawTestBatch.tests - ignoredTests, rawTestBatch.id)
        if (testBatch.tests.isEmpty()) {
            notifyIgnoredTest(ignoredTests, listener)
            listener.testRunEnded(0, emptyMap())
            return
        }

        val androidConfiguration = configuration.vendorConfiguration as AndroidConfiguration
        val infoToTestMap: Map<InstrumentationInfo, Test> = testBatch.tests.associateBy {
            bundleIdentifier.identify(it).instrumentationInfo
        }
        infoToTestMap.keys.forEach { info ->
            val runnerRequest = prepareTestRunnerRequest(configuration, androidConfiguration, info, testBatch)
            var channel: ReceiveChannel<List<TestEvent>>? = null
            try {
                withTimeoutOrNull(configuration.testBatchTimeoutMillis) {
                    notifyIgnoredTest(ignoredTests, listener)
                    clearData(androidConfiguration, info)
                    listener.beforeTestRun()
                    logger.debug { "Execution started" }
                    val localChannel = device.executeTestRequest(runnerRequest)
                    channel = localChannel

                    while (!localChannel.isClosedForReceive && isActive) {
                        val update: List<TestEvent>? = withTimeoutOrNull(configuration.testOutputTimeoutMillis) {
                            localChannel.receiveOrNull() ?: emptyList()
                        }
                        if (update == null) {
                            listener.testRunFailed(ERROR_STUCK)
                            return@withTimeoutOrNull
                        } else {
                            processEvents(update, listener)
                        }
                    }

                    logger.debug { "Execution finished" }
                    Unit
                } ?: listener.testRunFailed(ERROR_STUCK)
            } catch (e: IOException) {
                val errorMessage = "adb error while running tests ${testBatch.tests.map { it.toTestName() }}"
                logger.error(e) { errorMessage }
                listener.testRunFailed(errorMessage)
            } finally {
                channel?.cancel(null)
            }
        }
    }

    private suspend fun processEvents(
        events: List<TestEvent>,
        listener: AndroidTestRunListener
    ) {
        for (event in events) {
            when (event) {
                is TestRunStartedEvent -> listener.testRunStarted("AdamTestRun", event.testCount)
                is TestStarted -> listener.testStarted(event.id.toMarathonTestIdentifier())
                is TestFailed -> listener.testFailed(event.id.toMarathonTestIdentifier(), event.stackTrace)
                is TestAssumptionFailed -> listener.testAssumptionFailure(
                    event.id.toMarathonTestIdentifier(),
                    event.stackTrace
                )
                is TestIgnored -> listener.testIgnored(event.id.toMarathonTestIdentifier())
                is TestEnded -> listener.testEnded(event.id.toMarathonTestIdentifier(), event.metrics)
                is TestRunFailed -> listener.testRunFailed(event.error)
                is TestRunStopped -> listener.testRunStopped(event.elapsedTimeMillis)
                is TestRunEnded -> listener.testRunEnded(event.elapsedTimeMillis, event.metrics)
            }
        }
    }

    private suspend fun notifyIgnoredTest(ignoredTests: List<Test>, listeners: AndroidTestRunListener) {
        ignoredTests.forEach {
            val identifier = TestIdentifier("${it.pkg}.${it.clazz}", it.method)
            listeners.testStarted(identifier)
            listeners.testIgnored(identifier)
            listeners.testEnded(identifier, hashMapOf())
        }
    }

    private suspend fun clearData(androidConfiguration: AndroidConfiguration, info: InstrumentationInfo) {
        if (androidConfiguration.applicationPmClear) {
            device.safeClearPackage(info.applicationPackage)?.trim()?.also {
                logger.debug { "Package ${info.applicationPackage} cleared: $it" }
            }
        }
        if (androidConfiguration.testApplicationPmClear) {
            device.safeClearPackage(info.instrumentationPackage)?.trim()?.also {
                logger.debug { "Package ${info.instrumentationPackage} cleared: $it" }
            }
        }
        if (androidConfiguration.fileSyncConfiguration.pull.isNotEmpty()) {
            when {
                device.version.isGreaterOrEqualThan(30) -> {
                    val command = "appops set --uid ${info.applicationPackage} MANAGE_EXTERNAL_STORAGE allow"
                    device.criticalExecuteShellCommand(command).also {
                        logger.debug { "File pull requested. Granted MANAGE_EXTERNAL_STORAGE to ${info.applicationPackage}: ${it.trim()}" }
                    }
                }
                device.version.equals(29) -> {
                    //API 29 doesn't have MANAGE_EXTERNAL_STORAGE, force legacy storage
                    val command = "appops set --uid ${info.applicationPackage} LEGACY_STORAGE allow"
                    device.criticalExecuteShellCommand(command).also {
                        logger.debug { "File pull requested. Granted LEGACY_STORAGE to ${info.applicationPackage}: ${it.trim()}" }
                    }
                }
            }
        }
    }

    private fun prepareTestRunnerRequest(
        configuration: Configuration,
        androidConfiguration: AndroidConfiguration,
        info: InstrumentationInfo,
        testBatch: TestBatch
    ): TestRunnerRequest {
        val tests = testBatch.tests.map {
            if(it.method != "null") {
                "${it.pkg}.${it.clazz}#${it.method.bashEscape()}"
            } else {
                /**
                 * Special case for tests without any methods
                 */
                "${it.pkg}.${it.clazz}"
            }
        }

        logger.debug { "tests = ${tests.toList()}" }

        return TestRunnerRequest(
            testPackage = info.instrumentationPackage,
            runnerClass = info.testRunnerClass,
            noWindowAnimations = true,
            instrumentOptions = InstrumentOptions(
                clazz = tests,
                coverageFile = if (configuration.isCodeCoverageEnabled) "${device.externalStorageMount}/coverage/coverage-${testBatch.id}.ec" else null,
                overrides = androidConfiguration.instrumentationArgs.toMutableMap().apply {
                    if (configuration.isCodeCoverageEnabled) {
                        put("coverage", "true")
                    }
                }
            ),
            socketIdleTimeout = Long.MAX_VALUE
        )
    }
}

private fun String.bashEscape(): String {
    return replace(" ", "\\ ")
}

private fun com.malinskiy.adam.request.testrunner.TestIdentifier.toMarathonTestIdentifier(): TestIdentifier {
    return TestIdentifier(this.className, this.testName)
}
