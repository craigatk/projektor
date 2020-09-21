package projektor.incomingresults

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import projektor.incomingresults.model.GitMetadata
import projektor.incomingresults.model.GroupedResults
import projektor.incomingresults.model.GroupedTestSuites
import projektor.incomingresults.model.ResultsMetadata
import projektor.parser.grouped.GroupedResultsParser
import projektor.results.processor.TestResultsProcessor

class GroupedResultsConverter(
    private val groupedResultsParser: GroupedResultsParser,
    private val testResultsProcessor: TestResultsProcessor
) {
    suspend fun parseAndConvertGroupedResults(groupedResultsBlob: String): GroupedResults = withContext(Dispatchers.IO) {
        val incomingGroupedResults = groupedResultsParser.parseGroupedResults(groupedResultsBlob)

        val groupedTestSuites = incomingGroupedResults.groupedTestSuites.map {
            val nonEmptyTestSuites = testResultsProcessor.parseResultsBlob(it.testSuitesBlob)
                    .filter { testSuite -> !testSuite.testCases.isNullOrEmpty() }

            GroupedTestSuites(
                    groupName = it.groupName,
                    groupLabel = it.groupLabel,
                    directory = it.directory,
                    testSuites = nonEmptyTestSuites
            )
        }

        val metadata = incomingGroupedResults.metadata?.let { metadata ->
            ResultsMetadata(
                    git = metadata.git?.let { gitMetadata ->
                        GitMetadata(
                                repoName = gitMetadata.repoName,
                                projectName = gitMetadata.projectName,
                                branchName = gitMetadata.branchName,
                                isMainBranch = gitMetadata.isMainBranch
                        )
                    }
            )
        }

        GroupedResults(
                groupedTestSuites = groupedTestSuites,
                metadata = metadata,
                wallClockDuration = incomingGroupedResults.wallClockDuration
        )
    }
}
