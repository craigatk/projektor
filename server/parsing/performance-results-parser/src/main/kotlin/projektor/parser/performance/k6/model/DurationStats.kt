package projektor.parser.performance.k6.model

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

class DurationStats(
    @JsonProperty("avg") val average: BigDecimal,
    @JsonProperty("max") val maximum: BigDecimal,
    @JsonProperty("med") val median: BigDecimal,
    @JsonProperty("min") val minimum: BigDecimal,
    @JsonProperty("p(90)") val p90: BigDecimal,
    @JsonProperty("p(95)") val p95: BigDecimal
)
