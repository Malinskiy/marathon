package com.malinskiy.marathon.android.executor.listeners.filesync

import com.malinskiy.marathon.android.AndroidDevice
import com.malinskiy.marathon.android.configuration.AggregationMode
import com.malinskiy.marathon.android.configuration.FileSyncConfiguration
import com.malinskiy.marathon.android.configuration.FileSyncEntry
import com.malinskiy.marathon.android.executor.listeners.NoOpTestRunListener
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.toDeviceInfo
import com.malinskiy.marathon.io.FileManager
import com.malinskiy.marathon.io.FolderType
import com.malinskiy.marathon.log.MarathonLogging
import java.io.File

class FileSyncTestRunListener(
    private val pool: DevicePoolId,
    private val device: AndroidDevice,
    private val configuration: FileSyncConfiguration,
    private val fileManager: FileManager
) : NoOpTestRunListener() {

    private val logger = MarathonLogging.logger("FileSyncTestRunListener")

    override suspend fun beforeTestRun() {
        super.beforeTestRun()
        configuration.pull.forEach { entry ->
            val fullPath = getRemoteFullPath(entry)
            device.fileManager.removeRemotePath(fullPath, recursive = true)
            device.fileManager.createRemoteDirectory(fullPath)
        }
    }

    override suspend fun testRunEnded(elapsedTime: Long, runMetrics: Map<String, String>) {
        super.testRunEnded(elapsedTime, runMetrics)
        configuration.pull.forEach { entry ->
            val fullPath = getRemoteFullPath(entry)

            val localFolder = when (entry.aggregationMode) {
                AggregationMode.DEVICE_AND_POOL -> fileManager.createFolder(FolderType.DEVICE_FILES, pool, device.toDeviceInfo())
                AggregationMode.DEVICE -> fileManager.createFolder(FolderType.DEVICE_FILES, device.toDeviceInfo())
                AggregationMode.POOL -> fileManager.createFolder(FolderType.DEVICE_FILES, pool)
                AggregationMode.TEST_RUN -> fileManager.createFolder(FolderType.DEVICE_FILES)
            }

            val basename = entry.relativePath.removeSuffix("/").substringAfterLast('/')
            val subfolder = File(localFolder, basename).apply { mkdirs() }
            logger.debug { "Pulling into ${subfolder.absolutePath}" }
            device.safePullFolder(fullPath, subfolder.absolutePath)
        }
    }

    private fun getRemoteFullPath(entry: FileSyncEntry): String {
        val externalStorageMount = device.externalStorageMount.removeSuffix("/")
        val relativePath = entry.relativePath.removePrefix("/")
        return "$externalStorageMount/$relativePath"
    }
}
