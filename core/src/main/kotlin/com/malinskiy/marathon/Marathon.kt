package com.malinskiy.marathon

import com.google.gson.Gson
import com.malinskiy.marathon.analytics.AnalyticsFactory
import com.malinskiy.marathon.device.DeviceProvider
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.execution.Scheduler
import com.malinskiy.marathon.execution.TestParser
import com.malinskiy.marathon.execution.progress.ProgressReporter
import com.malinskiy.marathon.io.FileManager
import com.malinskiy.marathon.report.CompositeSummaryPrinter
import com.malinskiy.marathon.report.SummaryCompiler
import com.malinskiy.marathon.report.SummaryPrinter
import com.malinskiy.marathon.report.debug.timeline.TimelineSummaryPrinter
import com.malinskiy.marathon.report.debug.timeline.TimelineSummarySerializer
import com.malinskiy.marathon.report.html.HtmlSummaryPrinter
import com.malinskiy.marathon.report.internal.DeviceInfoReporter
import com.malinskiy.marathon.report.internal.TestResultReporter
import com.malinskiy.marathon.test.Test
import kotlinx.coroutines.experimental.runBlocking
import mu.KotlinLogging
import java.util.ServiceLoader
import java.util.concurrent.TimeUnit
import kotlin.system.measureTimeMillis

private val log = KotlinLogging.logger {}

class Marathon(val configuration: Configuration) {

    private val fileManager = FileManager(configuration.outputDir)
    private val gson = Gson()

    private val testResultReporter = TestResultReporter(fileManager, gson)
    private val deviceInfoReporter = DeviceInfoReporter(fileManager, gson)
    private val analyticsFactory = AnalyticsFactory(configuration, fileManager, deviceInfoReporter, testResultReporter)

    private val summaryCompiler = SummaryCompiler(deviceInfoReporter, testResultReporter, configuration)

    private fun loadSummaryPrinter(): SummaryPrinter {
        val outputDir = configuration.outputDir
        val htmlSummaryPrinter = HtmlSummaryPrinter(gson, outputDir)
        if (configuration.debug) {
            return CompositeSummaryPrinter(listOf(
                    htmlSummaryPrinter,
                    TimelineSummaryPrinter(TimelineSummarySerializer(testResultReporter), gson, outputDir)
            ))
        }
        return htmlSummaryPrinter
    }

    private fun loadDeviceProvider(): DeviceProvider {
        val deviceProvider = ServiceLoader.load(DeviceProvider::class.java).first()
        deviceProvider.initialize(configuration.vendorConfiguration)
        return deviceProvider
    }

    private fun loadTestParser(): TestParser {
        val loader = ServiceLoader.load(TestParser::class.java)
        return loader.first()
    }

    fun run(): Boolean = runBlocking {
        val testParser = loadTestParser()
        val deviceProvider = loadDeviceProvider()
        val analytics = analyticsFactory.create()

        val parsedTests = testParser.extract(configuration.testApplicationOutput)
        val tests = applyTestFilters(parsedTests)

        println { "${tests.size} test methods after filters" }
        val progressReporter = ProgressReporter()
        val scheduler = Scheduler(deviceProvider, analytics, configuration, tests, progressReporter)

        if (configuration.outputDir.exists()) {
            log.info { "Output ${configuration.outputDir} already exists" }
            configuration.outputDir.deleteRecursively()
        }
        configuration.outputDir.mkdirs()

        val timeMillis = measureTimeMillis {
            scheduler.execute()
        }

        val pools = scheduler.getPools()
        if (!pools.isEmpty()) {
            val summaryPrinter = loadSummaryPrinter()
            val summary = summaryCompiler.compile(scheduler.getPools())
            summaryPrinter.print(summary)
        }

        val hours = TimeUnit.MICROSECONDS.toHours(timeMillis)
        val minutes = TimeUnit.MICROSECONDS.toMinutes(timeMillis)
        val seconds = TimeUnit.MICROSECONDS.toSeconds(timeMillis)

        log.info { "Total time: ${hours}H ${minutes}m ${seconds}s" }
        analytics.terminate()
        deviceProvider.terminate()

        progressReporter.aggregateResult()
    }

    private fun applyTestFilters(parsedTests: List<Test>): List<Test> {
        var tests = parsedTests.filter { test ->
            configuration.testClassRegexes.all { it.matches(test.clazz) }
        }
        configuration.filteringConfiguration.whitelist.forEach { tests = it.filter(tests) }
        configuration.filteringConfiguration.blacklist.forEach { tests = it.filterNot(tests) }
        return tests
    }
}
