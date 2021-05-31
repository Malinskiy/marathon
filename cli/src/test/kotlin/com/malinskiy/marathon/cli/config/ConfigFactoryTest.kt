package com.malinskiy.marathon.cli.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.malinskiy.marathon.android.ScreenRecordConfiguration
import com.malinskiy.marathon.android.ScreenshotConfiguration
import com.malinskiy.marathon.android.VideoConfiguration
import com.malinskiy.marathon.android.configuration.AllureConfiguration
import com.malinskiy.marathon.android.configuration.AndroidConfiguration
import com.malinskiy.marathon.android.configuration.SerialStrategy
import com.malinskiy.marathon.android.configuration.TimeoutConfiguration
import com.malinskiy.marathon.cli.args.EnvironmentConfiguration
import com.malinskiy.marathon.cli.args.environment.EnvironmentReader
import com.malinskiy.marathon.cli.config.time.InstantTimeProvider
import com.malinskiy.marathon.device.DeviceFeature
import com.malinskiy.marathon.exceptions.ConfigurationException
import com.malinskiy.marathon.execution.AnalyticsConfiguration
import com.malinskiy.marathon.execution.AnnotationDataFilter
import com.malinskiy.marathon.execution.AnnotationFilter
import com.malinskiy.marathon.execution.CompositionFilter
import com.malinskiy.marathon.execution.FullyQualifiedClassnameFilter
import com.malinskiy.marathon.execution.SimpleClassnameFilter
import com.malinskiy.marathon.execution.TestMethodFilter
import com.malinskiy.marathon.execution.TestPackageFilter
import com.malinskiy.marathon.execution.policy.ScreenRecordingPolicy
import com.malinskiy.marathon.execution.strategy.impl.batching.FixedSizeBatchingStrategy
import com.malinskiy.marathon.execution.strategy.impl.batching.IsolateBatchingStrategy
import com.malinskiy.marathon.execution.strategy.impl.flakiness.IgnoreFlakinessStrategy
import com.malinskiy.marathon.execution.strategy.impl.flakiness.ProbabilityBasedFlakinessStrategy
import com.malinskiy.marathon.execution.strategy.impl.pooling.OmniPoolingStrategy
import com.malinskiy.marathon.execution.strategy.impl.pooling.parameterized.AbiPoolingStrategy
import com.malinskiy.marathon.execution.strategy.impl.pooling.parameterized.ComboPoolingStrategy
import com.malinskiy.marathon.execution.strategy.impl.pooling.parameterized.ManufacturerPoolingStrategy
import com.malinskiy.marathon.execution.strategy.impl.pooling.parameterized.ModelPoolingStrategy
import com.malinskiy.marathon.execution.strategy.impl.pooling.parameterized.OperatingSystemVersionPoolingStrategy
import com.malinskiy.marathon.execution.strategy.impl.retry.NoRetryStrategy
import com.malinskiy.marathon.execution.strategy.impl.retry.fixedquota.FixedQuotaRetryStrategy
import com.malinskiy.marathon.execution.strategy.impl.sharding.CountShardingStrategy
import com.malinskiy.marathon.execution.strategy.impl.sharding.ParallelShardingStrategy
import com.malinskiy.marathon.execution.strategy.impl.sorting.ExecutionTimeSortingStrategy
import com.malinskiy.marathon.execution.strategy.impl.sorting.NoSortingStrategy
import com.malinskiy.marathon.execution.strategy.impl.sorting.SuccessRateSortingStrategy
import com.malinskiy.marathon.ios.IOSConfiguration
import com.nhaarman.mockitokotlin2.whenever
import ddmlibModule
import org.amshove.kluent.`it returns`
import org.amshove.kluent.`should be equal to`
import org.amshove.kluent.`should be instance of`
import org.amshove.kluent.mock
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeEmpty
import org.amshove.kluent.shouldContainAll
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldThrow
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.File
import java.time.Duration
import java.time.Instant
import java.time.format.DateTimeFormatter

class ConfigFactoryTest {

    val referenceInstant: Instant = Instant.ofEpochSecond(1000000)
    private val mockInstantTimeProvider = object : InstantTimeProvider {
        override fun referenceTime(): Instant = referenceInstant
    }

    lateinit var parser: ConfigFactory

    fun mockEnvironmentReader(path: String? = null): EnvironmentReader {
        val environmentReader: EnvironmentReader = mock()
        whenever(environmentReader.read()) `it returns` EnvironmentConfiguration(path?.let { File(it) })
        return environmentReader
    }

