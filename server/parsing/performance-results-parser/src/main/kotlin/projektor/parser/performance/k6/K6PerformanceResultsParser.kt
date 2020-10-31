package projektor.parser.performance.k6

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import projektor.parser.performance.k6.model.K6Results

class K6PerformanceResultsParser {
    private val mapper = ObjectMapper()
        .registerKotlinModule()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    fun parseResults(k6Results: String): K6Results =
        mapper.readValue(k6Results, K6Results::class.java)
}
