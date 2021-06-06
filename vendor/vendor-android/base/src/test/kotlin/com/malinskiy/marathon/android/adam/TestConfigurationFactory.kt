package com.malinskiy.marathon.android.adam

import com.malinskiy.marathon.android.AndroidConfiguration
import com.malinskiy.marathon.android.adam.di.adamModule
import com.malinskiy.marathon.android.configuration.AllureConfiguration
import com.malinskiy.marathon.android.configuration.FileSyncConfiguration
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.execution.strategy.StrictMode
import java.io.File

object TestConfigurationFactory {
    fun create(
        autoGrantPermission: Boolean = false,
        installOptions: String = "",
        fileSyncConfiguration: FileSyncConfiguration = FileSyncConfiguration(),
        allureConfiguration: AllureConfiguration = AllureConfiguration(),
        isCodeCoverageEnabled: Boolean = false,
    ): Configuration {
        return Configuration(
            name = "",
            outputDir = File(""),
            analyticsConfiguration = null,
            poolingStrategy = null,
            shardingStrategy = null,
            sortingStrategy = null,
            batchingStrategy = null,
            flakinessStrategy = null,
            retryStrategy = null,
            filteringConfiguration = null,
            ignoreFailures = null,
            isCodeCoverageEnabled = isCodeCoverageEnabled,
            fallbackToScreenshots = null,
            strictMode = StrictMode.ANY_SUCCESS,
            uncompletedTestRetryQuota = null,
            testClassRegexes = null,
            includeSerialRegexes = null,
            excludeSerialRegexes = null,
            testBatchTimeoutMillis = null,
            testOutputTimeoutMillis = null,
            debug = false,
            screenRecordingPolicy = null,
            vendorConfiguration = AndroidConfiguration(
                androidSdk = File(""),
                applicationOutput = File(javaClass.classLoader.getResource("apk/app-debug.apk").file),
                testApplicationOutput = File(javaClass.classLoader.getResource("apk/app-debug-androidTest.apk").file),
                implementationModules = listOf(adamModule),
                autoGrantPermission = autoGrantPermission,
                installOptions = installOptions,
                fileSyncConfiguration = fileSyncConfiguration,
                allureConfiguration = allureConfiguration
            ),
            analyticsTracking = false,
            deviceInitializationTimeoutMillis = null,
        )
    }
}
