package com.malinskiy.marathon.ios

import com.malinskiy.marathon.execution.Configuration
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.shouldContainSame
import org.junit.jupiter.api.Test
import java.io.File
import com.malinskiy.marathon.test.Test as MarathonTest

class IOSTestParserTest {
    private val parser = IOSTestParser()
    private val sourceRoot =
        File(javaClass.classLoader.getResource("sample-xcworkspace/sample-appUITests").file)
    private val derivedDataDir =
        File(javaClass.classLoader.getResource("sample-xcworkspace/derived-data").file)
    private val xctestrunPath =
        File(javaClass.classLoader.getResource("sample-xcworkspace/derived-data/Build/Products/UITesting_iphonesimulator11.2-x86_64.xctestrun").file)
    private val configuration = Configuration(
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
        isCodeCoverageEnabled = null,
        fallbackToScreenshots = null,
        strictMode = null,
        uncompletedTestRetryQuota = null,
        testClassRegexes = null,
        includeSerialRegexes = null,
        excludeSerialRegexes = null,
        testBatchTimeoutMillis = null,
        testOutputTimeoutMillis = null,
        debug = null,
        screenRecordingPolicy = null,
        vendorConfiguration = IOSConfiguration(
            derivedDataDir = derivedDataDir,
            xctestrunPath = xctestrunPath,
            remoteUsername = "testuser",
            remotePrivateKey = File("/home/fakekey"),
            knownHostsPath = null,
            remoteRsyncPath = "/remote/rsync",
            sourceRoot = sourceRoot,
            debugSsh = false,
            alwaysEraseSimulators = true
        ),
        analyticsTracking = false,
        deviceInitializationTimeoutMillis = null
    )

    @Test
    fun `should return accurate list of tests`() {
        val extractedTests = runBlocking {
            parser.extract(configuration)
        }

        extractedTests shouldContainSame listOf(
            MarathonTest("sample-appUITests", "StoryboardTests", "testButton", emptyList()),
            MarathonTest("sample-appUITests", "StoryboardTests", "testLabel", emptyList()),
            MarathonTest("sample-appUITests", "MoreTests", "testPresentModal", emptyList()),
            MarathonTest("sample-appUITests", "CrashingTests", "testButton", emptyList()),
            MarathonTest("sample-appUITests", "FailingTests", "testAlwaysFailing", emptyList()),
            MarathonTest("sample-appUITests", "FlakyTests", "testTextFlaky", emptyList()),
            MarathonTest("sample-appUITests", "FlakyTests", "testTextFlaky1", emptyList()),
            MarathonTest("sample-appUITests", "FlakyTests", "testTextFlaky2", emptyList()),
            MarathonTest("sample-appUITests", "FlakyTests", "testTextFlaky3", emptyList()),
            MarathonTest("sample-appUITests", "FlakyTests", "testTextFlaky4", emptyList()),
            MarathonTest("sample-appUITests", "FlakyTests", "testTextFlaky5", emptyList()),
            MarathonTest("sample-appUITests", "FlakyTests", "testTextFlaky6", emptyList()),
            MarathonTest("sample-appUITests", "FlakyTests", "testTextFlaky7", emptyList()),
            MarathonTest("sample-appUITests", "FlakyTests", "testTextFlaky8", emptyList()),
            MarathonTest("sample-appUITests", "SlowTests", "testTextSlow", emptyList()),
            MarathonTest("sample-appUITests", "SlowTests", "testTextSlow1", emptyList()),
            MarathonTest("sample-appUITests", "SlowTests", "testTextSlow2", emptyList()),
            MarathonTest("sample-appUITests", "SlowTests", "testTextSlow3", emptyList()),
            MarathonTest("sample-appUITests", "SlowTests", "testTextSlow4", emptyList())
        )
    }
}
