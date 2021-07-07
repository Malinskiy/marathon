package com.malinskiy.marathon.android

import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.test.MetaProperty
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.shouldEqual
import org.junit.jupiter.api.Test
import java.io.File
import com.malinskiy.marathon.test.Test as MarathonTest

class AndroidTestParserTest {
    private val testBundleIdentifier = AndroidTestBundleIdentifier()
    private val parser = AndroidTestParser(testBundleIdentifier)
    private val apkFile = File(javaClass.classLoader.getResource("android_test_1.apk").file)
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
        vendorConfiguration = AndroidConfiguration(
            File(""),
            applicationOutput = File(""),
            testApplicationOutput = apkFile,
            implementationModules = emptyList()
        ),
        analyticsTracking = false,
        deviceInitializationTimeoutMillis = null
    )

    @Test
    fun `should return proper list of test methods`() {
        val extractedTests = runBlocking {
            parser.extract(configuration)
        }
        extractedTests shouldEqual listOf(
            MarathonTest(
                "com.example", "MainActivityTest", "testText",
                listOf(
                    MetaProperty("org.junit.Test"),
                    MetaProperty("kotlin.Metadata"),
                    MetaProperty("org.junit.runner.RunWith")
                )
            )
        )
    }
}