    @BeforeEach
    fun `setup yaml mapper`() {
        val mapper = ObjectMapper(YAMLFactory().disable(YAMLGenerator.Feature.USE_NATIVE_TYPE_ID))
        mapper.registerModule(DeserializeModule(mockInstantTimeProvider))
            .registerModule(KotlinModule())
            .registerModule(JavaTimeModule())
        parser = ConfigFactory(mapper)
    }

    @Test
    fun `on sample config 1 should deserialize`() {
        val file = File(ConfigFactoryTest::class.java.getResource("/fixture/config/sample_1.yaml").file)
        val configuration = parser.create(file, mockEnvironmentReader())

        configuration.name shouldEqual "sample-app tests"
        configuration.outputDir shouldEqual File("./marathon")
        configuration.analyticsConfiguration shouldEqual AnalyticsConfiguration.InfluxDbConfiguration(
            url = "http://influx.svc.cluster.local:8086",
            user = "root",
            password = "root",
            dbName = "marathon",
            retentionPolicyConfiguration = AnalyticsConfiguration.InfluxDbConfiguration.RetentionPolicyConfiguration.default
        )
        configuration.poolingStrategy shouldEqual ComboPoolingStrategy(
            listOf(
                OmniPoolingStrategy(),
                ModelPoolingStrategy(),
                OperatingSystemVersionPoolingStrategy(),
                ManufacturerPoolingStrategy(),
                AbiPoolingStrategy()
            )
        )
        configuration.shardingStrategy shouldEqual CountShardingStrategy(5)
        configuration.sortingStrategy shouldEqual SuccessRateSortingStrategy(
            Instant.from(
                DateTimeFormatter.ISO_DATE_TIME.parse("2015-03-14T09:26:53.590Z")
            ), false
        )
        configuration.batchingStrategy shouldEqual FixedSizeBatchingStrategy(5)
        configuration.flakinessStrategy shouldEqual ProbabilityBasedFlakinessStrategy(
            0.7,
            3,
            Instant.from(
                DateTimeFormatter.ISO_DATE_TIME.parse(
                    "2015-03-14T09:26:53.590Z"
                )
            )
        )
        configuration.retryStrategy shouldEqual FixedQuotaRetryStrategy(100, 2)
        SimpleClassnameFilter(".*".toRegex()) shouldEqual SimpleClassnameFilter(".*".toRegex())

        configuration.filteringConfiguration.allowlist shouldContainAll listOf(
            SimpleClassnameFilter(".*".toRegex()),
            FullyQualifiedClassnameFilter(".*".toRegex()),
            TestMethodFilter(".*".toRegex()),
            CompositionFilter(
                listOf(
                    TestPackageFilter(".*".toRegex()),
                    TestMethodFilter(".*".toRegex())
                ), CompositionFilter.OPERATION.UNION
            )
        )

        configuration.filteringConfiguration.blocklist shouldContainAll listOf(
            TestPackageFilter(".*".toRegex()),
            AnnotationFilter(".*".toRegex()),
            AnnotationDataFilter(".*".toRegex(), ".*".toRegex())
        )
        configuration.testClassRegexes.map { it.toString() } shouldContainAll listOf("^((?!Abstract).)*Test$")

        // Regex doesn't have proper equals method. Need to check the patter itself
        configuration.includeSerialRegexes.joinToString(separator = "") { it.pattern } shouldEqual """emulator-500[2,4]""".toRegex().pattern
        configuration.excludeSerialRegexes.joinToString(separator = "") { it.pattern } shouldEqual """emulator-5002""".toRegex().pattern
        configuration.ignoreFailures shouldEqual false
        configuration.isCodeCoverageEnabled shouldEqual false
        configuration.fallbackToScreenshots shouldEqual false
        configuration.strictMode shouldEqual true
        configuration.testBatchTimeoutMillis shouldEqual 20_000
        configuration.testOutputTimeoutMillis shouldEqual 30_000
        configuration.debug shouldEqual true
        configuration.screenRecordingPolicy shouldEqual ScreenRecordingPolicy.ON_ANY

        configuration.deviceInitializationTimeoutMillis shouldEqual 300_000
        configuration.vendorConfiguration shouldEqual AndroidConfiguration(
            File("/local/android"),
            File("kotlin-buildscript/build/outputs/apk/debug/kotlin-buildscript-debug.apk"),
            File("kotlin-buildscript/build/outputs/apk/androidTest/debug/kotlin-buildscript-debug-androidTest.apk"),
            listOf(ddmlibModule),
            true,
            mapOf("debug" to "false"),
            true,
            true,
            30_000,
            "-d",
            SerialStrategy.AUTOMATIC,
            ScreenRecordConfiguration(
                preferableRecorderType = DeviceFeature.SCREENSHOT,
                videoConfiguration = VideoConfiguration(false, 1080, 1920, 2, 300),
                screenshotConfiguration = ScreenshotConfiguration(false, 1080, 1920, 200)
            ),
            15000L,
            AllureConfiguration()
        )
    }

