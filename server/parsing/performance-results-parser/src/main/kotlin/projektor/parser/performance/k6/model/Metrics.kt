package projektor.parser.performance.k6.model

import com.fasterxml.jackson.annotation.JsonProperty

class Metrics(
    @JsonProperty("http_req_duration") val requestDurationStats: DurationStats,
    @JsonProperty("iterations") val iterations: RequestCounts
)
