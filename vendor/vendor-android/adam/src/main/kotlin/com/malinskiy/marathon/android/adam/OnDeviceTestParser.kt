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
import com.malinskiy.marathon.analytics.internal.pub.Track
import com.malinskiy.marathon.android.AndroidAppInstaller
import com.malinskiy.marathon.android.AndroidConfiguration
import com.malinskiy.marathon.android.AndroidTestBundleIdentifier
import com.malinskiy.marathon.android.model.AndroidTestBundle
import com.malinskiy.marathon.android.model.TestIdentifier
import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.execution.TestParser
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.time.SystemTimer
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.NonCancellable.isActive
import kotlinx.coroutines.withTimeoutOrNull
import java.time.Clock

class OnDeviceTestParser(private val testBundleIdentifier: AndroidTestBundleIdentifier) : TestParser {
    private val logger = MarathonLogging.logger {}

    override suspend fun extract(configuration: Configuration): List<Test> {
        val vendorConfiguration = configuration.vendorConfiguration as AndroidConfiguration
        val testBundles = vendorConfiguration.testBundlesCompat()

        val provider = AdamDeviceProvider(configuration, vendorConfiguration, Track(), SystemTimer(Clock.systemDefaultZone()))
        provider.initialize(vendorConfiguration)
        val channel = provider.subscribe()

        try {
            for (update in channel) {
                if (update is DeviceProvider.DeviceEvent.DeviceConnected) {
                    val device = update.device as AdamAndroidDevice
                    return parseTests(device, configuration, vendorConfiguration, testBundles)
                }
            }
            throw RuntimeException("failed to parse")
        } finally {
            channel.close()
            provider.terminate()
        }
    }

    @OptIn(InternalCoroutinesApi::class)
    private suspend fun parseTests(
        device: AdamAndroidDevice,
        configuration: Configuration,
        vendorConfiguration: AndroidConfiguration,
        testBundles: List<AndroidTestBundle>
    ): List<Test> {
        return testBundles.flatMap { bundle ->
            val androidTestBundle = AndroidTestBundle(bundle.application, bundle.testApplication)
            val instrumentationInfo = androidTestBundle.instrumentationInfo

            val runnerRequest = TestRunnerRequest(
                testPackage = instrumentationInfo.instrumentationPackage,
                runnerClass = instrumentationInfo.testRunnerClass,
                instrumentOptions = InstrumentOptions(
                    log = true
                ),
            )
            val androidAppInstaller = AndroidAppInstaller(configuration)
            androidAppInstaller.prepareInstallation(device)
            val channel = device.executeTestRequest(runnerRequest)

            val tests = mutableListOf<Test>()
            while (!channel.isClosedForReceive && isActive) {
                val events: List<TestEvent>? = withTimeoutOrNull(configuration.testOutputTimeoutMillis) {
                    channel.receiveOrNull() ?: emptyList()
                }
                if (events == null) {
                    throw RuntimeException("Unable to parse test list using ${device.serialNumber}")
                } else {
                    for (event in events) {
                        when (event) {
                            is TestRunStartedEvent -> Unit
                            is TestStarted -> {
                                val test = TestIdentifier(event.id.className, event.id.testName).toTest()
                                tests.add(test)
                                testBundleIdentifier.put(test, androidTestBundle)
                            }
                            is TestFailed -> Unit
                            is TestAssumptionFailed -> Unit
                            is TestIgnored -> Unit
                            is TestEnded -> Unit
                            is TestRunFailed -> Unit
                            is TestRunStopped -> Unit
                            is TestRunEnded -> Unit
                        }
                    }
                }
            }
            tests
        }
    }
}
