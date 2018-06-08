package com.malinskiy.marathon.execution

import com.malinskiy.marathon.aktor.Aktor
import com.malinskiy.marathon.analytics.Analytics
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.healthCheck
import com.malinskiy.marathon.test.Test
import kotlinx.coroutines.experimental.launch

/**
 * The logic of scheduler
 *
 * 1. Pooling:      Create pools of devices
 * 2. Sharding:     Define sharding (creates device-test association)
 * 3. Flakiness:    Add known retries to tests in all shards
 * 4. Sorting:      Sort all tests
 * 5. Batching:     TestBatch into manageable chunks
 * 6. Retries:      Retry if something fails and we didn't account for it in the flakiness
 */

class Scheduler(private val deviceProvider: DeviceProvider,
                private val analytics: Analytics,
                private val configuration: Configuration,
                private val shard: TestShard) {

    companion object {
        private const val DEFAULT_INITIAL_DELAY_MILLIS = 10_000L
    }

    private val pools = mutableMapOf<DevicePoolId, Aktor<DevicePoolMessage>>()
    private val poolingStrategy = configuration.poolingStrategy

    suspend fun execute() {
        subscribeOnDevices()
        healthCheck(startDelay = DEFAULT_INITIAL_DELAY_MILLIS) {
            !pools.values.all { it.isClosedForSend }
        }.join()
    }

    fun getPools(): List<DevicePoolId> {
        return pools.keys.toList()
    }

    private fun subscribeOnDevices() {
        launch {
            for (msg in deviceProvider.subscribe()) {
                when (msg) {
                    is DeviceProvider.DeviceEvent.DeviceConnected -> {
                        onDeviceConnected(msg)
                    }
                    is DeviceProvider.DeviceEvent.DeviceDisconnected -> {
                        onDeviceDisconnected(msg)
                    }
                }
            }
        }
    }

    private suspend fun onDeviceDisconnected(item: DeviceProvider.DeviceEvent.DeviceDisconnected) {
        pools.values.forEach {
            it.send(DevicePoolMessage.RemoveDevice(item.device))
        }
    }

    private suspend fun onDeviceConnected(item: DeviceProvider.DeviceEvent.DeviceConnected) {
        val device = item.device
        val poolId = poolingStrategy.associate(device)
        pools.computeIfAbsent(poolId, { id -> DevicePoolAktor(id, configuration, analytics, shard) })
        pools[poolId]?.send(DevicePoolMessage.AddDevice(device))
        analytics.trackDeviceConnected(poolId, device)
    }
}
