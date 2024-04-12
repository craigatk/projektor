package projektor.parser.coverage.payload

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

class CoveragePayloadParser {
    private val objectMapper =
        ObjectMapper()
            .registerKotlinModule()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    fun parseCoverageFilePayload(payload: String): CoverageFilePayload = objectMapper.readValue(payload, CoverageFilePayload::class.java)

    fun serializeCoverageFilePayload(payload: CoverageFilePayload): String = objectMapper.writeValueAsString(payload)
}
