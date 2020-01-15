package projektor.incomingresults

import projektor.incomingresults.model.GroupedResults
import projektor.incomingresults.model.GroupedTestSuites
import projektor.parser.grouped.GroupedResultsParser
import projektor.results.processor.TestResultsProcessor

class GroupedResultsConverter(
    private val groupedResultsParser: GroupedResultsParser,
    private val testResultsProcessor: TestResultsProcessor
) {
    fun parseAndConvertGroupedResults(groupedResultsXml: String): GroupedResults {
        val incomingGroupedResults = groupedResultsParser.parseGroupedResults(groupedResultsXml)

        val groupedTestSuites = incomingGroupedResults.groupedTestSuites.map {
            val nonEmptyTestSuites = testResultsProcessor.parseResultsBlob(it.testSuitesBlob).filter { testSuite -> !testSuite.testCases.isNullOrEmpty() }
            GroupedTestSuites(
                    groupName = it.groupName,
                    groupLabel = it.groupLabel,
                    directory = it.directory,
                    testSuites = nonEmptyTestSuites
            )
        }

        return GroupedResults(groupedTestSuites)
    }
}
