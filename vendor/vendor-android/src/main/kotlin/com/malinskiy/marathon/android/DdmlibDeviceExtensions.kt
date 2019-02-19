package com.malinskiy.marathon.android

import com.android.annotations.NonNull
import com.android.ddmlib.AdbCommandRejectedException
import com.android.ddmlib.IDevice
import com.android.ddmlib.IShellOutputReceiver
import com.android.ddmlib.InstallException
import com.android.ddmlib.InstallReceiver
import com.android.ddmlib.MultiLineReceiver
import com.android.ddmlib.ScreenRecorderOptions
import com.android.ddmlib.ShellCommandUnresponsiveException
import com.android.ddmlib.TimeoutException
import java.io.IOException
import java.util.concurrent.TimeUnit

const val ADB_INSTALL_TIMEOUT_MINUTES = 4L
const val ADB_SHORT_TIMEOUT_SECONDS = 20L
const val ADB_SCREEN_RECORD_TIMEOUT = 10L

fun IDevice.safeUninstallPackage(packageName: String): String? {
    try {
        val receiver = InstallReceiver()
        executeShellCommand("pm uninstall $packageName",
                receiver,
                ADB_INSTALL_TIMEOUT_MINUTES,
                ADB_INSTALL_TIMEOUT_MINUTES,
                TimeUnit.MINUTES)

        return receiver.errorMessage
    } catch (e: TimeoutException) {
        throw InstallException(e)
    } catch (e: AdbCommandRejectedException) {
        throw InstallException(e)
    } catch (e: ShellCommandUnresponsiveException) {
        throw InstallException(e)
    } catch (e: IOException) {
        throw InstallException(e)
    }
}

fun IDevice.safeInstallPackage(packageFilePath: String, reinstall: Boolean, vararg extraArgs: String): String? {
    val receiver = InstallReceiver()

    installPackage(packageFilePath,
            reinstall,
            receiver,
            ADB_INSTALL_TIMEOUT_MINUTES,
            ADB_INSTALL_TIMEOUT_MINUTES,
            TimeUnit.MINUTES,
            *extraArgs)

    return receiver.errorMessage
}

fun IDevice.safeExecuteShellCommand(command: String, receiver: IShellOutputReceiver) {
    executeShellCommand(command, receiver, ADB_SHORT_TIMEOUT_SECONDS, ADB_SHORT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
}

fun IDevice.safeStartScreenRecorder(remoteFilePath: String, options: ScreenRecorderOptions, receiver: IShellOutputReceiver) {
    val screenRecorderCommand = getScreenRecorderCommand(remoteFilePath, options)
    executeShellCommand(screenRecorderCommand, receiver, ADB_SCREEN_RECORD_TIMEOUT, ADB_SCREEN_RECORD_TIMEOUT, TimeUnit.MINUTES)
}

fun IDevice.safeClearPackage(packageName: String): String? {
    var result: String? = null

    try {
        val receiver = SimpleOutputReceiver()
        executeShellCommand("pm clear $packageName",
                receiver,
                ADB_SHORT_TIMEOUT_SECONDS,
                ADB_SHORT_TIMEOUT_SECONDS,
                TimeUnit.SECONDS)

        result = receiver.output()
    } catch (e: TimeoutException) {
    } catch (e: AdbCommandRejectedException) {
    } catch (e: ShellCommandUnresponsiveException) {
    } catch (e: IOException) {
    } finally {
        return result
    }
}

fun getScreenRecorderCommand(@NonNull remoteFilePath: String,
                             @NonNull options: ScreenRecorderOptions): String {
    val sb = StringBuilder()

    sb.append("screenrecord")
    sb.append(' ')

    if (options.width > 0 && options.height > 0) {
        sb.append("--size ")
        sb.append(options.width)
        sb.append('x')
        sb.append(options.height)
        sb.append(' ')
    }

    if (options.bitrateMbps > 0) {
        sb.append("--bit-rate ")
        sb.append(options.bitrateMbps * 1000000)
        sb.append(' ')
    }

    if (options.timeLimit > 0) {
        sb.append("--time-limit ")
        var seconds = TimeUnit.SECONDS.convert(options.timeLimit, options.timeLimitUnits)
        if (seconds > 180) {
            seconds = 180
        }
        sb.append(seconds)
        sb.append(' ')
    }

    sb.append(remoteFilePath)

    return sb.toString()
}

class SimpleOutputReceiver: MultiLineReceiver() {
    private val buffer = StringBuffer()

    fun output() = buffer.toString()

    override fun processNewLines(lines: Array<out String>?) {
        lines?.forEach {
            buffer.append(it)
        }
    }
    override fun isCancelled() = false
}
