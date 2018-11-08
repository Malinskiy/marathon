package com.malinskiy.marathon.android.executor

import com.android.ddmlib.IDevice
import com.android.ddmlib.InstallException
import com.malinskiy.marathon.android.AndroidConfiguration
import com.malinskiy.marathon.android.ApkParser
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.execution.withRetry
import com.malinskiy.marathon.log.MarathonLogging
import java.io.File

class AndroidAppInstaller(private val configuration: Configuration) {

    companion object {
        private const val MAX_RETIRES = 3
        private const val MARSHMALLOW_VERSION_CODE = 23
    }

    private val logger = MarathonLogging.logger("AndroidAppInstaller")
    private val androidConfiguration = configuration.vendorConfiguration as AndroidConfiguration

    fun prepareInstallation(device: IDevice) {
        val applicationInfo = ApkParser().parseInstrumentationInfo(androidConfiguration.testApplicationOutput)
        androidConfiguration.applicationOutput?.let {
            reinstall(device, applicationInfo.applicationPackage, it)
        }
        reinstall(device, applicationInfo.instrumentationPackage, androidConfiguration.testApplicationOutput)
    }

    @Suppress("TooGenericExceptionThrown")
    private fun reinstall(device: IDevice, appPackage: String, appApk: File) {
        withRetry(attempts = MAX_RETIRES, delay = 1000) {
            try {
                logger.info("Uninstalling $appPackage from $device.serialNumber")
                device.uninstallPackage(appPackage)
                logger.info("Installing $appPackage to $device.serialNumber")
                device.installPackage(appApk.absolutePath, true, optionalParams(device))
            } catch (e: InstallException) {
                throw RuntimeException("Error while installing $appPackage on ${device.serialNumber}", e)
            }
        }
    }

    private fun optionalParams(device: IDevice): String {
        return if (device.version.apiLevel >= MARSHMALLOW_VERSION_CODE && androidConfiguration.autoGrantPermission) {
            "-g"
        } else {
            ""
        }
    }
}
