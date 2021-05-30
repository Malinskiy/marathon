package com.malinskiy.marathon.execution

import com.malinskiy.marathon.execution.policy.ScreenRecordingPolicy
import com.malinskiy.marathon.execution.strategy.BatchingStrategy
import com.malinskiy.marathon.execution.strategy.FlakinessStrategy
import com.malinskiy.marathon.execution.strategy.PoolingStrategy
import com.malinskiy.marathon.execution.strategy.RetryStrategy
import com.malinskiy.marathon.execution.strategy.ShardingStrategy
import com.malinskiy.marathon.execution.strategy.SortingStrategy
import com.malinskiy.marathon.execution.strategy.StrictMode
import com.malinskiy.marathon.execution.strategy.impl.batching.IsolateBatchingStrategy
import com.malinskiy.marathon.execution.strategy.impl.flakiness.IgnoreFlakinessStrategy
import com.malinskiy.marathon.execution.strategy.impl.pooling.OmniPoolingStrategy
import com.malinskiy.marathon.execution.strategy.impl.retry.NoRetryStrategy
import com.malinskiy.marathon.execution.strategy.impl.sharding.ParallelShardingStrategy
import com.malinskiy.marathon.execution.strategy.impl.sorting.NoSortingStrategy
import com.malinskiy.marathon.vendor.VendorConfiguration
import java.io.File

private const val DEFAULT_BATCH_EXECUTION_TIMEOUT_MILLIS: Long = 1800_000 //30 min
private const val DEFAULT_OUTPUT_TIMEOUT_MILLIS: Long = 300_000 //5 min
private const val DEFAULT_DEVICE_INITIALIZATION_TIMEOUT_MILLIS = 180_000L

