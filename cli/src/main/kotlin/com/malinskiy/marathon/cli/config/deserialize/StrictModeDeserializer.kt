package com.malinskiy.marathon.cli.config.deserialize

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.malinskiy.marathon.exceptions.ConfigurationException
import com.malinskiy.marathon.execution.strategy.StrictMode
import com.malinskiy.marathon.log.MarathonLogging

class StrictModeDeserializer : StdDeserializer<StrictMode>(StrictMode::class.java) {

    private val logger = MarathonLogging.logger("StrictModeDeserializer")

    override fun deserialize(
        p: JsonParser,
        ctxt: DeserializationContext
    ): StrictMode {
        val valueAsString = p.valueAsString
        val deprecatedValues = setOf("true", "false")
        val strictModeValues = StrictMode.values().map { it.name.toLowerCase() }
        return when {
            deprecatedValues.contains(valueAsString.toLowerCase()) ->
                parseDeprecatedStrictMode(valueAsString)
            strictModeValues.contains(valueAsString) ->
                StrictMode.valueOf(valueAsString.toUpperCase())
            else -> throw ConfigurationException("Invalid strict_mode value: `$valueAsString`. Should be one of ${strictModeValues}")
        }
    }

    private fun parseDeprecatedStrictMode(valueAsString: String): StrictMode {
        logger.warn(
            """
            |You use deprecated strict_mode configuration: [true, false].
            |This API will be deleted in the next major version.
            |Replace `true` by `all_success`, `false` by `any_success`. Look at documentation for details. 
        """.trimMargin()
        )
        val value: Boolean = requireNotNull(valueAsString.toBoolean()) {
            "Failed to cast $valueAsString to Boolean"
        }
        return when (value) {
            true -> StrictMode.ALL_SUCCESS
            false -> StrictMode.ANY_SUCCESS
        }
    }
}
