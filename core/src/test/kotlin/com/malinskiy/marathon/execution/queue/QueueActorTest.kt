package com.malinskiy.marathon.execution.queue

import com.malinskiy.marathon.analytics.external.Analytics
import com.malinskiy.marathon.analytics.internal.pub.Track
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.device.DeviceStub
import com.malinskiy.marathon.device.toDeviceInfo
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.execution.DevicePoolMessage.FromQueue
import com.malinskiy.marathon.execution.TestBatchResults
import com.malinskiy.marathon.execution.TestBatchResults.RunCompletionReason
import com.malinskiy.marathon.execution.TestResult
import com.malinskiy.marathon.execution.TestShard
import com.malinskiy.marathon.execution.TestStatus
import com.malinskiy.marathon.execution.strategy.impl.batching.FixedSizeBatchingStrategy
import com.malinskiy.marathon.test.TestVendorConfiguration
import com.nhaarman.mockitokotlin2.KArgumentCaptor
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.reset
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.runBlocking
import org.amshove.kluent.shouldBe
import org.amshove.kluent.shouldBeInstanceOf
import org.amshove.kluent.shouldContainSame
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import com.malinskiy.marathon.test.Test as MarathonTest

class QueueActorTest {
    private lateinit var track: Track
    lateinit var job: Job
    lateinit var poolChannel: Channel<FromQueue>
    lateinit var analytics: Analytics

    lateinit var actor: QueueActor
    lateinit var testResultCaptor: KArgumentCaptor<TestResult>
    lateinit var testBatchResults: TestBatchResults


    @BeforeEach
    fun setup() {
        track = mock()
        analytics = mock()
        job = Job()
        poolChannel = Channel()
    }

    @AfterEach
    fun teardown() {
        reset(track, analytics)
        job.cancel()
    }

    @Test
    fun `setup 1 should have empty queue`() {
        setup_1()

        val isEmptyDeferred = CompletableDeferred<Boolean>()
        runBlocking {
            actor.send(QueueMessage.RequestBatch(TEST_DEVICE_INFO))
            poolChannel.receive()
            actor.send(QueueMessage.Completed(TEST_DEVICE_INFO, testBatchResults))
            actor.send(QueueMessage.IsEmpty(isEmptyDeferred))
            isEmptyDeferred.await() shouldBe true
        }
    }

    @Test
    fun `setup 1 should report failure`() {
        setup_1()

        runBlocking {
            actor.send(QueueMessage.RequestBatch(TEST_DEVICE_INFO))
            poolChannel.receive()
            actor.send(QueueMessage.Completed(TEST_DEVICE_INFO, testBatchResults))
            verify(track).test(any(), any(), testResultCaptor.capture(), any())
            testResultCaptor.firstValue.test shouldBe TEST_1
            testResultCaptor.firstValue.status shouldBe TestStatus.FAILURE
        }
    }

    @Test
    fun `setup 2 should have non empty queue`() {
        setup_2()

        val isEmptyDeferred = CompletableDeferred<Boolean>()
        runBlocking {
            actor.send(QueueMessage.RequestBatch(TEST_DEVICE_INFO))
            poolChannel.receive() shouldBeInstanceOf FromQueue.ExecuteBatch::class
            actor.send(QueueMessage.Completed(TEST_DEVICE_INFO, testBatchResults))
            poolChannel.receive() shouldBeInstanceOf FromQueue.Notify::class
            actor.send(QueueMessage.IsEmpty(isEmptyDeferred))
            isEmptyDeferred.await() shouldBe false
        }
    }

    @Test
    fun `setup 2 should report test failed`() {
        setup_2()

        runBlocking {
            actor.send(QueueMessage.RequestBatch(TEST_DEVICE_INFO))
            poolChannel.receive() shouldBeInstanceOf FromQueue.ExecuteBatch::class
            actor.send(QueueMessage.Completed(TEST_DEVICE_INFO, testBatchResults))
            poolChannel.receive() shouldBeInstanceOf FromQueue.Notify::class

            verify(track, times(1)).test(any(), any(), testResultCaptor.capture(), any())
            testResultCaptor.firstValue.test shouldBe TEST_1
            testResultCaptor.firstValue.status shouldBe TestStatus.FAILURE
        }
    }

    @Test
    fun `setup 2 should provide uncompleted test in the batch`() {
        setup_2()
        runBlocking {
            actor.send(QueueMessage.RequestBatch(TEST_DEVICE_INFO))
            poolChannel.receive() shouldBeInstanceOf FromQueue.ExecuteBatch::class
            actor.send(QueueMessage.Completed(TEST_DEVICE_INFO, testBatchResults))
            poolChannel.receive() shouldBeInstanceOf FromQueue.Notify::class
            actor.send(QueueMessage.RequestBatch(TEST_DEVICE_INFO))
            val actual = poolChannel.receive()

            actual shouldBeInstanceOf FromQueue.ExecuteBatch::class
            (actual as FromQueue.ExecuteBatch).batch.tests shouldContainSame listOf(TEST_1)
        }
    }

