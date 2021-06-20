package com.malinskiy.marathon.cli.config.deserialize

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import com.malinskiy.marathon.execution.strategy.ExecutionStrategy

class ExecutionStrategyDeserializer : StdDeserializer<ExecutionStrategy>(ExecutionStrategy::class.java) {

    override fun deserialize(
        p: JsonParser,
        ctxt: DeserializationContext
    ): ExecutionStrategy {
        val userValue = p.valueAsString
        val availableValues = ExecutionStrategy
            .values()
            .map { it.name.toLowerCase() }
            .toSet()
        require(availableValues.contains(userValue)) {
            "Incorrect `execution_strategy` value: $userValue. Available values: $availableValues"
        }
        return ExecutionStrategy.valueOf(userValue.toUpperCase())
    }
}
