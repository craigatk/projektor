package projektor.server.api

import java.time.LocalDateTime

data class ResultsProcessing(
    val id: String,
    val status: ResultsProcessingStatus,
    val createdTimestamp: LocalDateTime?,
    val errorMessage: String?
)
