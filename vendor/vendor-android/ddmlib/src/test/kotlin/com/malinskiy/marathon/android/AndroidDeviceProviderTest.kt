package com.malinskiy.marathon.android

import com.malinskiy.marathon.analytics.internal.pub.Track
import com.malinskiy.marathon.android.configuration.AndroidConfiguration
import com.malinskiy.marathon.android.ddmlib.DdmlibDeviceProvider
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.time.SystemTimer
import ddmlibModule
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.shouldEqual
import org.junit.jupiter.api.Test
import java.io.File
import java.time.Clock

class AndroidDeviceProviderTest {
    @Test
    fun `terminate should close the channel`() {
        val configuration = Configuration(
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
            strictMode = null,
            uncompletedTestRetryQuota = null,
            testClassRegexes = null,
            includeSerialRegexes = null,
            excludeSerialRegexes = null,
            testBatchTimeoutMillis = null,
            testOutputTimeoutMillis = null,
            debug = null,
            screenRecordingPolicy = null,
            vendorConfiguration = AndroidConfiguration(
                File(""),
                applicationOutput = File(""),
                testApplicationOutput = File(""),
                implementationModules = listOf(ddmlibModule)
            ),
            analyticsTracking = false,
            deviceInitializationTimeoutMillis = null
        )
        val provider = DdmlibDeviceProvider(configuration, Track(), SystemTimer(Clock.systemDefaultZone()))

        runBlocking {
            provider.terminate()
        }

        provider.subscribe().isClosedForReceive shouldEqual true
        provider.subscribe().isClosedForSend shouldEqual true
    }
}
