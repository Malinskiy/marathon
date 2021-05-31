package com.malinskiy.marathon.android.configuration

import com.malinskiy.marathon.android.ScreenRecordConfiguration
import com.malinskiy.marathon.android.di.androidModule
import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.execution.TestParser
import com.malinskiy.marathon.log.MarathonLogConfigurator
import com.malinskiy.marathon.vendor.VendorConfiguration
import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.core.module.Module
import org.koin.dsl.module
import java.io.File

const val defaultInitTimeoutMillis = 30_000

const val DEFAULT_AUTO_GRANT_PERMISSION = false
const val DEFAULT_APPLICATION_PM_CLEAR = false
const val DEFAULT_TEST_APPLICATION_PM_CLEAR = false
const val DEFAULT_INSTALL_OPTIONS = ""
const val DEFAULT_WAIT_FOR_DEVICES_TIMEOUT = 30000L

data class AndroidConfiguration(
    val androidSdk: File,
    val applicationOutput: File?,
    val testApplicationOutput: File,
    val implementationModules: List<Module>,
    val autoGrantPermission: Boolean = DEFAULT_AUTO_GRANT_PERMISSION,
    val instrumentationArgs: Map<String, String> = emptyMap(),
    val applicationPmClear: Boolean = DEFAULT_APPLICATION_PM_CLEAR,
    val testApplicationPmClear: Boolean = DEFAULT_TEST_APPLICATION_PM_CLEAR,
    val adbInitTimeoutMillis: Int = defaultInitTimeoutMillis,
    val installOptions: String = DEFAULT_INSTALL_OPTIONS,
    val serialStrategy: SerialStrategy = SerialStrategy.AUTOMATIC,
    val screenRecordConfiguration: ScreenRecordConfiguration = ScreenRecordConfiguration(),
    val waitForDevicesTimeoutMillis: Long = DEFAULT_WAIT_FOR_DEVICES_TIMEOUT,
    val allureConfiguration: AllureConfiguration = DEFAULT_ALLURE_CONFIGURATION,
    val timeoutConfiguration: TimeoutConfiguration = TimeoutConfiguration(),
    val fileSyncConfiguration: FileSyncConfiguration = FileSyncConfiguration(),
    val threadingConfiguration: ThreadingConfiguration = ThreadingConfiguration(),
    val testAccessConfiguration: TestAccessConfiguration = TestAccessConfiguration(),
) : VendorConfiguration, KoinComponent {

    override fun testParser(): TestParser? = get()

    override fun deviceProvider(): DeviceProvider? = get()

    override fun logConfigurator(): MarathonLogConfigurator = AndroidLogConfigurator()

    override fun modules() = listOf(androidModule) + implementationModules + module { single { this@AndroidConfiguration } }
}
