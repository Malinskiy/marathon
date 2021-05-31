package com.malinskiy.marathon.android.adam.execution

import com.malinskiy.adam.android.contract.TestRunnerContract
import com.malinskiy.adam.request.forwarding.RemoteTcpPortSpec
import com.malinskiy.marathon.android.adam.AdamAndroidDevice
import com.malinskiy.marathon.android.configuration.AndroidConfiguration
import com.malinskiy.marathon.android.configuration.TestAccessConfiguration
import com.malinskiy.marathon.execution.Configuration
import com.malinskiy.marathon.log.MarathonLogging

class ArgumentsFactory(private val device: AdamAndroidDevice) {
    private val logger = MarathonLogging.logger("ArgumentsFactory")


    fun generate(configuration: Configuration, androidConfiguration: AndroidConfiguration): Map<String, String> {
        return mutableMapOf<String, String>().apply {
            putAll(androidConfiguration.instrumentationArgs)
            putAll(generateTestAccessArgs(androidConfiguration.testAccessConfiguration))
            if (configuration.isCodeCoverageEnabled) {
                put("coverage", "true")
            }
        }.toMap()
    }

    private fun generateTestAccessArgs(configuration: TestAccessConfiguration): Map<String, String> {
        val additionalArgs = mutableMapOf<String, String>()

        if (configuration.adb) {
            when {
                device.isLocalEmulator() -> {
                    additionalArgs[TestRunnerContract.adbPortArgumentName] = device.client.port.toString()
                    additionalArgs[TestRunnerContract.adbHostArgumentName] = EMULATOR_HOST_LOOPBACK_ADDR
                    additionalArgs[TestRunnerContract.deviceSerialArgumentName] = device.adbSerial
                }
                else -> {
                    if (device.portForwardingRules.containsKey("adb")) {
                        val port = (device.portForwardingRules["adb"]?.localSpec as? RemoteTcpPortSpec)?.port?.toString()
                        if (port != null) {
                            additionalArgs[TestRunnerContract.adbPortArgumentName] = port
                            additionalArgs[TestRunnerContract.adbHostArgumentName] = "localhost"
                            additionalArgs[TestRunnerContract.deviceSerialArgumentName] = device.adbSerial
                        }
                    }
                }
            }
        }

        if (configuration.console) {
            if (device.isLocalEmulator()) {
                val consolePort = device.adbSerial.substringAfter("emulator-").trim().toIntOrNull()
                if (consolePort == null) {
                    logger.debug { "Unable to parse emulator console port for serial ${device.adbSerial}" }
                } else {
                    additionalArgs[TestRunnerContract.consolePortArgumentName] = consolePort.toString()
                    additionalArgs[TestRunnerContract.consoleHostArgumentName] = EMULATOR_HOST_LOOPBACK_ADDR
                    if (configuration.consoleToken.isNotBlank()) {
                        additionalArgs[TestRunnerContract.emulatorAuthTokenArgumentName] = configuration.consoleToken
                    }
                }
            } else {
                logger.warn { "Access to emulator console port is requested, but device ${device.adbSerial} is not a local emulator" }
                logger.warn { "If ${device.adbSerial} is an emulator, connect the adb server instead of `adb connect` for this to work" }
            }
        }

        if (configuration.gRPC) {
            if (device.isLocalEmulator()) {
                val consolePort = device.adbSerial.substringAfter("emulator-").trim().toIntOrNull()
                if (consolePort == null) {
                    logger.debug { "Unable to parse emulator console port for serial ${device.adbSerial}" }
                } else {
                    val gRPCPort: Int = consolePort + 3000
                    logger.debug { "Assuming gRPC port is console port $consolePort + 3000 = $gRPCPort" }
                    additionalArgs[TestRunnerContract.grpcPortArgumentName] = gRPCPort.toString()
                    additionalArgs[TestRunnerContract.grpcHostArgumentName] = EMULATOR_HOST_LOOPBACK_ADDR
                }
            } else {
                logger.warn { "Access to emulator gRPC port is requested, but device ${device.adbSerial} is not a local emulator" }
                logger.warn { "If ${device.adbSerial} is an emulator, connect the adb server instead of `adb connect` for this to work" }
            }
        }

        return additionalArgs.toMap()
    }

    companion object {
        const val EMULATOR_HOST_LOOPBACK_ADDR = "10.0.2.2"
    }
}
