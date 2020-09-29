package projektor.parser

import projektor.parser.grouped.GroupedResultsParser
import projektor.parser.grouped.model.GroupedResults
import projektor.parser.grouped.model.GroupedTestSuites
import projektor.parser.grouped.model.ResultsMetadata
import java.math.BigDecimal

class GroupedResultsXmlLoader {
    private val groupedResultsParser = GroupedResultsParser()
    private val resultsXmlLoader = ResultsXmlLoader()

    fun passingGroupedResults(metadata: ResultsMetadata? = null, wallClockDuration: BigDecimal? = null): String {
        val groupedTestSuites1 = GroupedTestSuites()
        groupedTestSuites1.groupName = "Group1"
        groupedTestSuites1.groupLabel = "unitTest"
        groupedTestSuites1.directory = "/test/unit"
        groupedTestSuites1.testSuitesBlob = resultsXmlLoader.passing() + resultsXmlLoader.longOutput()

        val groupedTestSuites2 = GroupedTestSuites()
        groupedTestSuites2.groupName = "Group2"
        groupedTestSuites2.groupLabel = "functionalTest"
        groupedTestSuites2.directory = "/test/functional"
        groupedTestSuites2.testSuitesBlob = resultsXmlLoader.output()

        val groupedResults = GroupedResults()
        groupedResults.groupedTestSuites = listOf(groupedTestSuites1, groupedTestSuites2)
        groupedResults.metadata = metadata
        groupedResults.wallClockDuration = wallClockDuration

        return groupedResultsParser.serializeGroupedResults(groupedResults)
    }

    fun wrapResultsXmlInGroup(resultsXml: String, metadata: ResultsMetadata? = null): String {
        val groupedTestSuites = GroupedTestSuites()
        groupedTestSuites.groupName = "Group1"
        groupedTestSuites.groupLabel = "unitTest"
        groupedTestSuites.directory = "/test/unit"
        groupedTestSuites.testSuitesBlob = resultsXml

        val groupedResults = GroupedResults()
        groupedResults.groupedTestSuites = listOf(groupedTestSuites)
        groupedResults.metadata = metadata

        return groupedResultsParser.serializeGroupedResults(groupedResults)
    }
}
