package projektor.incomingresults

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import projektor.incomingresults.model.*
import projektor.parser.coverage.payload.CoverageFilePayload
import projektor.parser.grouped.GroupedResultsParser
import projektor.parser.performance.PerformanceResultsParser
import projektor.results.processor.TestResultsProcessor

class GroupedResultsConverter(
    private val groupedResultsParser: GroupedResultsParser,
    private val performanceResultsParser: PerformanceResultsParser,
    private val testResultsProcessor: TestResultsProcessor
) {
    suspend fun parseAndConvertGroupedResults(groupedResultsBlob: String): GroupedResults = withContext(Dispatchers.IO) {
        val incomingGroupedResults = groupedResultsParser.parseGroupedResults(groupedResultsBlob)

        val groupedTestSuites = incomingGroupedResults.groupedTestSuites?.map {
            val nonEmptyTestSuites = testResultsProcessor.parseResultsBlob(it.testSuitesBlob)
                .filter { testSuite -> !testSuite.testCases.isNullOrEmpty() }

            GroupedTestSuites(
                groupName = it.groupName,
                groupLabel = it.groupLabel,
                directory = it.directory,
                testSuites = nonEmptyTestSuites
            )
        } ?: listOf()

        val metadata = incomingGroupedResults.metadata?.let { metadata ->
            ResultsMetadata(
                git = metadata.git?.let { gitMetadata ->
                    GitMetadata(
                        repoName = gitMetadata.repoName,
                        projectName = gitMetadata.projectName,
                        branchName = gitMetadata.branchName,
                        isMainBranch = gitMetadata.isMainBranch
                    )
                },
                ci = metadata.ci
            )
        }

        val performanceResults = incomingGroupedResults.performanceResults?.mapNotNull { perfResults ->
            val parsedResults = performanceResultsParser.parseResults(perfResults.resultsBlob)

            if (parsedResults != null) PerformanceResult(
                name = perfResults.name,
                requestCount = parsedResults.requestStats.count,
                requestsPerSecond = parsedResults.requestStats.ratePerSecond,
                average = parsedResults.performanceStats.average,
                maximum = parsedResults.performanceStats.maximum,
                p95 = parsedResults.performanceStats.p95
            ) else null
        } ?: listOf()

        GroupedResults(
            groupedTestSuites = groupedTestSuites,
            performanceResults = performanceResults,
            metadata = metadata,
            wallClockDuration = incomingGroupedResults.wallClockDuration,
            coverageFiles = incomingGroupedResults.coverageFiles?.map { incomingCoverageFile ->
                CoverageFilePayload(
                    reportContents = incomingCoverageFile.reportContents,
                    baseDirectoryPath = incomingCoverageFile.baseDirectoryPath
                )
            }
        )
    }
}
