package com.example

import androidx.test.ext.junit.rules.ActivityScenarioRule
import com.malinskiy.adam.junit4.android.rule.EmulatorConsoleRule
import com.malinskiy.adam.junit4.android.rule.Mode
import io.qameta.allure.android.runners.AllureAndroidJUnit4
import io.qameta.allure.kotlin.*
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AllureAndroidJUnit4::class)
@Epic("Emulator")
@Feature("console")
@Story("Slow")
@Owner("user2")
@Severity(SeverityLevel.CRITICAL)
@LocalEmulatorTest
class ConsoleActivityTest {
    @get:Rule
    val rule = ActivityScenarioRule(MainActivity::class.java)

    @get:Rule
    val console = EmulatorConsoleRule(mode = Mode.ASSERT)

    @Test
    fun testVmState() {
        runBlocking {
            val result = console.execute("avd status")
            Allure.description("VM state is $result")
            assert(result.contains("running"))
        }
    }
}
