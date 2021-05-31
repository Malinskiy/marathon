package com.malinskiy.marathon.android.configuration

import java.time.Duration


data class TimeoutConfiguration(
    var shell: Duration = Duration.ofSeconds(20),
    var listFiles: Duration = shell,
    var pushFile: Duration = Duration.ofSeconds(60),
    var pullFile: Duration = Duration.ofSeconds(30),
    var uninstall: Duration = shell,
    var install: Duration = shell,
    var screenrecorder: Duration = Duration.ofMinutes(10),
    var screencapturer: Duration = Duration.ofMillis(300),
    var socketIdleTimeout: Duration = Duration.ofSeconds(30),
    var portForward: Duration = shell,
)
