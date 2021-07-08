package com.malinskiy.marathon.report.junit.model

data class Rerun(
    val message: String? = null,
    val type: String = "rerun exception",
    val stackTrace: String? = null,
    val systemOut: String? = null,
    val systemErr: String? = null,
)
