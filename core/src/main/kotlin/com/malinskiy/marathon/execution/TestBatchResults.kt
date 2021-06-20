package com.malinskiy.marathon.execution

import com.malinskiy.marathon.device.Device

data class TestBatchResults(
    val device: Device,
    val finished: Collection<TestResult>,
    val failed: Collection<TestResult>,
    val uncompleted: Collection<TestResult>,
    val runCompletionReason: RunCompletionReason
) {

    enum class RunCompletionReason {
        RUN_FAILED,
        RUN_STOPPED,
        RUN_END
    }
}
