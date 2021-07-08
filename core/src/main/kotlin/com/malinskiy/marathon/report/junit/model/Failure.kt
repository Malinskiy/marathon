package com.malinskiy.marathon.report.junit.model

/**
 * @param message The message specified in the assert
 * @param type The type of the assert
 */
data class Failure(
    val type: String? = null,
    val message: String? = null,
    val description: String,
)
