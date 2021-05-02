package projektor.incomingresults.model

data class ResultsMetadata(
    val git: GitMetadata?,
    val ci: Boolean?,
    val group: String?
)
