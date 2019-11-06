package com.malinskiy.marathon.android

import com.malinskiy.marathon.android.di.androidModule
import com.malinskiy.marathon.android.serial.SerialStrategy
import com.malinskiy.marathon.device.DeviceFeature
import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.execution.TestParser
import com.malinskiy.marathon.log.MarathonLogConfigurator
import com.malinskiy.marathon.vendor.VendorConfiguration
import org.koin.core.KoinComponent
import org.koin.core.get
import java.io.File

const val defaultInitTimeoutMillis = 30_000

const val DEFAULT_AUTO_GRANT_PERMISSION = false
const val DEFAULT_APPLICATION_PM_CLEAR = false
const val DEFAULT_TEST_APPLICATION_PM_CLEAR = false
const val DEFAULT_INSTALL_OPTIONS = ""
const val DEFAULT_ENABLE_KASPRESSO_STEPS_LISTENER = false

data class AndroidConfiguration(
    val androidSdk: File,
    val applicationOutput: File?,
    val testApplicationOutput: File,
    val autoGrantPermission: Boolean = DEFAULT_AUTO_GRANT_PERMISSION,
    val instrumentationArgs: Map<String, String> = emptyMap(),
    val applicationPmClear: Boolean = DEFAULT_APPLICATION_PM_CLEAR,
    val testApplicationPmClear: Boolean = DEFAULT_TEST_APPLICATION_PM_CLEAR,
    val adbInitTimeoutMillis: Int = defaultInitTimeoutMillis,
    val installOptions: String = DEFAULT_INSTALL_OPTIONS,
    val preferableRecorderType: DeviceFeature? = null,
    val serialStrategy: SerialStrategy = SerialStrategy.AUTOMATIC,
    val enableKaspressoStepsListener: Boolean = DEFAULT_ENABLE_KASPRESSO_STEPS_LISTENER
) : VendorConfiguration, KoinComponent {

    override fun testParser(): TestParser? = get()

    override fun deviceProvider(): DeviceProvider? = get()

    override fun logConfigurator(): MarathonLogConfigurator = AndroidLogConfigurator()

    override fun preferableRecorderType(): DeviceFeature? = preferableRecorderType

    override fun modules() = listOf(androidModule)
}
