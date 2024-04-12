package projektor.parser.performance.k6.model

import com.fasterxml.jackson.annotation.JsonProperty

class K6Results(
    @JsonProperty("metrics") val metrics: Metrics,
)
