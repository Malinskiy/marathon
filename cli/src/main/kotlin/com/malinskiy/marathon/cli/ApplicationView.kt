package com.malinskiy.marathon.cli

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.malinskiy.marathon.BuildConfig
import com.malinskiy.marathon.Marathon
import com.malinskiy.marathon.cli.args.MarathonCliConfiguration
import com.malinskiy.marathon.cli.args.environment.SystemEnvironmentReader
import com.malinskiy.marathon.cli.config.ConfigFactory
import com.malinskiy.marathon.cli.config.DeserializeModule
import com.malinskiy.marathon.cli.config.time.InstantTimeProviderImpl
import com.malinskiy.marathon.config.AppType
import com.malinskiy.marathon.di.marathonStartKoin
import com.malinskiy.marathon.exceptions.ExceptionsReporterFactory
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.usageanalytics.TrackActionType
import com.malinskiy.marathon.usageanalytics.UsageAnalytics
import com.malinskiy.marathon.usageanalytics.tracker.Event
import com.xenomachina.argparser.ArgParser
import com.xenomachina.argparser.SystemExitException
import com.xenomachina.argparser.mainBody
import org.koin.core.context.stopKoin

private val logger = MarathonLogging.logger {}

fun main(args: Array<String>): Unit = mainBody(
    programName = "marathon v${BuildConfig.VERSION}"
) {
    ArgParser(args).parseInto(::MarathonCliConfiguration).run {
        logger.info { "Starting marathon v${BuildConfig.VERSION}" }
        val bugsnagExceptionsReporter = ExceptionsReporterFactory.get(bugsnagReporting)
        try {
            bugsnagExceptionsReporter.start(AppType.CLI)
            val mapper = ObjectMapper(YAMLFactory().disable(YAMLGenerator.Feature.USE_NATIVE_TYPE_ID))
                .registerModule(DeserializeModule(InstantTimeProviderImpl()))
                .registerModule(KotlinModule())
                .registerModule(JavaTimeModule())
            val configuration = ConfigFactory(mapper).create(
                marathonfile = marathonfile,
                environmentReader = SystemEnvironmentReader()
            )

            val application = marathonStartKoin(configuration)
            val marathon: Marathon = application.koin.get()

            UsageAnalytics.enable = this.analyticsTracking
            UsageAnalytics.USAGE_TRACKER.trackEvent(Event(TrackActionType.RunType, "cli"))
            val success = marathon.run()

            val shouldReportFailure = !configuration.ignoreFailures
            if (!success && shouldReportFailure) {
                throw SystemExitException("Test run failed", 1)
            } else {
                throw SystemExitException("Test run finished", 0)
            }
        } finally {
            stopKoin()
            bugsnagExceptionsReporter.end()
        }
    }
}
