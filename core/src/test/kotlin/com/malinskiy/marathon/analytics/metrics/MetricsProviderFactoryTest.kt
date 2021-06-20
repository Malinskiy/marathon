package com.malinskiy.marathon.analytics.metrics

import com.malinskiy.marathon.analytics.external.MetricsProviderFactory
import com.malinskiy.marathon.analytics.external.NoOpMetricsProvider
import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.execution.AnalyticsConfiguration
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.execution.TestParser
import com.malinskiy.marathon.log.MarathonLogConfigurator
import com.malinskiy.marathon.vendor.VendorConfiguration
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.jupiter.api.Test
import java.io.File

class MetricsProviderFactoryTest {

    @Test
    fun shouldReturnNoopProviderWhenDisabled() {
        val configuration = Configuration(
            name = "",
            outputDir = File(""),
            analyticsConfiguration = AnalyticsConfiguration.DisabledAnalytics,
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
            vendorConfiguration = object : VendorConfiguration {
                override fun testParser(): TestParser? = null
                override fun deviceProvider(): DeviceProvider? = null
                override fun logConfigurator(): MarathonLogConfigurator? = null
            },
            analyticsTracking = false,
            deviceInitializationTimeoutMillis = null
        )
        val factory = MetricsProviderFactory(configuration)
        val metricsProvider = factory.create()
        metricsProvider shouldBeInstanceOf NoOpMetricsProvider::class
    }

    @Test
    fun shouldReturnNoopProviderWhenConfigurationIsInvalid() {
        val analyticsConfiguration = AnalyticsConfiguration.InfluxDbConfiguration(
            "host",
            "user",
            "password",
            "db",
            AnalyticsConfiguration.InfluxDbConfiguration.RetentionPolicyConfiguration.default
        )
        val configuration = Configuration(
            name = "",
            outputDir = File(""),
            analyticsConfiguration = analyticsConfiguration,
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
            vendorConfiguration = object : VendorConfiguration {
                override fun testParser(): TestParser? = null
                override fun deviceProvider(): DeviceProvider? = null
                override fun logConfigurator(): MarathonLogConfigurator? = null
            },
            analyticsTracking = false,
            deviceInitializationTimeoutMillis = null
        )
        val factory = MetricsProviderFactory(configuration)
        val metricsProvider = factory.create()
        metricsProvider shouldBeInstanceOf NoOpMetricsProvider::class
    }
}
