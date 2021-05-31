package com.malinskiy.marathon.android.ddmlib

import com.android.ddmlib.AdbCommandRejectedException
import com.android.ddmlib.CollectingOutputReceiver
import com.android.ddmlib.IDevice
import com.android.ddmlib.InstallException
import com.android.ddmlib.RawImage
import com.android.ddmlib.ShellCommandUnresponsiveException
import com.android.ddmlib.SyncException
import com.android.ddmlib.TimeoutException
import com.android.ddmlib.logcat.LogCatMessage
import com.android.ddmlib.logcat.LogCatReceiverTask
import com.malinskiy.marathon.analytics.internal.pub.Track
import com.malinskiy.marathon.android.ADB_SCREEN_RECORD_TIMEOUT_MILLIS
import com.malinskiy.marathon.android.AndroidAppInstaller
import com.malinskiy.marathon.android.BaseAndroidDevice
import com.malinskiy.marathon.android.VideoConfiguration
import com.malinskiy.marathon.android.configuration.AndroidConfiguration
import com.malinskiy.marathon.android.configuration.SerialStrategy
import com.malinskiy.marathon.android.ddmlib.shell.receiver.CollectingShellOutputReceiver
import com.malinskiy.marathon.android.ddmlib.sync.NoOpSyncProgressMonitor
import com.malinskiy.marathon.android.exception.CommandRejectedException
import com.malinskiy.marathon.android.exception.TransferException
import com.malinskiy.marathon.android.executor.listeners.line.LineListener
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.NetworkState
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.execution.TestBatchResults
import com.malinskiy.marathon.execution.progress.ProgressReporter
import com.malinskiy.marathon.test.TestBatch
import com.malinskiy.marathon.time.Timer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeout
import java.awt.image.BufferedImage
import java.io.IOException
import java.time.Duration
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.resumeWithException

