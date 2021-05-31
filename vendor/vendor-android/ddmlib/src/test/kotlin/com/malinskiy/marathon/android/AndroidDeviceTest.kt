package com.malinskiy.marathon.android

import com.android.ddmlib.IDevice
import com.android.sdklib.AndroidVersion
import com.malinskiy.marathon.analytics.internal.pub.Track
import com.malinskiy.marathon.android.configuration.AndroidConfiguration
import com.malinskiy.marathon.android.configuration.SerialStrategy
import com.malinskiy.marathon.android.ddmlib.DdmlibAndroidDevice
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.time.SystemTimer
import com.nhaarman.mockitokotlin2.reset
import com.nhaarman.mockitokotlin2.whenever
import org.amshove.kluent.mock
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEqualTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Clock

class AndroidDeviceTest {
    private val iDevice = mock<IDevice>()
    private val configuration = mock<Configuration>()
    private val androidConfiguration = mock<AndroidConfiguration>()
    private val track = Track()
    private val timer = SystemTimer(Clock.systemDefaultZone())

    @BeforeEach
    fun `setup mock`() {
        reset(iDevice)
    }

    @Test
    fun `model return Unknown if ddmDevice property ro_product_model`() {
        whenever(iDevice.getProperty("ro.product.model")).thenReturn(null)
        DdmlibAndroidDevice(
            iDevice,
            "serial",
            configuration,
            androidConfiguration,
            track,
            timer,
            SerialStrategy.AUTOMATIC
        ).model shouldBe "Unknown"
    }

    @Test
    fun `manufacturer return Unknown if ddmlib property`() {
        whenever(iDevice.getProperty("ro.product.manufacturer")).thenReturn(null)
        DdmlibAndroidDevice(
            iDevice,
            "serial",
            configuration,
            androidConfiguration,
            track,
            timer,
            SerialStrategy.AUTOMATIC
        ).manufacturer shouldBe "Unknown"
    }

    @Test
    fun `should return ddmlib version instead of ro_build_version_sdk property value`() {
        val default = AndroidVersion.DEFAULT
        whenever(iDevice.version).thenReturn(default)
        whenever(iDevice.getProperty("ro.build.version.sdk")).thenReturn("INVALID_VERSION")
        DdmlibAndroidDevice(
            iDevice,
            "serial",
            configuration,
            androidConfiguration,
            track,
            timer,
            SerialStrategy.AUTOMATIC
        ).operatingSystem.version shouldBeEqualTo default.apiString
    }
}
