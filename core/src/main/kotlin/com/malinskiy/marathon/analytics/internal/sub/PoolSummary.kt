package com.malinskiy.marathon.analytics.internal.sub

import com.malinskiy.marathon.device.DeviceInfo
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.TestResult

data class PoolSummary(
    val poolId: DevicePoolId,
    val tests: List<TestResult>,
    val retries: Map<TestResult, List<TestEvent>>,
    val passed: Set<String>,
    val ignored: Set<String>,
    val failed: Set<String>,
    val flaky: Int,
    val durationMillis: Long,
    val devices: List<DeviceInfo>,
    val rawPassed: List<String>,
    val rawIgnored: List<String>,
    val rawFailed: List<String>,
    val rawIncomplete: List<String>,
    val rawDurationMillis: Long
)