    @Test
    fun `on sample config 1 with custom retention policy should deserialize`() {
        val file = File(ConfigFactoryTest::class.java.getResource("/fixture/config/sample_1_rp.yaml").file)

        val configuration = parser.create(file, mockEnvironmentReader())
        configuration.analyticsConfiguration shouldEqual AnalyticsConfiguration.InfluxDbConfiguration(
            url = "http://influx.svc.cluster.local:8086",
            user = "root",
            password = "root",
            dbName = "marathon",
            retentionPolicyConfiguration = AnalyticsConfiguration.InfluxDbConfiguration.RetentionPolicyConfiguration(
                "rpMarathonTest",
                "90d",
                "1h",
                5,
                false
            )
        )
    }

    @Test
    fun `on sample config 2 with minimal configuration should deserialize`() {
        val file = File(ConfigFactoryTest::class.java.getResource("/fixture/config/sample_2.yaml").file)
        val configuration = parser.create(file, mockEnvironmentReader())

        configuration.name shouldEqual "sample-app tests"
        configuration.outputDir shouldEqual File("./marathon")
        configuration.analyticsConfiguration shouldEqual AnalyticsConfiguration.DisabledAnalytics
        configuration.poolingStrategy shouldEqual OmniPoolingStrategy()
        configuration.shardingStrategy shouldEqual ParallelShardingStrategy()
        configuration.sortingStrategy shouldEqual NoSortingStrategy()
        configuration.batchingStrategy shouldEqual IsolateBatchingStrategy()
        configuration.flakinessStrategy shouldEqual IgnoreFlakinessStrategy()
        configuration.retryStrategy shouldEqual NoRetryStrategy()
        SimpleClassnameFilter(".*".toRegex()) shouldEqual SimpleClassnameFilter(".*".toRegex())

        configuration.filteringConfiguration.allowlist.shouldBeEmpty()
        configuration.filteringConfiguration.blocklist.shouldBeEmpty()

        configuration.testClassRegexes.map { it.toString() } shouldContainAll listOf("^((?!Abstract).)*Test[s]*$")

        configuration.includeSerialRegexes shouldEqual emptyList()
        configuration.excludeSerialRegexes shouldEqual emptyList()
        configuration.ignoreFailures shouldEqual false
        configuration.isCodeCoverageEnabled shouldEqual false
        configuration.fallbackToScreenshots shouldEqual false
        configuration.testBatchTimeoutMillis shouldEqual 1800_000
        configuration.testOutputTimeoutMillis shouldEqual 300_000
        configuration.debug shouldEqual true
        configuration.screenRecordingPolicy shouldEqual ScreenRecordingPolicy.ON_FAILURE
        configuration.vendorConfiguration shouldEqual AndroidConfiguration(
            File("/local/android"),
            File("kotlin-buildscript/build/outputs/apk/debug/kotlin-buildscript-debug.apk"),
            File("kotlin-buildscript/build/outputs/apk/androidTest/debug/kotlin-buildscript-debug-androidTest.apk"),
            listOf(ddmlibModule),
            false,
            mapOf(),
            false,
            false,
            30_000,
            "",
            SerialStrategy.AUTOMATIC
        )
    }

    @Test
    fun `on config with ios vendor configuration should initialize a specific vendor configuration`() {
        val file = File(ConfigFactoryTest::class.java.getResource("/fixture/config/sample_3.yaml").file)
        val configuration = parser.create(file, mockEnvironmentReader())

        configuration.vendorConfiguration shouldEqual IOSConfiguration(
            derivedDataDir = file.parentFile.resolve("a"),
            xctestrunPath = file.parentFile.resolve("a/Build/Products/UITesting_iphonesimulator11.0-x86_64.xctestrun"),
            remoteUsername = "testuser",
            remotePrivateKey = File("/home/testuser/.ssh/id_rsa"),
            knownHostsPath = file.parentFile.resolve("known_hosts"),
            remoteRsyncPath = "/usr/local/bin/rsync",
            debugSsh = true,
            alwaysEraseSimulators = false,
            hideRunnerOutput = true,
            compactOutput = true,
            keepAliveIntervalMillis = 300000L,
            devicesFile = file.parentFile.resolve("Testdevices")
        )
    }

