package com.malinskiy.marathon.android.adam.di

import com.malinskiy.marathon.android.adam.AdamDeviceProvider
import com.malinskiy.marathon.android.adam.OnDeviceTestParser
import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.execution.TestParser
import org.koin.dsl.module

val adamModule = module {
    single<DeviceProvider?> { AdamDeviceProvider(get(), get(), get(), get()) }
    single<TestParser?>(override = true) { OnDeviceTestParser(get()) }
}
