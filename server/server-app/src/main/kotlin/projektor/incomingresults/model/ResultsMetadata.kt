package projektor.incomingresults.model

import java.time.Instant

data class ResultsMetadata(
    val git: GitMetadata?,
    val ci: Boolean?,
    val group: String?,
    val createdTimestamp: Instant?,
)
