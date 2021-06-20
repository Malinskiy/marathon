package com.malinskiy.marathon.execution.progress.tracker

import com.malinskiy.marathon.actor.StateMachine
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.execution.strategy.ExecutionStrategy
import com.malinskiy.marathon.test.Test
import java.util.concurrent.atomic.AtomicInteger

class PoolProgressTracker(private val configuration: Configuration) {

    private val tests = mutableMapOf<Test, StateMachine<ProgressTestState, ProgressEvent, Any>>()

    private fun createState() = StateMachine.create<ProgressTestState, ProgressEvent, Any> {
        initialState(ProgressTestState.Started)
        state<ProgressTestState.Started> {
            on<ProgressEvent.Failed> {
                transitionTo(ProgressTestState.Failed)
            }
            on<ProgressEvent.Passed> {
                transitionTo(ProgressTestState.Passed)
            }
            on<ProgressEvent.Ignored> {
                transitionTo(ProgressTestState.Ignored)
            }
        }
        state<ProgressTestState.Passed> {
            on<ProgressEvent.Failed> {
                when (configuration.executionStrategy) {
                    ExecutionStrategy.ANY_SUCCESS -> dontTransition()
                    ExecutionStrategy.ANY_FAIL,
                    ExecutionStrategy.ALL_SUCCESS -> transitionTo(ProgressTestState.Failed)
                }
            }
            on<ProgressEvent.Ignored> {
                dontTransition()
            }
        }
        state<ProgressTestState.Failed> {
            on<ProgressEvent.Passed> {
                when (configuration.executionStrategy) {
                    ExecutionStrategy.ANY_SUCCESS -> transitionTo(ProgressTestState.Passed)
                    ExecutionStrategy.ANY_FAIL,
                    ExecutionStrategy.ALL_SUCCESS -> dontTransition()
                }
            }

        }
        state<ProgressTestState.Ignored> {
            on<ProgressEvent.Passed> {
                transitionTo(ProgressTestState.Passed)
            }
        }
    }

    private fun updateStatus(test: Test, newStatus: ProgressEvent) = tests[test]?.transition(newStatus)

    private val totalTests = AtomicInteger(0)
    private val completed = AtomicInteger(0)
    private val failed = AtomicInteger(0)
    private val ignored = AtomicInteger(0)

    fun testStarted(test: Test) {
        tests.computeIfAbsent(test) { _ -> createState() }
    }

    fun testFailed(test: Test) {
        failed.updateAndGet {
            it + 1
        }
        updateStatus(test, ProgressEvent.Failed)
    }

    fun testPassed(test: Test) {
        completed.updateAndGet {
            it + 1
        }
        updateStatus(test, ProgressEvent.Passed)
    }

    fun testIgnored(test: Test) {
        ignored.updateAndGet {
            it + 1
        }
        updateStatus(test, ProgressEvent.Ignored)
    }

    fun aggregateResult(): Boolean = tests.all {
        when (it.value.state) {
            is ProgressTestState.Passed -> true
            is ProgressTestState.Ignored -> true
            else -> false
        }
    }

    fun totalTests(size: Int) {
        totalTests.set(size)
    }

    fun removeTests(count: Int) {
        totalTests.updateAndGet {
            it - count
        }
    }

    fun progress(): Float {
        return (completed.toFloat() + failed.toFloat() + ignored.toFloat()) / totalTests.toFloat()
    }

    fun addTests(count: Int) {
        totalTests.updateAndGet {
            it + count
        }
    }
}
