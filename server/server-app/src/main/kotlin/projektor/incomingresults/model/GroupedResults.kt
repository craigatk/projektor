package projektor.incomingresults.model

data class GroupedResults(
    val groupedTestSuites: List<GroupedTestSuites>,
    val metadata: ResultsMetadata?
)