data class Configuration constructor(
    val name: String,
    val outputDir: File,

    val analyticsConfiguration: AnalyticsConfiguration,
    val poolingStrategy: PoolingStrategy,
    val shardingStrategy: ShardingStrategy,
    val sortingStrategy: SortingStrategy,
    val batchingStrategy: BatchingStrategy,
    val flakinessStrategy: FlakinessStrategy,
    val retryStrategy: RetryStrategy,
    val filteringConfiguration: FilteringConfiguration,

    val ignoreFailures: Boolean,
    val isCodeCoverageEnabled: Boolean,
    val fallbackToScreenshots: Boolean,
    val strictMode: StrictMode,
    val uncompletedTestRetryQuota: Int,

    val testClassRegexes: Collection<Regex>,
    val includeSerialRegexes: Collection<Regex>,
    val excludeSerialRegexes: Collection<Regex>,

    val testBatchTimeoutMillis: Long,
    val testOutputTimeoutMillis: Long,
    val debug: Boolean,

    val screenRecordingPolicy: ScreenRecordingPolicy,

    val vendorConfiguration: VendorConfiguration,

    val analyticsTracking: Boolean,
    val deviceInitializationTimeoutMillis: Long
) {

    constructor(
        name: String,
        outputDir: File,

        analyticsConfiguration: AnalyticsConfiguration?,
        poolingStrategy: PoolingStrategy?,
        shardingStrategy: ShardingStrategy?,
        sortingStrategy: SortingStrategy?,
        batchingStrategy: BatchingStrategy?,
        flakinessStrategy: FlakinessStrategy?,
        retryStrategy: RetryStrategy?,
        filteringConfiguration: FilteringConfiguration?,

        ignoreFailures: Boolean?,
        isCodeCoverageEnabled: Boolean?,
        fallbackToScreenshots: Boolean?,
        strictMode: StrictMode?,
        uncompletedTestRetryQuota: Int?,

        testClassRegexes: Collection<Regex>?,
        includeSerialRegexes: Collection<Regex>?,
        excludeSerialRegexes: Collection<Regex>?,

        testBatchTimeoutMillis: Long?,
        testOutputTimeoutMillis: Long?,
        debug: Boolean?,

        screenRecordingPolicy: ScreenRecordingPolicy?,

        vendorConfiguration: VendorConfiguration,

        analyticsTracking: Boolean?,
        deviceInitializationTimeoutMillis: Long?
    ) :

            this(
                name = name,
                outputDir = outputDir,
                analyticsConfiguration = analyticsConfiguration ?: AnalyticsConfiguration.DisabledAnalytics,
                poolingStrategy = poolingStrategy ?: OmniPoolingStrategy(),
                shardingStrategy = shardingStrategy ?: ParallelShardingStrategy(),
                sortingStrategy = sortingStrategy ?: NoSortingStrategy(),
                batchingStrategy = batchingStrategy ?: IsolateBatchingStrategy(),
                flakinessStrategy = flakinessStrategy ?: IgnoreFlakinessStrategy(),
                retryStrategy = retryStrategy ?: NoRetryStrategy(),
                filteringConfiguration = filteringConfiguration ?: FilteringConfiguration(emptyList(), emptyList()),
                ignoreFailures = ignoreFailures ?: false,
                isCodeCoverageEnabled = isCodeCoverageEnabled ?: false,
                fallbackToScreenshots = fallbackToScreenshots ?: false,
                strictMode = strictMode ?: StrictMode.ANY_SUCCESS,
                uncompletedTestRetryQuota = uncompletedTestRetryQuota ?: Integer.MAX_VALUE,
                testClassRegexes = testClassRegexes ?: listOf(Regex("^((?!Abstract).)*Test[s]*$")),
                includeSerialRegexes = includeSerialRegexes ?: emptyList(),
                excludeSerialRegexes = excludeSerialRegexes ?: emptyList(),
                testBatchTimeoutMillis = testBatchTimeoutMillis ?: DEFAULT_BATCH_EXECUTION_TIMEOUT_MILLIS,
                testOutputTimeoutMillis = testOutputTimeoutMillis ?: DEFAULT_OUTPUT_TIMEOUT_MILLIS,
                debug = debug ?: true,
                screenRecordingPolicy = screenRecordingPolicy ?: ScreenRecordingPolicy.ON_FAILURE,
                vendorConfiguration = vendorConfiguration,
                analyticsTracking = analyticsTracking ?: false,
                deviceInitializationTimeoutMillis = deviceInitializationTimeoutMillis ?: DEFAULT_DEVICE_INITIALIZATION_TIMEOUT_MILLIS
            )

    fun toMap() =
        mapOf<String, String>(
            "name" to name,
            "outputDir" to outputDir.absolutePath,
            "analyticsConfiguration" to analyticsConfiguration.toString(),
            "pooling" to poolingStrategy.toString(),
            "sharding" to shardingStrategy.toString(),
            "sorting" to sortingStrategy.toString(),
            "batching" to batchingStrategy.toString(),
            "flakiness" to flakinessStrategy.toString(),
            "retry" to retryStrategy.toString(),
            "filtering" to filteringConfiguration.toString(),
            "ignoreFailures" to ignoreFailures.toString(),
            "isCodeCoverageEnabled" to isCodeCoverageEnabled.toString(),
            "fallbackToScreenshots" to fallbackToScreenshots.toString(),
            "strictMode" to strictMode.toString(),
            "testClassRegexes" to testClassRegexes.toString(),
            "includeSerialRegexes" to includeSerialRegexes.toString(),
            "excludeSerialRegexes" to excludeSerialRegexes.toString(),
            "testBatchTimeoutMillis" to testBatchTimeoutMillis.toString(),
            "testOutputTimeoutMillis" to testOutputTimeoutMillis.toString(),
            "debug" to debug.toString(),
            "screenRecordingPolicy" to screenRecordingPolicy.toString(),
            "vendorConfiguration" to vendorConfiguration.toString(),
            "deviceInitializationTimeoutMillis" to deviceInitializationTimeoutMillis.toString()
        )
}