    @Test
    fun `setup 2 should have empty queue`() {
        setup_2()
        val isEmptyDeferred = CompletableDeferred<Boolean>()
        runBlocking {
            actor.send(QueueMessage.RequestBatch(TEST_DEVICE_INFO))
            poolChannel.receive() shouldBeInstanceOf FromQueue.ExecuteBatch::class
            actor.send(QueueMessage.Completed(TEST_DEVICE_INFO, testBatchResults))

            actor.send(QueueMessage.RequestBatch(TEST_DEVICE_INFO))
            poolChannel.receive() shouldBeInstanceOf FromQueue.Notify::class
            poolChannel.receive() shouldBeInstanceOf FromQueue.ExecuteBatch::class
            actor.send(QueueMessage.Completed(TEST_DEVICE_INFO, testBatchResults))

            actor.send(QueueMessage.IsEmpty(isEmptyDeferred))
            isEmptyDeferred.await() shouldBe true
        }
    }

    @Test
    fun `setup 2 should report test as failed`() {
        setup_2()

        runBlocking {
            actor.send(QueueMessage.RequestBatch(TEST_DEVICE_INFO))
            poolChannel.receive()
            actor.send(QueueMessage.Completed(TEST_DEVICE_INFO, testBatchResults))

            verify(track).test(any(), any(), testResultCaptor.capture(), any())
            testResultCaptor.firstValue.test shouldBe TEST_1
            testResultCaptor.firstValue.status shouldBe TestStatus.FAILURE
        }
    }

    /**
     * uncompleted tests retry quota is 0, max batch size is 1 and one test in the shard and processing finished
     */
    private fun setup_1() {
        actor =
            createQueueActor(
                configuration = DEFAULT_CONFIGURATION.copy(
                    uncompletedTestRetryQuota = 0,
                    batchingStrategy = FixedSizeBatchingStrategy(size = 1)
                ),
                tests = listOf(TEST_1),
                poolChannel = poolChannel,
                analytics = analytics,
                job = job,
                track = track
            )
        testResultCaptor = argumentCaptor<TestResult>()
        testBatchResults = createBatchResult(
            uncompleted = listOf(
                createTestResult(TEST_1, TestStatus.FAILURE)
            )
        )
    }

    /**
     * uncompleted tests retry quota is 1, max batch size is 1 and one test in the shard
     */
    private fun setup_2() {
        actor =
            createQueueActor(
                configuration = DEFAULT_CONFIGURATION.copy(
                    uncompletedTestRetryQuota = 1,
                    batchingStrategy = FixedSizeBatchingStrategy(size = 1)
                ),
                tests = listOf(TEST_1),
                poolChannel = poolChannel,
                analytics = analytics,
                job = job,
                track = track
            )
        testResultCaptor = argumentCaptor<TestResult>()
        testBatchResults = createBatchResult(
            uncompleted = listOf(
                createTestResult(TEST_1, TestStatus.FAILURE)
            )
        )
    }
}

private val TEST_DEVICE = DeviceStub()
private val TEST_DEVICE_INFO = TEST_DEVICE.toDeviceInfo()

private val TEST_1 = MarathonTest("", "", "test1", emptyList())

private fun createBatchResult(
    finished: List<TestResult> = emptyList(),
    failed: List<TestResult> = emptyList(),
    uncompleted: List<TestResult> = emptyList(),
    completionReason: RunCompletionReason = RunCompletionReason.RUN_END
): TestBatchResults = TestBatchResults(
    TEST_DEVICE,
    finished,
    failed,
    uncompleted,
    completionReason
)

private fun createTestResult(test: MarathonTest, status: TestStatus) = TestResult(
    test = test,
    device = TEST_DEVICE_INFO,
    status = status,
    startTime = 0,
    endTime = 0,
    stacktrace = null,
    attachments = emptyList(),
    testBatchId = "stub-batch"
)

private fun createQueueActor(
    configuration: Configuration,
    tests: List<MarathonTest>,
    poolChannel: SendChannel<FromQueue>,
    analytics: Analytics,
    track: Track,
    job: Job
) = QueueActor(
    configuration,
    TestShard(tests, emptyList()),
    analytics,
    poolChannel,
    DevicePoolId("test"),
    mock(),
    track,
    mock(),
    job,
    Dispatchers.Unconfined
)

private val DEFAULT_CONFIGURATION = Configuration(
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
    vendorConfiguration = TestVendorConfiguration(
        testParser = mock(),
        deviceProvider = mock()
    ),
    analyticsTracking = false,
    deviceInitializationTimeoutMillis = null
)