    @Test
    fun `on configuration without an explicit remote rsync path should initialize a default one`() {
        val file = File(ConfigFactoryTest::class.java.getResource("/fixture/config/sample_4.yaml").file)
        val configuration = parser.create(file, mockEnvironmentReader())

        val iosConfiguration = configuration.vendorConfiguration as IOSConfiguration
        iosConfiguration.remoteRsyncPath shouldEqual "/usr/bin/rsync"
    }

    @Test
    fun `on configuration without an explicit xctestrun path should throw an exception`() {
        val file = File(ConfigFactoryTest::class.java.getResource("/fixture/config/sample_5.yaml").file)
        val create = { parser.create(file, mockEnvironmentReader()) }

        create shouldThrow ConfigurationException::class
    }

    @Test
    fun `on configuration without androidSdk value should use value provided by environment`() {
        val file = File(ConfigFactoryTest::class.java.getResource("/fixture/config/sample_6.yaml").file)
        val environmentReader = mockEnvironmentReader("/android/home")
        val configuration = parser.create(file, environmentReader)

        configuration.vendorConfiguration shouldEqual AndroidConfiguration(
            environmentReader.read().androidSdk!!,
            File("kotlin-buildscript/build/outputs/apk/debug/kotlin-buildscript-debug.apk"),
            File("kotlin-buildscript/build/outputs/apk/androidTest/debug/kotlin-buildscript-debug-androidTest.apk"),
            listOf(ddmlibModule),
            false,
            mapOf(),
            false,
            false,
            30_000,
            "",
            SerialStrategy.HOSTNAME
        )
    }

    @Test
    fun `on configuration without androidSdk value should throw an exception when ANDROID_HOME is not set`() {
        val file = File(ConfigFactoryTest::class.java.getResource("/fixture/config/sample_7.yaml").file)
        assertThrows<ConfigurationException> {
            parser.create(file, mockEnvironmentReader(null))
        }
    }

    @Test
    fun `on configuration with allowlist but no blocklist should initialize an empty blocklist`() {
        val file = File(ConfigFactoryTest::class.java.getResource("/fixture/config/sample_8.yaml").file)
        val configuration = parser.create(file, mockEnvironmentReader())

        configuration.filteringConfiguration.allowlist shouldEqual listOf(
            SimpleClassnameFilter(".*".toRegex())
        )

        configuration.filteringConfiguration.blocklist shouldBe emptyList()
    }

    @Test
    fun `on configuration with blocklist but no allowlist should initialize an empty allowlist`() {
        val file = File(ConfigFactoryTest::class.java.getResource("/fixture/config/sample_9.yaml").file)
        val configuration = parser.create(file, mockEnvironmentReader())

        configuration.filteringConfiguration.allowlist shouldBe emptyList()

        configuration.filteringConfiguration.blocklist shouldEqual listOf(
            SimpleClassnameFilter(".*".toRegex())
        )
    }

    @Test
    fun `on configuration time limits specified as Duration should be used as relative values`() {
        val file = File(ConfigFactoryTest::class.java.getResource("/fixture/config/sample_10.yaml").file)
        val configuration = parser.create(file, mockEnvironmentReader())

        configuration.sortingStrategy `should be instance of` ExecutionTimeSortingStrategy::class
        val sortingStrategy = configuration.sortingStrategy as ExecutionTimeSortingStrategy
        sortingStrategy.timeLimit shouldEqual referenceInstant.minus(Duration.ofHours(1))

        configuration.flakinessStrategy `should be instance of` ProbabilityBasedFlakinessStrategy::class
        val flakinessStrategy = configuration.flakinessStrategy as ProbabilityBasedFlakinessStrategy
        flakinessStrategy.timeLimit shouldEqual referenceInstant.minus(Duration.ofDays(30))
    }

    @Test
    fun `on configuration with timeout configuration in Android`() {
        val file = File(ConfigFactoryTest::class.java.getResource("/fixture/config/sample_11.yaml").file)
        val configuration = parser.create(file, mockEnvironmentReader())

        val timeoutConfiguration = (configuration.vendorConfiguration as AndroidConfiguration).timeoutConfiguration
        timeoutConfiguration `should be equal to` TimeoutConfiguration(
            shell = Duration.ofSeconds(30),
            listFiles = Duration.ofMinutes(1),
            pushFile = Duration.ofHours(1),
            pullFile = Duration.ofDays(1),
            uninstall = Duration.ofSeconds(1),
            install = Duration.parse("P1DT12H30M5S"),
            screenrecorder = Duration.ofHours(1),
            screencapturer = Duration.ofSeconds(1),
            socketIdleTimeout = Duration.ofSeconds(45)
        )
    }
}
