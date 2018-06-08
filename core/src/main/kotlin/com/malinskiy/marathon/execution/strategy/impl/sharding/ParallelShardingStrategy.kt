package com.malinskiy.marathon.execution.strategy.impl.sharding

import com.malinskiy.marathon.execution.TestShard
import com.malinskiy.marathon.execution.strategy.ShardingStrategy
import com.malinskiy.marathon.test.Test

/**
 * Implements default sharding which executes all tests in parallel on all available devices in the pool
 */
class ParallelShardingStrategy : ShardingStrategy {
    override fun createShard(tests: Collection<Test>): TestShard {
        return TestShard(tests)
    }
}
