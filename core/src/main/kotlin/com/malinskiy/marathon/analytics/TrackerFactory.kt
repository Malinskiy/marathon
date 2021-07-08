package com.malinskiy.marathon.analytics

import com.google.gson.Gson
import com.malinskiy.marathon.analytics.external.graphite.BasicGraphiteClient
import com.malinskiy.marathon.analytics.external.graphite.GraphiteTracker
import com.malinskiy.marathon.analytics.external.influx.InfluxDbProvider
import com.malinskiy.marathon.analytics.external.influx.InfluxDbTracker
import com.malinskiy.marathon.analytics.internal.pub.Track
import com.malinskiy.marathon.analytics.internal.sub.DelegatingTrackerInternal
import com.malinskiy.marathon.analytics.internal.sub.ExecutionReportGenerator
import com.malinskiy.marathon.analytics.internal.sub.TrackerInternal
import com.malinskiy.marathon.execution.AnalyticsConfiguration.GraphiteConfiguration
import com.malinskiy.marathon.execution.AnalyticsConfiguration.InfluxDbConfiguration
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.io.FileManager
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.report.allure.AllureReporter
import com.malinskiy.marathon.report.device.DeviceInfoJsonReporter
import com.malinskiy.marathon.report.html.HtmlSummaryReporter
import com.malinskiy.marathon.report.junit.JUnitReporter
import com.malinskiy.marathon.report.raw.RawJsonReporter
import com.malinskiy.marathon.report.stdout.StdoutReporter
import com.malinskiy.marathon.report.test.TestJsonReporter
import com.malinskiy.marathon.report.timeline.TimelineReporter
import com.malinskiy.marathon.report.timeline.TimelineSummaryProvider
import com.malinskiy.marathon.time.Timer
import java.io.File

internal class TrackerFactory(
    private val configuration: Configuration,
    private val fileManager: FileManager,
    private val gson: Gson,
    private val timer: Timer,
    private val track: Track
) {

    val log = MarathonLogging.logger("TrackerFactory")

    fun create(): TrackerInternal {
        val defaultTrackers = mutableListOf<TrackerInternal>(createExecutionReportGenerator())

        val analyticsConfiguration = configuration.analyticsConfiguration
        val analyticsTracker = when (analyticsConfiguration) {
            is InfluxDbConfiguration -> createInfluxDbTracker(analyticsConfiguration)
            is GraphiteConfiguration -> createGraphiteTracker(analyticsConfiguration)
            else -> null
        }
        if (analyticsTracker != null) {
            defaultTrackers.add(analyticsTracker)
        }

        val delegatingTrackerInternal = DelegatingTrackerInternal(defaultTrackers)
        val mappingTracker = MappingTracker(delegatingTrackerInternal)
        track.add(mappingTracker)

        return delegatingTrackerInternal
    }

    private fun createInfluxDbTracker(config: InfluxDbConfiguration): InfluxDbTracker? {
        val db = try {
            InfluxDbProvider(config).createDb()
        } catch (e: Exception) {
            log.warn(e) { "Failed to reach InfluxDB at ${config.url}" }
            null
        }
        return db?.let { InfluxDbTracker(it, config.dbName, config.retentionPolicyConfiguration.name) }
    }

    private fun createGraphiteTracker(config: GraphiteConfiguration): GraphiteTracker {
        return GraphiteTracker(BasicGraphiteClient(config.host, config.port ?: 2003, config.prefix))
    }

    private fun createExecutionReportGenerator(): ExecutionReportGenerator {
        return ExecutionReportGenerator(
            listOf(
                DeviceInfoJsonReporter(fileManager, gson),
                JUnitReporter(configuration.outputDir),
                TimelineReporter(TimelineSummaryProvider(), gson, configuration.outputDir),
                RawJsonReporter(fileManager, gson),
                TestJsonReporter(fileManager, gson),
                AllureReporter(configuration, File(configuration.outputDir, "allure-results")),
                HtmlSummaryReporter(gson, configuration.outputDir, configuration),
                StdoutReporter(timer)
            )
        )
    }
}