class DdmlibAndroidDevice(
    val ddmsDevice: IDevice,
    adbSerial: String,
    configuration: Configuration,
    androidConfiguration: AndroidConfiguration,
    track: Track,
    timer: Timer,
    serialStrategy: SerialStrategy
) : BaseAndroidDevice(adbSerial, serialStrategy, configuration, androidConfiguration, track, timer) {

    override suspend fun setup() {
        super.setup()
    }

    override suspend fun getProperty(name: String, cached: Boolean): String? = ddmsDevice.getProperty(name)

    private val receiver = LogCatReceiverTask(ddmsDevice)

    private var logcatThread = thread(name = "LogCatLogger-${ddmsDevice.serialNumber}") {
        receiver.run()
    }

    private val logcatListeners = mutableListOf<LineListener>()
    private val listener: (List<LogCatMessage>) -> Unit = {
        it.forEach { msg ->
            synchronized(logcatListeners) {
                logcatListeners.forEach { listener ->
                    listener.onLine("${msg.timestamp} ${msg.pid}-${msg.tid}/${msg.appName} ${msg.logLevel.priorityLetter}/${msg.tag}: ${msg.message}")
                }
            }
        }
    }

    override fun addLogcatListener(listener: LineListener) {
        synchronized(logcatListeners) {
            logcatListeners.add(listener)
        }
    }

    override fun removeLogcatListener(listener: LineListener) {
        synchronized(logcatListeners) {
            logcatListeners.remove(listener)
        }
    }

    override suspend fun pullFile(remoteFilePath: String, localFilePath: String) {
        try {
            ddmsDevice.pullFile(remoteFilePath, localFilePath)
        } catch (e: SyncException) {
            throw TransferException(e)
        }
    }

    override suspend fun pushFile(localFilePath: String, remoteFilePath: String, verify: Boolean) {
        try {
            ddmsDevice.pushFile(localFilePath, remoteFilePath)
        } catch (e: SyncException) {
            throw TransferException(e)
        }
    }

    override suspend fun pullFolder(remoteFolderPath: String, localFolderPath: String) {
        val parts = remoteFolderPath.split('/').filter { it.isNotBlank() }

        try {
            val listingService = ddmsDevice.fileListingService
            var root = listingService.root
            var children = listingService.getChildrenSync(root)

            for (part in parts) {
                root = children.find { it.pathSegments.last() == part }
                    ?: throw TransferException("Remote folder $remoteFolderPath doesn't exist on $serialNumber")

                children = listingService.getChildrenSync(root)
            }

            with(ddmsDevice.syncService) {
                pull(children, localFolderPath, NoOpSyncProgressMonitor())
                close()
            }

        } catch (e: Exception) {
            logger.warn { "Can't pull the folder $remoteFolderPath from device $serialNumber" }
            throw TransferException(e)
        }
    }

    override suspend fun executeShellCommand(command: String, errorMessage: String): String? {
        try {
            val outputReceiver = SimpleOutputReceiver()
            ddmsDevice.safeExecuteShellCommand(command, outputReceiver)
            return outputReceiver.output()
        } catch (e: TimeoutException) {
            logger.error(errorMessage, e)
            return null
        } catch (e: AdbCommandRejectedException) {
            logger.error(errorMessage, e)
            return null
        } catch (e: ShellCommandUnresponsiveException) {
            logger.error(errorMessage, e)
            return null
        } catch (e: IOException) {
            logger.error(errorMessage, e)
            return null
        }
    }

    override suspend fun getScreenshot(timeout: Duration): BufferedImage? {
        return try {
            val rawImage = ddmsDevice.getScreenshot(timeout.toMillis(), TimeUnit.MILLISECONDS)
            bufferedImageFrom(rawImage)
        } catch (e: TimeoutException) {
            throw java.util.concurrent.TimeoutException(e.message)
        } catch (e: AdbCommandRejectedException) {
            throw CommandRejectedException(e)
        }
    }

    override suspend fun safeStartScreenRecorder(
        remoteFilePath: String,
        options: VideoConfiguration
    ) {
        val outputReceiver = CollectingOutputReceiver()
        withTimeoutOrInterrupt(ADB_SCREEN_RECORD_TIMEOUT_MILLIS) {
            ddmsDevice.safeStartScreenRecorder(
                remoteFilePath,
                options,
                outputReceiver
            )
        }
        logger.debug { "screenrecord output:\n ${outputReceiver.output}" }
    }

    private fun bufferedImageFrom(rawImage: RawImage): BufferedImage {
        val image = BufferedImage(rawImage.width, rawImage.height, BufferedImage.TYPE_INT_ARGB)

        var index = 0
        val bytesPerPixel = rawImage.bpp shr 3
        for (y in 0 until rawImage.height) {
            for (x in 0 until rawImage.width) {
                image.setRGB(x, y, rawImage.getARGB(index) or -0x1000000)
                index += bytesPerPixel
            }
        }
        return image
    }

    private val dispatcher by lazy {
        newFixedThreadPoolContext(2, "AndroidDevice - execution - ${ddmsDevice.serialNumber}")
    }

    override val coroutineContext: CoroutineContext = dispatcher

    override suspend fun installPackage(absolutePath: String, reinstall: Boolean, optionalParams: String): String? {
        return try {
            ddmsDevice.safeInstallPackage(absolutePath, reinstall, optionalParams)
        } catch (e: InstallException) {
            throw com.malinskiy.marathon.android.exception.InstallException(e)
        }
    }

    override val networkState: NetworkState
        get() = when (ddmsDevice.isOnline) {
            true -> NetworkState.CONNECTED
            else -> NetworkState.DISCONNECTED
        }

    override val healthy: Boolean
        get() = when (ddmsDevice.state) {
            IDevice.DeviceState.ONLINE -> true
            else -> false
        }

    override suspend fun execute(
        configuration: Configuration,
        devicePoolId: DevicePoolId,
        testBatch: TestBatch,
        deferred: CompletableDeferred<TestBatchResults>,
        progressReporter: ProgressReporter
    ) {

        val deferredResult = async {
            val listener = createExecutionListeners(configuration, devicePoolId, testBatch, deferred, progressReporter)
            AndroidDeviceTestRunner(this@DdmlibAndroidDevice)
                .execute(configuration, testBatch, listener)
        }
        deferredResult.await()
    }

    override suspend fun prepare(configuration: Configuration) {
        track.trackDevicePreparing(this) {
            val deferred = async {
                AndroidAppInstaller(configuration).prepareInstallation(this@DdmlibAndroidDevice)
                fileManager.removeRemoteDirectory()
                fileManager.createRemoteDirectory()
                clearLogcat()
                receiver.addLogCatListener(listener)

            }
            deferred.await()
        }
    }

    override fun dispose() {
        logcatThread.interrupt()
        receiver.removeLogCatListener(listener)
        dispatcher.close()
    }

    override suspend fun safeUninstallPackage(appPackage: String, keepData: Boolean): String? {
        return try {
            ddmsDevice.safeUninstallPackage(appPackage, keepData)
        } catch (e: InstallException) {
            throw com.malinskiy.marathon.android.exception.InstallException(e)
        }
    }

    override suspend fun safeClearPackage(packageName: String): String? = ddmsDevice.safeClearPackage(packageName)

    override suspend fun safeExecuteShellCommand(command: String, errorMessage: String): String? {
        val receiver = CollectingShellOutputReceiver()
        ddmsDevice.safeExecuteShellCommand(command, receiver)
        return receiver.output()
    }

    override suspend fun criticalExecuteShellCommand(command: String, errorMessage: String): String {
        val receiver = CollectingShellOutputReceiver()
        try {
            ddmsDevice.safeExecuteShellCommand(command, receiver)
        } catch (e: Exception) {
            throw CommandRejectedException(errorMessage)
        }
        return receiver.output()
    }

    /**
     * The only way to interrupt the current screen recording is to interrupt the thread
     * This is undesirable with coroutines and requires another thread to execute the process.
     * We can then interrupt it from the coroutine
     */
    private val externalThreadPool: ExecutorService = Executors.newCachedThreadPool()
    private suspend fun <T> withTimeoutOrInterrupt(timeMillis: Long, block: () -> T) {
        withTimeout(timeMillis) {
            suspendCancellableCoroutine<Unit> { cont ->
                val future = externalThreadPool.submit {
                    try {
                        block()
                        cont.resumeWith(Result.success(Unit))
                    } catch (e: InterruptedException) {
                        cont.resumeWithException(CancellationException())
                    } catch (e: Throwable) {
                        cont.resumeWithException(e)
                    }
                }
                cont.invokeOnCancellation {
                    future.cancel(true)
                }
            }
        }
    }
}
