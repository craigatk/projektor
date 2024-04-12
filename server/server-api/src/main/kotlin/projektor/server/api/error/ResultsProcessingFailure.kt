package projektor.server.api.error

import java.time.Instant

data class ResultsProcessingFailure(
    val id: String,
    val body: String,
    val bodyType: FailureBodyType,
    val createdTimestamp: Instant,
    val failureMessage: String?,
)
