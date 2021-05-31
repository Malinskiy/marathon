package com.example

import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.android.emulator.control.VmRunState
import com.google.protobuf.Empty
import com.malinskiy.adam.junit4.android.UnsafeAdbAccess
import com.malinskiy.adam.junit4.android.rule.Mode
import com.malinskiy.adam.junit4.rule.AdbRule
import com.malinskiy.adam.request.device.ListDevicesRequest
import com.malinskiy.adam.request.shell.v1.ShellCommandRequest
import io.qameta.allure.android.runners.AllureAndroidJUnit4
import io.qameta.allure.kotlin.*
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AllureAndroidJUnit4::class)
@Epic("Emulator")
@Feature("adb")
@Story("Slow")
@Owner("user2")
@Severity(SeverityLevel.CRITICAL)
class AdbActivityTest {

    @get:Rule
    val rule = ActivityScenarioRule(MainActivity::class.java)

    @get:Rule
    val adbRule = AdbRule(mode = Mode.ASSERT)

    @Test
    fun testVmState() {
        runBlocking {
            val result = adbRule.adb.execute(ShellCommandRequest("echo \"hello world\""))
            assert(result.exitCode == 0)
            assert(result.output.startsWith("hello world"))
        }
    }

    @UnsafeAdbAccess
    @Test
    fun testUnsafeAccess() {
        runBlocking {
            val list = adbRule.adbUnsafe.execute(ListDevicesRequest())
            assert(list.isNotEmpty())
        }
    }
}
