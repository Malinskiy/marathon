package com.malinskiy.marathon.cli.args

import com.malinskiy.marathon.android.ScreenRecordConfiguration
import com.malinskiy.marathon.android.VendorType
import com.malinskiy.marathon.android.configuration.AllureConfiguration
import com.malinskiy.marathon.android.configuration.FileSyncConfiguration
import com.malinskiy.marathon.android.configuration.SerialStrategy
import com.malinskiy.marathon.android.configuration.TestAccessConfiguration
import com.malinskiy.marathon.android.configuration.ThreadingConfiguration
import com.malinskiy.marathon.android.configuration.TimeoutConfiguration
import com.malinskiy.marathon.exceptions.ConfigurationException
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldThrow
import org.junit.jupiter.api.Test
import java.io.File

class FileAndroidConfigurationTest {
    private val configuration = FileAndroidConfiguration(
        VendorType.DDMLIB,
        null,
        null,
        File.createTempFile("foo", "bar"),
        null,
        null,
        null,
        null,
        null,
        null,
        SerialStrategy.AUTOMATIC,
        ScreenRecordConfiguration(),
        15000L,
        AllureConfiguration(),
        TimeoutConfiguration(),
        FileSyncConfiguration(),
        ThreadingConfiguration(),
        TestAccessConfiguration()
    )

    private val env: File = File.createTempFile("foo", "bar")
    private val sdk: File = File.createTempFile("android", "sdk")

    @Test
    fun `if androidSdk is null should throw Exception if env android sdk also is null`() {
        { configuration.toAndroidConfiguration(null) } shouldThrow ConfigurationException::class
    }

    @Test
    fun `if androidSdk is null should use env android sdk if it is not null`() {
        configuration.toAndroidConfiguration(env).androidSdk shouldEqual env
    }

    @Test
    fun `if android sdk is not null should use android sdk instead of env if both exists`() {
        configuration.copy(androidSdk = sdk).toAndroidConfiguration(env).androidSdk shouldEqual sdk
    }

    @Test
    fun `if android sdk is not null, test application output should be null by default`() {
        configuration.toAndroidConfiguration(env).applicationOutput shouldEqual null
    }

    @Test
    fun `if android sdk is not null, test application output should be null if provided`() {
        configuration.copy(applicationOutput = env).toAndroidConfiguration(env).applicationOutput shouldEqual env
    }

    @Test
    fun `if android sdk is not null test application apk should be equal`() {
        configuration.copy(testApplicationOutput = env).toAndroidConfiguration(env).testApplicationOutput shouldEqual env
    }

    @Test
    fun `if android sdk is not null auto grant permissions should be false by default`() {
        configuration.toAndroidConfiguration(env).autoGrantPermission shouldEqual false
    }

    @Test
    fun `if android sdk is not null auto grant permissions should be equal`() {
        configuration.copy(autoGrantPermission = false).toAndroidConfiguration(env).autoGrantPermission shouldEqual false
        configuration.copy(autoGrantPermission = true).toAndroidConfiguration(env).autoGrantPermission shouldEqual true
    }

    @Test
    fun `if android sdk is not null adb init timeout millis should be 30_000 by default`() {
        configuration.toAndroidConfiguration(env).adbInitTimeoutMillis shouldEqual 30_000
    }

    @Test
    fun `if android sdk is not null adb init timeout millis should be equal`() {
        val timeout = 500_000
        configuration.copy(adbInitTimeoutMillis = timeout).toAndroidConfiguration(env).adbInitTimeoutMillis shouldEqual timeout
    }

    @Test
    fun `if android sdk is not null install options should be empty string by default`() {
        configuration.toAndroidConfiguration(env).installOptions shouldEqual ""
    }

    @Test
    fun `if android sdk is not null install options should be equal if provided`() {
        configuration.copy(installOptions = "-d").toAndroidConfiguration(env).installOptions shouldEqual "-d"
    }
}
