package com.malinskiy.marathon.android

import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.execution.TestParser
import com.malinskiy.marathon.log.MarathonLogConfigurator
import com.malinskiy.marathon.vendor.VendorConfiguration
import java.io.File

const val defaultInitTimeoutMillis = 30_000

const val DEFAULT_AUTO_GRANT_PERMISSION = false
const val DEFAULT_APPLICATION_PM_CLEAR = false
const val DEFAULT_TEST_APPLICATION_PM_CLEAR = false

data class AndroidConfiguration(val androidSdk: File,
                                val applicationOutput: File?,
                                val testApplicationOutput: File,
                                val autoGrantPermission: Boolean = DEFAULT_AUTO_GRANT_PERMISSION,
                                val applicationPmClear: Boolean = DEFAULT_APPLICATION_PM_CLEAR,
                                val testApplicationPmClear: Boolean = DEFAULT_TEST_APPLICATION_PM_CLEAR,
                                val adbInitTimeoutMillis: Int = defaultInitTimeoutMillis) : VendorConfiguration {

    override fun testParser(): TestParser? {
        return AndroidTestParser()
    }

    override fun deviceProvider(): DeviceProvider? {
        return AndroidDeviceProvider()
    }

    override fun logConfigurator(): MarathonLogConfigurator? = null
}
