package projektor.incomingresults.model

import projektor.parser.model.TestSuite

data class GroupedTestSuites(
    val testSuites: List<TestSuite>,
    val groupName: String?,
    val groupLabel: String?,
    val directory: String?,
)
