package com.malinskiy.marathon.android.configuration

data class TestAccessConfiguration(
    val adb: Boolean = false,
    val gRPC: Boolean = false,
    val console: Boolean = false,
    val consoleToken: String = "",
)
