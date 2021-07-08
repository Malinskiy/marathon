package com.malinskiy.marathon.report.junit.model

/**
 * @param type The type of error that occurred. e.g., if a java exception is thrown the full class name of the exception
 * @param message The error message. e.g., if a java exception is thrown, the return value of getMessage()
 */
data class Error(
    val type: String,
    val message: String,
    val description: String
)
