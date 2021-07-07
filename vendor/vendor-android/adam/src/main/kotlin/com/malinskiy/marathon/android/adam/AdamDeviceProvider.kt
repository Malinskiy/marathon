package com.malinskiy.marathon.android.adam

import com.malinskiy.adam.AndroidDebugBridgeClient
import com.malinskiy.adam.AndroidDebugBridgeClientFactory
import com.malinskiy.adam.interactor.StartAdbInteractor
import com.malinskiy.adam.request.device.AsyncDeviceMonitorRequest
import com.malinskiy.adam.request.device.Device
import com.malinskiy.adam.request.device.ListDevicesRequest
import com.malinskiy.adam.request.misc.GetAdbServerVersionRequest
import com.malinskiy.marathon.actor.unboundedChannel
import com.malinskiy.marathon.analytics.internal.pub.Track
import com.malinskiy.marathon.android.AndroidConfiguration
import com.malinskiy.marathon.android.AndroidTestBundleIdentifier
import com.malinskiy.marathon.android.exception.AdbStartException
import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.exceptions.NoDevicesException
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.time.Timer
import com.malinskiy.marathon.vendor.VendorConfiguration
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeoutOrNull
import java.net.ConnectException
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext

const val DEFAULT_WAIT_FOR_DEVICES_SLEEP_TIME = 500L

class AdamDeviceProvider(
    val configuration: Configuration,
    androidConfiguration: AndroidConfiguration,
    private val track: Track,
    private val timer: Timer
) : DeviceProvider, CoroutineScope {
    private val devices: MutableMap<String, ProvidedDevice> = ConcurrentHashMap()
    private val logger = MarathonLogging.logger("AdamDeviceProvider")

    private val channel: Channel<DeviceProvider.DeviceEvent> = unboundedChannel()
    override val coroutineContext: CoroutineContext by lazy { newFixedThreadPoolContext(1, "DeviceMonitor") }
    private val adbCommunicationContext: CoroutineContext by lazy { newFixedThreadPoolContext(androidConfiguration.threadingConfiguration.adbIoThreads, "AdbIOThreadPool") }
    private val setupSupervisor = SupervisorJob()
    private var providerJob: Job? = null

    override val deviceInitializationTimeoutMillis: Long = configuration.deviceInitializationTimeoutMillis

    private lateinit var client: AndroidDebugBridgeClient
    private lateinit var logcatManager: LogcatManager
    private lateinit var deviceEventsChannel: ReceiveChannel<List<Device>>
    private lateinit var testBundleIdentifier: AndroidTestBundleIdentifier
    private val deviceEventsChannelMutex = Mutex()
    private val deviceStateTracker = DeviceStateTracker()


    override suspend fun initialize(vendorConfiguration: VendorConfiguration) {
        if (vendorConfiguration !is AndroidConfiguration) {
            throw IllegalStateException("Invalid configuration $vendorConfiguration passed")
        }

        client = AndroidDebugBridgeClientFactory().apply {
            coroutineContext = adbCommunicationContext
            idleTimeout = vendorConfiguration.timeoutConfiguration.socketIdleTimeout
        }.build()
        logcatManager = LogcatManager(client)
        testBundleIdentifier = vendorConfiguration.testBundleIdentifier() as AndroidTestBundleIdentifier

        try {
            printAdbServerVersion()
        } catch (e: ConnectException) {
            val success = StartAdbInteractor().execute(androidHome = vendorConfiguration.androidSdk)
            if (!success) {

                throw AdbStartException()
            }
            printAdbServerVersion()
        }

        withTimeoutOrNull(vendorConfiguration.waitForDevicesTimeoutMillis) {
            while (client.execute(ListDevicesRequest()).isEmpty()) {
                delay(DEFAULT_WAIT_FOR_DEVICES_SLEEP_TIME)
            }
        } ?: throw NoDevicesException("No devices found")

        providerJob = launch {
                /**
                 * This allows us to survive `adb kill-server`
                 */
                while (isActive) {
                    deviceEventsChannelMutex.withLock {
                        deviceEventsChannel = client.execute(AsyncDeviceMonitorRequest(), this)
                    }
                    for (currentDeviceList in deviceEventsChannel) {
                        deviceStateTracker.update(currentDeviceList).forEach { update ->
                            val serial = update.first
                            val state = update.second
                            when (state) {
                                TrackingUpdate.CONNECTED -> {
                                    val device =
                                        AdamAndroidDevice(
                                            client,
                                            deviceStateTracker,
                                            logcatManager,
                                            testBundleIdentifier,
                                            serial,
                                            configuration,
                                            vendorConfiguration,
                                            track,
                                            timer,
                                            vendorConfiguration.serialStrategy
                                        )
                                    track.trackProviderDevicePreparing(device) {
                                    val job = launch(setupSupervisor) {
                                        device.setup()
                                        channel.send(DeviceProvider.DeviceEvent.DeviceConnected(device))
                                    }
                                    devices[serial] = ProvidedDevice(device, job)
                                }
                            }
                            TrackingUpdate.DISCONNECTED -> {
                                devices[serial]?.let { (device, job) ->
                                    if (job.isActive) {
                                        job.cancelAndJoin()
                                    }
                                    channel.send(DeviceProvider.DeviceEvent.DeviceDisconnected(device))
                                    device.dispose()
                                }
                            }
                            TrackingUpdate.NOTHING_TO_DO -> Unit
                        }
                        logger.debug { "Device $serial changed state to $state" }
                    }
                }
            }
        }
    }

    private suspend fun printAdbServerVersion() {
        val adbVersion = client.execute(GetAdbServerVersionRequest())
        logger.debug { "Android Debug Bridge version $adbVersion" }
    }

    override suspend fun terminate() {
        coroutineContext.cancel()
        setupSupervisor.cancel()
        providerJob?.cancel()
        channel.close()
        deviceEventsChannelMutex.withLock {
            deviceEventsChannel.cancel()
        }
        logcatManager.close()
        client.socketFactory.close()
    }

    override fun subscribe() = channel
}

data class ProvidedDevice(
    val device: AdamAndroidDevice,
    val setupJob: Job
)
