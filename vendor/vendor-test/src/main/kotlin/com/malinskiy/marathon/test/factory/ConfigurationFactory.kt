package com.malinskiy.marathon.test.factory

import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.execution.policy.ScreenRecordingPolicy
import com.malinskiy.marathon.execution.strategy.FlakinessStrategy
import com.malinskiy.marathon.execution.strategy.RetryStrategy
import com.malinskiy.marathon.execution.strategy.ShardingStrategy
import com.malinskiy.marathon.test.Mocks
import com.malinskiy.marathon.test.StubDeviceProvider
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.TestVendorConfiguration
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.channels.Channel
import java.nio.file.Files

class ConfigurationFactory {
    var name = "DEFAULT_TEST_CONFIG"
    var outputDir = Files.createTempDirectory("test-run").toFile()
    var vendorConfiguration = TestVendorConfiguration(Mocks.TestParser.DEFAULT, StubDeviceProvider())
    var debug = null
    var batchingStrategy = null
    var analyticsConfiguration = null
    var excludeSerialRegexes: List<Regex>? = null
    var fallbackToScreenshots = null
    var strictMode = null
    var uncompletedTestRetryQuota: Int? = null
    var filteringConfiguration = null
    var flakinessStrategy: FlakinessStrategy? = null
    var ignoreFailures = null
    var includeSerialRegexes: List<Regex>? = null
    var isCodeCoverageEnabled = null
    var poolingStrategy = null
    var retryStrategy: RetryStrategy? = null
    var shardingStrategy: ShardingStrategy? = null
    var sortingStrategy = null
    var testClassRegexes = null
    var testBatchTimeoutMillis = null
    var testOutputTimeoutMillis = null
    var analyticsTracking = false
    var screenRecordingPolicy: ScreenRecordingPolicy? = null
    var deviceInitializationTimeoutMillis: Long? = null

    suspend fun tests(block: () -> List<Test>) {
        val testParser = vendorConfiguration.testParser()
        whenever(testParser.extract(any())).thenReturn(block.invoke())
    }

    fun devices(f: suspend (Channel<DeviceProvider.DeviceEvent>) -> Unit) {
        val stubDeviceProvider = vendorConfiguration.deviceProvider()
        stubDeviceProvider.providingLogic = f
    }

    fun build(): Configuration =
        Configuration(
            name,
            outputDir,
            analyticsConfiguration,
            poolingStrategy,
            shardingStrategy,
            sortingStrategy,
            batchingStrategy,
            flakinessStrategy,
            retryStrategy,
            filteringConfiguration,
            ignoreFailures,
            isCodeCoverageEnabled,
            fallbackToScreenshots,
            strictMode,
            uncompletedTestRetryQuota,
            testClassRegexes,
            includeSerialRegexes,
            excludeSerialRegexes,
            testBatchTimeoutMillis,
            testOutputTimeoutMillis,
            debug,
            screenRecordingPolicy,
            vendorConfiguration,
            analyticsTracking,
            deviceInitializationTimeoutMillis
        )
}
