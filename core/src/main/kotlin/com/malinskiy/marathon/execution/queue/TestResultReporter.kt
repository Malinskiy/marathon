package com.malinskiy.marathon.execution.queue

import com.malinskiy.marathon.actor.StateMachine
import com.malinskiy.marathon.analytics.internal.pub.Track
import com.malinskiy.marathon.device.DeviceInfo
import com.malinskiy.marathon.device.DevicePoolId
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.execution.TestResult
import com.malinskiy.marathon.execution.TestShard
import com.malinskiy.marathon.execution.strategy.ExecutionStrategy
import com.malinskiy.marathon.log.MarathonLogging
import com.malinskiy.marathon.test.Test
import com.malinskiy.marathon.test.toTestName

class TestResultReporter(
    private val poolId: DevicePoolId,
    shard: TestShard,
    private val configuration: Configuration,
    private val track: Track
) {

    private val tests: HashMap<String, StateMachine<TestState, TestEvent, TestAction>> = HashMap()

    private val logger = MarathonLogging.logger("TestResultReporter")

    private fun createState(initialCount: Int) = StateMachine.create<TestState, TestEvent, TestAction> {
        initialState(TestState.Added(initialCount))
        state<TestState.Added> {
            on<TestEvent.Passed> {
                if (count <= 1) {
                    transitionTo(TestState.Passed(it.device, it.testResult), TestAction.SaveReport(it.device, it.testResult))
                } else {
                    when (configuration.executionStrategy) {
                        ExecutionStrategy.ANY_SUCCESS ->
                            transitionTo(TestState.Passed(it.device, it.testResult), TestAction.SaveReport(it.device, it.testResult))
                        ExecutionStrategy.ALL_SUCCESS,
                        ExecutionStrategy.ANY_FAIL ->
                            transitionTo(TestState.Executed(it.device, it.testResult, count - 1))
                    }
                }
            }
            on<TestEvent.Failed> {
                if (count <= 1) {
                    transitionTo(TestState.Failed(it.device, it.testResult), TestAction.SaveReport(it.device, it.testResult))
                } else {
                    when (configuration.executionStrategy) {
                        ExecutionStrategy.ANY_SUCCESS ->
                            transitionTo(TestState.Executed(it.device, it.testResult, count - 1))
                        ExecutionStrategy.ALL_SUCCESS,
                        ExecutionStrategy.ANY_FAIL ->
                            transitionTo(TestState.Failed(it.device, it.testResult), TestAction.SaveReport(it.device, it.testResult))
                    }
                }
            }
            on<TestEvent.Incomplete> {
                if (it.final) {
                    transitionTo(TestState.Failed(it.device, it.testResult), TestAction.SaveReport(it.device, it.testResult))
                } else {
                    transitionTo(TestState.Executed(it.device, it.testResult, count))
                }
            }
            on<TestEvent.Retry> {
                dontTransition()
            }
            on<TestEvent.Remove> {
                transitionTo(this.copy(count = this.count - it.diff))
            }
        }
        state<TestState.Executed> {
            on<TestEvent.Failed> {
                if (count <= 1) {
                    transitionTo(TestState.Failed(it.device, it.testResult), TestAction.SaveReport(it.device, it.testResult))
                } else {
                    when (configuration.executionStrategy) {
                        ExecutionStrategy.ANY_SUCCESS ->
                            transitionTo(TestState.Executed(it.device, it.testResult, count - 1))
                        ExecutionStrategy.ALL_SUCCESS,
                        ExecutionStrategy.ANY_FAIL ->
                            transitionTo(TestState.Failed(it.device, it.testResult), TestAction.SaveReport(it.device, it.testResult))
                    }
                }
            }
            on<TestEvent.Incomplete> {
                if (it.final) {
                    transitionTo(TestState.Failed(it.device, it.testResult), TestAction.SaveReport(it.device, it.testResult))
                } else {
                    transitionTo(TestState.Executed(it.device, it.testResult, count))
                }
            }
            on<TestEvent.Remove> {
                transitionTo(this.copy(count = this.count - it.diff))
            }
            on<TestEvent.Passed> {
                if (count <= 1) {
                    transitionTo(TestState.Passed(it.device, it.testResult), TestAction.SaveReport(it.device, it.testResult))
                } else {
                    when (configuration.executionStrategy) {
                        ExecutionStrategy.ANY_SUCCESS ->
                            transitionTo(TestState.Passed(it.device, it.testResult), TestAction.SaveReport(it.device, it.testResult))
                        ExecutionStrategy.ALL_SUCCESS,
                        ExecutionStrategy.ANY_FAIL ->
                            transitionTo(TestState.Executed(it.device, it.testResult, count - 1))
                    }
                }
            }
            on<TestEvent.Retry> {
                transitionTo(this.copy(count = this.count + 1))
            }
        }
        state<TestState.Failed> {
        }
        state<TestState.Passed> {
            on<TestEvent.Failed> {
                dontTransition()
            }
            on<TestEvent.Incomplete> {
                dontTransition()
            }
        }
        onTransition {
            if (it as? StateMachine.Transition.Valid !is StateMachine.Transition.Valid) {
                logger.error { "from ${it.fromState} event ${it.event}" }
            }
            trackTestTransition(poolId, it)
        }
    }

    init {
        val allTests = shard.tests + shard.flakyTests
        allTests.groupBy { it }.map {
            val count = it.value.size
            it.key.toTestName() to createState(count)
        }.also {
            tests.putAll(it)
        }
    }

    fun testFinished(device: DeviceInfo, testResult: TestResult) {
        tests[testResult.test.toTestName()]?.transition(TestEvent.Passed(device, testResult))
    }

    fun testFailed(device: DeviceInfo, testResult: TestResult) {
        tests[testResult.test.toTestName()]?.transition(TestEvent.Failed(device, testResult))
    }

    fun testIncomplete(device: DeviceInfo, testResult: TestResult, final: Boolean) {
        tests[testResult.test.toTestName()]?.transition(TestEvent.Incomplete(device, testResult, final))
    }

    fun retryTest(device: DeviceInfo, testResult: TestResult) {
        tests[testResult.test.toTestName()]?.transition(TestEvent.Retry(device, testResult))
    }

    fun removeTest(test: Test, diff: Int) {
        tests[test.toTestName()]?.transition(TestEvent.Remove(diff))
    }

    private fun trackTestTransition(poolId: DevicePoolId, transition: StateMachine.Transition<TestState, TestEvent, TestAction>) {
        val validTransition = transition as? StateMachine.Transition.Valid
        val final = if (validTransition is StateMachine.Transition.Valid) {
            when (validTransition.sideEffect) {
                is TestAction.SaveReport -> true
                else -> false
            }
        } else false

        val (testResult: TestResult?, device: DeviceInfo?) = extractEventAndDevice(transition)
        if (testResult == null || device == null) return

        track.test(poolId, device, testResult, final)
    }

    private fun extractEventAndDevice(transition: StateMachine.Transition<TestState, TestEvent, TestAction>): Pair<TestResult?, DeviceInfo?> {
        val event = transition.event
        val testResult: TestResult? = when (event) {
            is TestEvent.Passed -> event.testResult
            is TestEvent.Failed -> event.testResult
            is TestEvent.Retry -> event.testResult
            is TestEvent.Incomplete -> event.testResult
            else -> null
        }
        val device: DeviceInfo? = when (event) {
            is TestEvent.Passed -> event.device
            is TestEvent.Failed -> event.device
            is TestEvent.Retry -> event.device
            is TestEvent.Incomplete -> event.device
            else -> null
        }
        return Pair(testResult, device)
    }
}
