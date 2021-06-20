package com.malinskiy.marathon.ios

import com.malinskiy.marathon.analytics.internal.pub.Track
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.time.SystemTimer
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.shouldEqual
import org.junit.jupiter.api.Test
import java.io.File
import java.time.Clock

class IOSDeviceProviderTest {
    private val configuration = Configuration(
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
        debug = null,
        screenRecordingPolicy = null,
        vendorConfiguration = IOSConfiguration(
            derivedDataDir = File(""),
            xctestrunPath = File(""),
            remoteUsername = "testuser",
            remotePrivateKey = File("/home/fakekey"),
            knownHostsPath = null,
            remoteRsyncPath = "/remote/rsync",
            sourceRoot = File(""),
            debugSsh = false,
            alwaysEraseSimulators = true
        ),
        analyticsTracking = false,
        deviceInitializationTimeoutMillis = null
    )
    private val provider = IOSDeviceProvider(configuration, Track(), SystemTimer(Clock.systemDefaultZone()))

    @Test
    fun `should close the channel`() {
        runBlocking {
            provider.terminate()
        }

        provider.subscribe().isClosedForReceive shouldEqual true
        provider.subscribe().isClosedForSend shouldEqual true
    }
}
