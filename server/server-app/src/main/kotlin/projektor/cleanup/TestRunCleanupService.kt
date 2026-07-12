package projektor.cleanup

import org.slf4j.LoggerFactory
import projektor.attachment.AttachmentService
import projektor.coverage.CoverageService
import projektor.incomingresults.processing.ResultsProcessingRepository
import projektor.metadata.TestRunMetadataService
import projektor.repository.coverage.RepositoryCoverageRepository
import projektor.server.api.PublicId
import projektor.server.api.results.ResultsProcessingStatus
import projektor.testrun.TestRunRepository
import java.time.LocalDate

class TestRunCleanupService(
    private val cleanupConfig: CleanupConfig,
    private val testRunRepository: TestRunRepository,
    private val resultsProcessingRepository: ResultsProcessingRepository,
    private val coverageService: CoverageService,
    private val attachmentService: AttachmentService?,
    private val testRunMetadataService: TestRunMetadataService,
    private val repositoryCoverageRepository: RepositoryCoverageRepository,
) {
    private val logger = LoggerFactory.getLogger(javaClass.canonicalName)

    suspend fun conditionallyCleanupTestRuns(): List<PublicId> =
        if (cleanupConfig.reportCleanupEnabled) {
            val maxReportAgeDays = cleanupConfig.maxReportAgeDays?.toLong() ?: 0

            val createdBefore = LocalDate.now().minusDays(maxReportAgeDays)
            val testRunsToCleanUp = testRunRepository.findTestRunsCreatedBeforeAndNotPinned(createdBefore)

            val cleanedUpTestRunIds =
                testRunsToCleanUp.mapNotNull {
                    conditionallyCleanupTestRun(it, cleanupConfig)
                }

            logger.info("Removed ${cleanedUpTestRunIds.size} test runs created before $createdBefore")

            cleanedUpTestRunIds
        } else {
            logger.info("Test run clean up not enabled, skipping")

            listOf()
        }

    suspend fun cleanupTestRun(publicId: PublicId) {
        preserveLastKnownCoverage(publicId)

        testRunRepository.deleteTestRun(publicId)

        coverageService.deleteCoverage(publicId)

        attachmentService?.deleteAttachments(publicId)

        resultsProcessingRepository.updateResultsProcessingStatus(publicId, ResultsProcessingStatus.DELETED)
    }

    // Snapshots this run's coverage into a durable, per-repo table before it's deleted, so the
    // repository coverage badge/current-coverage endpoints keep reflecting the last known value
    // instead of going empty once every test run for a repo has aged out. Best-effort: a failure
    // here must never block the actual cleanup of the test run.
    private suspend fun preserveLastKnownCoverage(publicId: PublicId) {
        try {
            val gitMetadata = testRunMetadataService.fetchGitMetadata(publicId)
            val repoName = gitMetadata?.repoName

            if (gitMetadata != null && gitMetadata.isMainBranch && repoName != null) {
                val coveredPercentage = coverageService.getCoveredLinePercentage(publicId)

                if (coveredPercentage != null) {
                    val testRunSummary = testRunRepository.fetchTestRunSummary(publicId)

                    if (testRunSummary != null) {
                        repositoryCoverageRepository.saveLastKnownCoverageIfNewer(
                            repoName = repoName,
                            projectName = gitMetadata.projectName,
                            branchName = gitMetadata.branchName,
                            coveredPercentage = coveredPercentage,
                            testRunPublicId = publicId,
                            createdTimestamp = testRunSummary.createdTimestamp,
                        )
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("Failed to preserve last known coverage for test run $publicId before cleanup", e)
        }
    }

    private suspend fun conditionallyCleanupTestRun(
        publicId: PublicId,
        cleanupConfig: CleanupConfig,
    ): PublicId? =
        try {
            if (!cleanupConfig.dryRun) {
                cleanupTestRun(publicId)
            } else {
                logger.info("Not deleting test run $publicId since dry run is enabled")
            }
            publicId
        } catch (e: Exception) {
            logger.error("Failed to clean up test run $publicId", e)
            null
        }
}
