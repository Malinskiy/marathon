package com.malinskiy.marathon.cli.config.deserialize

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.malinskiy.marathon.execution.strategy.StrictMode

class StrictModeDeserializer : StdDeserializer<StrictMode>(StrictMode::class.java) {
    override fun deserialize(
        p: JsonParser,
        ctxt: DeserializationContext
    ): StrictMode {
        val value = p.valueAsString.toUpperCase()
        return StrictMode.valueOf(value)
    }
}
