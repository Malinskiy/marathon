package com.example

import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.android.emulator.control.VmRunState
import com.google.protobuf.Empty
import com.malinskiy.adam.junit4.android.rule.Mode
import com.malinskiy.adam.junit4.rule.EmulatorGrpcRule
import io.qameta.allure.android.runners.AllureAndroidJUnit4
import io.qameta.allure.kotlin.*
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AllureAndroidJUnit4::class)
@Epic("Emulator")
@Feature("gRPC")
@Story("Slow")
@Owner("user2")
@Severity(SeverityLevel.CRITICAL)
@LocalEmulatorTest
class GrpcActivityTest {

    @get:Rule
    val rule = ActivityScenarioRule(MainActivity::class.java)

    @get:Rule
    val emulator = EmulatorGrpcRule(mode = Mode.ASSERT)

    @Test
    fun testVmState() {
        runBlocking {
            val vmState = emulator.grpc.getVmState(Empty.getDefaultInstance())
            Allure.description("VM state is $vmState")
            assert(vmState.state == VmRunState.RunState.RUNNING)
        }
    }
}
