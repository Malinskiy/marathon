package com.malinskiy.marathon.cli.config

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.malinskiy.marathon.cli.args.FileAndroidConfiguration
import com.malinskiy.marathon.cli.args.FileConfiguration
import com.malinskiy.marathon.cli.args.FileIOSConfiguration
import com.malinskiy.marathon.cli.args.environment.EnvironmentReader
import com.malinskiy.marathon.exceptions.ConfigurationException
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.execution.strategy.ExecutionStrategy
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.vendor.VendorConfiguration
import org.apache.commons.text.StringSubstitutor
import org.apache.commons.text.lookup.StringLookupFactory
import java.io.File

private val logger = MarathonLogging.logger {}

class ConfigFactory(private val mapper: ObjectMapper) {
    private val environmentVariableSubstitutor = StringSubstitutor(StringLookupFactory.INSTANCE.environmentVariableStringLookup())

    fun create(marathonfile: File, environmentReader: EnvironmentReader): Configuration {
        logger.info { "Checking $marathonfile config" }

        if (!marathonfile.isFile) {
            logger.error { "No config ${marathonfile.absolutePath} present" }
            throw ConfigurationException("No config ${marathonfile.absolutePath} present")
        }

        val config = readConfigFile(marathonfile) ?: throw ConfigurationException("Invalid config format")

        val fileVendorConfiguration = config.vendorConfiguration
        val vendorConfiguration = when (fileVendorConfiguration) {
            is FileIOSConfiguration -> fileVendorConfiguration.toIOSConfiguration(
                marathonfile.canonicalFile.parentFile
            )
            is FileAndroidConfiguration -> {
                fileVendorConfiguration.toAndroidConfiguration(environmentReader.read().androidSdk)
            }
            else -> throw ConfigurationException("No vendor config present in ${marathonfile.absolutePath}")
        }

        return Configuration(
            name = config.name,
            outputDir = config.outputDir,

            analyticsConfiguration = config.analyticsConfiguration,
            poolingStrategy = config.poolingStrategy,
            shardingStrategy = config.shardingStrategy,
            sortingStrategy = config.sortingStrategy,
            batchingStrategy = config.batchingStrategy,
            flakinessStrategy = config.flakinessStrategy,
            retryStrategy = config.retryStrategy,
            filteringConfiguration = config.filteringConfiguration,
            ignoreFailures = config.ignoreFailures,
            isCodeCoverageEnabled = config.isCodeCoverageEnabled,
            fallbackToScreenshots = config.fallbackToScreenshots,
            executionStrategy = parseExecutionStrategy(config),
            uncompletedTestRetryQuota = config.uncompletedTestRetryQuota,
            testClassRegexes = config.testClassRegexes,
            includeSerialRegexes = config.includeSerialRegexes,
            excludeSerialRegexes = config.excludeSerialRegexes,
            testBatchTimeoutMillis = config.testBatchTimeoutMillis,
            testOutputTimeoutMillis = config.testOutputTimeoutMillis,
            debug = config.debug,
            screenRecordingPolicy = config.screenRecordingPolicy,
            vendorConfiguration = vendorConfiguration as VendorConfiguration,
            analyticsTracking = config.analyticsTracking,
            deviceInitializationTimeoutMillis = config.deviceInitializationTimeoutMillis
        )
    }

    private fun parseExecutionStrategy(config: FileConfiguration): ExecutionStrategy? {
        val configStrictMode = config.strictMode
        val configExecutionStrategy = config.executionStrategy
        var executionStrategy: ExecutionStrategy? = null
        if (configStrictMode != null) {
            executionStrategy = when {
                configStrictMode -> ExecutionStrategy.ALL_SUCCESS
                else -> ExecutionStrategy.ANY_SUCCESS
            }
            logger.warn("""
            |You are using deprecated strict_mode configuration: [true, false].
            |This API will be removed in 0.7.0.
            |Replace `true` with `execution_strategy: all_success`, `false` with `execution_strategy: any_success`. See documentation for details."""
            )
        }
        if (configExecutionStrategy != null) {
            executionStrategy = configExecutionStrategy
        }

        return executionStrategy
    }

    private fun readConfigFile(configFile: File): FileConfiguration? {
        val configWithEnvironmentVariablesReplaced = environmentVariableSubstitutor.replace(configFile.readText())
        try {
            return mapper.readValue(configWithEnvironmentVariablesReplaced, FileConfiguration::class.java)
        } catch (e: JsonProcessingException) {
            logger.error(e) { "Error parsing config file ${configFile.absolutePath}" }
            throw ConfigurationException(e)
        }
    }
}
