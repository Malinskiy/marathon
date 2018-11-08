package com.malinskiy.marathon.execution

import com.malinskiy.marathon.execution.strategy.BatchingStrategy
import com.malinskiy.marathon.execution.strategy.FlakinessStrategy
import com.malinskiy.marathon.execution.strategy.PoolingStrategy
import com.malinskiy.marathon.execution.strategy.RetryStrategy
import com.malinskiy.marathon.execution.strategy.ShardingStrategy
import com.malinskiy.marathon.execution.strategy.SortingStrategy
import com.malinskiy.marathon.execution.strategy.impl.batching.IsolateBatchingStrategy
import com.malinskiy.marathon.execution.strategy.impl.flakiness.IgnoreFlakinessStrategy
import com.malinskiy.marathon.execution.strategy.impl.pooling.OmniPoolingStrategy
import com.malinskiy.marathon.execution.strategy.impl.retry.NoRetryStrategy
import com.malinskiy.marathon.execution.strategy.impl.sharding.ParallelShardingStrategy
import com.malinskiy.marathon.execution.strategy.impl.sorting.NoSortingStrategy
import com.malinskiy.marathon.vendor.VendorConfiguration
import java.io.File

private const val DEFAULT_OUTPUT_TIMEOUT_MILLIS: Long = 60_000

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

        val testClassRegexes: Collection<Regex>,
        val includeSerialRegexes: Collection<Regex>,
        val excludeSerialRegexes: Collection<Regex>,

        val testOutputTimeoutMillis: Long,
        val debug: Boolean,

        val vendorConfiguration: VendorConfiguration) {

    constructor(name: String,
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

                testClassRegexes: Collection<Regex>?,
                includeSerialRegexes: Collection<Regex>?,
                excludeSerialRegexes: Collection<Regex>?,

                testOutputTimeoutMillis: Long?,
                debug: Boolean?,

                vendorConfiguration: VendorConfiguration) :

            this(name = name,
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
                    testClassRegexes = testClassRegexes ?: listOf(Regex("^((?!Abstract).)*Test$")),
                    includeSerialRegexes = includeSerialRegexes ?: emptyList(),
                    excludeSerialRegexes = excludeSerialRegexes ?: emptyList(),
                    testOutputTimeoutMillis = testOutputTimeoutMillis ?: DEFAULT_OUTPUT_TIMEOUT_MILLIS,
                    debug = debug ?: true,
                    vendorConfiguration = vendorConfiguration
            )
}
