package com.malinskiy.marathon.cli.args

import com.fasterxml.jackson.annotation.JsonProperty
import com.malinskiy.marathon.cli.config.ConfigurationException
import com.malinskiy.marathon.ios.IOSConfiguration
import java.io.File

data class FileIOSConfiguration(
        @JsonProperty("xctestrunPath") val xctestrunPath: File?,
        @JsonProperty("derivedDataDir") val derivedDataDir: File?,
        @JsonProperty("remoteUsername") val remoteUsername: String,
        @JsonProperty("remotePublicKey") val remotePublicKey: File,
        @JsonProperty("sourceRoot") val sourceRoot: File?) : FileVendorConfiguration {

    fun toIOSConfiguration(xctestrunPathOverride: File? = null,
                           sourceRootOverride: File? = null): IOSConfiguration {

        if (derivedDataDir == null) {
            throw ConfigurationException("A path to derived data folder is required")
        }

        val finalXCTestRunPath = xctestrunPathOverride
                ?: xctestrunPath
                ?: throw ConfigurationException("No xctestrunPath specified")

        val optionalSourceRoot = sourceRootOverride ?: sourceRoot

        return if (optionalSourceRoot == null) {
            IOSConfiguration(finalXCTestRunPath, derivedDataDir, remoteUsername, remotePublicKey)
        } else {
            IOSConfiguration(finalXCTestRunPath, derivedDataDir, remoteUsername, remotePublicKey, optionalSourceRoot)
        }
    }
}