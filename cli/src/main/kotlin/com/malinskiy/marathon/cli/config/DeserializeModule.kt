package com.malinskiy.marathon.cli.config

import com.fasterxml.jackson.databind.module.SimpleModule
import com.malinskiy.marathon.cli.args.FileVendorConfiguration
import com.malinskiy.marathon.cli.config.deserialize.AnalyticsConfigurationDeserializer
import com.malinskiy.marathon.cli.config.deserialize.BatchingStrategyDeserializer
import com.malinskiy.marathon.cli.config.deserialize.ExecutionStrategyDeserializer
import com.malinskiy.marathon.cli.config.deserialize.ExecutionTimeSortingStrategyDeserializer
import com.malinskiy.marathon.cli.config.deserialize.FileVendorConfigurationDeserializer
import com.malinskiy.marathon.cli.config.deserialize.FixedSizeBatchingStrategyDeserializer
import com.malinskiy.marathon.cli.config.deserialize.FlakinessStrategyDeserializer
import com.malinskiy.marathon.cli.config.deserialize.GraphiteConfigurationDeserializer
import com.malinskiy.marathon.cli.config.deserialize.InfluxDbConfigurationDeserializer
import com.malinskiy.marathon.cli.config.deserialize.PoolingStrategyDeserializer
import com.malinskiy.marathon.cli.config.deserialize.ProbabilityBasedFlakinessStrategyDeserializer
import com.malinskiy.marathon.cli.config.deserialize.RetentionPolicyConfigurationDeserializer
import com.malinskiy.marathon.cli.config.deserialize.RetryStrategyDeserializer
import com.malinskiy.marathon.cli.config.deserialize.ScreenRecordingPolicyDeserializer
import com.malinskiy.marathon.cli.config.deserialize.ShardingStrategyDeserializer
import com.malinskiy.marathon.cli.config.deserialize.SortingStrategyDeserializer
import com.malinskiy.marathon.cli.config.deserialize.TestFilterDeserializer
import com.malinskiy.marathon.cli.config.time.InstantTimeProvider
import com.malinskiy.marathon.execution.AnalyticsConfiguration
import com.malinskiy.marathon.execution.TestFilter
import com.malinskiy.marathon.execution.policy.ScreenRecordingPolicy
import com.malinskiy.marathon.execution.strategy.BatchingStrategy
import com.malinskiy.marathon.execution.strategy.ExecutionStrategy
import com.malinskiy.marathon.execution.strategy.FlakinessStrategy
import com.malinskiy.marathon.execution.strategy.PoolingStrategy
import com.malinskiy.marathon.execution.strategy.RetryStrategy
import com.malinskiy.marathon.execution.strategy.ShardingStrategy
import com.malinskiy.marathon.execution.strategy.SortingStrategy
import com.malinskiy.marathon.execution.strategy.impl.batching.FixedSizeBatchingStrategy
import com.malinskiy.marathon.execution.strategy.impl.flakiness.ProbabilityBasedFlakinessStrategy
import com.malinskiy.marathon.execution.strategy.impl.sorting.ExecutionTimeSortingStrategy

class DeserializeModule(instantTimeProvider: InstantTimeProvider) : SimpleModule() {
    init {
        addDeserializer(AnalyticsConfiguration::class.java, AnalyticsConfigurationDeserializer())
        addDeserializer(AnalyticsConfiguration.InfluxDbConfiguration::class.java, InfluxDbConfigurationDeserializer())
        addDeserializer(AnalyticsConfiguration.GraphiteConfiguration::class.java, GraphiteConfigurationDeserializer())
        addDeserializer(
            AnalyticsConfiguration.InfluxDbConfiguration.RetentionPolicyConfiguration::class.java,
            RetentionPolicyConfigurationDeserializer()
        )
        addDeserializer(PoolingStrategy::class.java, PoolingStrategyDeserializer())
        addDeserializer(ShardingStrategy::class.java, ShardingStrategyDeserializer())
        addDeserializer(SortingStrategy::class.java, SortingStrategyDeserializer())
        addDeserializer(
            ExecutionTimeSortingStrategy::class.java,
            ExecutionTimeSortingStrategyDeserializer(instantTimeProvider)
        )
        addDeserializer(BatchingStrategy::class.java, BatchingStrategyDeserializer())
        addDeserializer(FlakinessStrategy::class.java, FlakinessStrategyDeserializer())
        addDeserializer(
            ProbabilityBasedFlakinessStrategy::class.java,
            ProbabilityBasedFlakinessStrategyDeserializer(instantTimeProvider)
        )
        addDeserializer(
            FixedSizeBatchingStrategy::class.java,
            FixedSizeBatchingStrategyDeserializer(instantTimeProvider)
        )
        addDeserializer(RetryStrategy::class.java, RetryStrategyDeserializer())
        addDeserializer(TestFilter::class.java, TestFilterDeserializer())
        addDeserializer(FileVendorConfiguration::class.java, FileVendorConfigurationDeserializer())
        addDeserializer(ScreenRecordingPolicy::class.java, ScreenRecordingPolicyDeserializer())
        addDeserializer(ExecutionStrategy::class.java, ExecutionStrategyDeserializer())
    }
}
