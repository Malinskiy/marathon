package com.malinskiy.marathon

import com.malinskiy.marathon.android.ScreenRecordConfiguration
import com.malinskiy.marathon.android.VendorType
import com.malinskiy.marathon.android.configuration.AllureConfiguration
import com.malinskiy.marathon.android.configuration.FileSyncConfiguration
import com.malinskiy.marathon.android.configuration.SerialStrategy
import com.malinskiy.marathon.android.configuration.TimeoutConfiguration
import com.malinskiy.marathon.execution.policy.ScreenRecordingPolicy
import com.malinskiy.marathon.execution.strategy.ExecutionStrategy
import groovy.lang.Closure

open class MarathonExtension {
    var name: String = "Marathon"

    var vendor: VendorType? = null
    var bugsnag: Boolean? = null

    var analyticsConfiguration: AnalyticsConfig? = null

    var poolingStrategy: PoolingStrategyConfiguration? = null
    var shardingStrategy: ShardingStrategyConfiguration? = null
    var sortingStrategy: SortingStrategyConfiguration? = null
    var batchingStrategy: BatchingStrategyConfiguration? = null
    var flakinessStrategy: FlakinessStrategyConfiguration? = null
    var retryStrategy: RetryStrategyConfiguration? = null
    var filteringConfiguration: FilteringPluginConfiguration? = null

    var baseOutputDir: String? = null

    var ignoreFailures: Boolean? = null
    var isCodeCoverageEnabled: Boolean? = null
    var fallbackToScreenshots: Boolean? = null
    @Deprecated("Will be deleted in 0.7.0. Use executionStrategy")
    var strictMode: Boolean? = null
    var executionStrategy: ExecutionStrategy? = null
    var uncompletedTestRetryQuota: Int? = null

    var testClassRegexes: Collection<String>? = null
    var includeSerialRegexes: Collection<String>? = null
    var excludeSerialRegexes: Collection<String>? = null

    var testBatchTimeoutMillis: Long? = null
    var testOutputTimeoutMillis: Long? = null
    var debug: Boolean? = null

    var screenRecordingPolicy: ScreenRecordingPolicy? = null

    var applicationPmClear: Boolean? = null
    var testApplicationPmClear: Boolean? = null
    var adbInitTimeout: Int? = null
    var installOptions: String? = null
    var serialStrategy: SerialStrategy? = null

    var screenRecordConfiguration: ScreenRecordConfiguration? = null

    var analyticsTracking: Boolean = false

    var deviceInitializationTimeoutMillis: Long? = null
    var waitForDevicesTimeoutMillis: Long? = null

    var allureConfiguration: AllureConfiguration? = null
    var timeoutConfiguration: TimeoutConfiguration? = null
    var fileSyncConfiguration: FileSyncConfiguration? = null

    //Android specific for now
    var autoGrantPermission: Boolean? = null
    var instrumentationArgs: MutableMap<String, String> = mutableMapOf()

    //Kotlin way
    fun analytics(block: AnalyticsConfig.() -> Unit) {
        analyticsConfiguration = AnalyticsConfig().also(block)
    }

    fun batchingStrategy(block: BatchingStrategyConfiguration.() -> Unit) {
        batchingStrategy = BatchingStrategyConfiguration().also(block)
    }

    fun flakinessStrategy(block: FlakinessStrategyConfiguration.() -> Unit) {
        flakinessStrategy = FlakinessStrategyConfiguration().also(block)
    }

    fun poolingStrategy(block: PoolingStrategyConfiguration.() -> Unit) {
        poolingStrategy = PoolingStrategyConfiguration().also(block)
    }

    fun retryStrategy(block: RetryStrategyConfiguration.() -> Unit) {
        retryStrategy = RetryStrategyConfiguration().also(block)
    }

    fun shardingStrategy(block: ShardingStrategyConfiguration.() -> Unit) {
        shardingStrategy = ShardingStrategyConfiguration().also(block)
    }

    fun sortingStrategy(block: SortingStrategyConfiguration.() -> Unit) {
        sortingStrategy = SortingStrategyConfiguration().also(block)
    }

    fun filteringConfiguration(block: FilteringPluginConfiguration.() -> Unit) {
        filteringConfiguration = FilteringPluginConfiguration().also(block)
    }

    fun instrumentationArgs(block: MutableMap<String, String>.() -> Unit) {
        instrumentationArgs = mutableMapOf<String, String>().also(block)
    }

    fun allureConfiguration(block: AllureConfiguration.() -> Unit) {
        allureConfiguration = AllureConfiguration().also(block)
    }

    fun timeoutConfiguration(block: TimeoutConfiguration.() -> Unit) {
        timeoutConfiguration = TimeoutConfiguration().also(block)
    }

    fun fileSyncConfiguration(block: FileSyncConfiguration.() -> Unit) {
        fileSyncConfiguration = FileSyncConfiguration().also(block)
    }

    //Groovy way
    fun analytics(closure: Closure<*>) {
        analyticsConfiguration = AnalyticsConfig()
        closure.delegate = analyticsConfiguration
        closure.call()
    }

    fun batchingStrategy(closure: Closure<*>) {
        batchingStrategy = BatchingStrategyConfiguration()
        closure.delegate = batchingStrategy
        closure.call()
    }

    fun flakinessStrategy(closure: Closure<*>) {
        flakinessStrategy = FlakinessStrategyConfiguration()
        closure.delegate = flakinessStrategy
        closure.call()
    }

    fun poolingStrategy(closure: Closure<*>) {
        poolingStrategy = PoolingStrategyConfiguration()
        closure.delegate = poolingStrategy
        closure.call()
    }

    fun retryStrategy(closure: Closure<*>) {
        retryStrategy = RetryStrategyConfiguration()
        closure.delegate = retryStrategy
        closure.call()
    }

    fun shardingStrategy(closure: Closure<*>) {
        shardingStrategy = ShardingStrategyConfiguration()
        closure.delegate = shardingStrategy
        closure.call()
    }

    fun sortingStrategy(closure: Closure<*>) {
        sortingStrategy = SortingStrategyConfiguration()
        closure.delegate = sortingStrategy
        closure.call()
    }

    fun filteringConfiguration(closure: Closure<*>) {
        filteringConfiguration = FilteringPluginConfiguration()
        closure.delegate = filteringConfiguration
        closure.call()
    }

    fun instrumentationArgs(closure: Closure<*>) {
        instrumentationArgs = mutableMapOf()
        closure.delegate = instrumentationArgs
        closure.call()
    }

    fun allureConfiguration(closure: Closure<*>) {
        allureConfiguration = AllureConfiguration()
        closure.delegate = allureConfiguration
        closure.call()
    }

    fun timeoutConfiguration(closure: Closure<*>) {
        timeoutConfiguration = TimeoutConfiguration()
        closure.delegate = timeoutConfiguration
        closure.call()
    }

    fun fileSyncConfiguration(closure: Closure<*>) {
        fileSyncConfiguration = FileSyncConfiguration()
        closure.delegate = fileSyncConfiguration
        closure.call()
    }
}
