package com.malinskiy.marathon.report.junit.model

/**
 * @param message message/description string why the test case was skipped. optional
 */
data class Skipped(
    val message: String? = null,
    val description: String
)
