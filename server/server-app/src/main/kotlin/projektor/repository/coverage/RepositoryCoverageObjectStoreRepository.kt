package projektor.repository.coverage

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import projektor.coverage.toCoverageStats
import projektor.objectstore.json.JsonObjectStore
import projektor.parser.coverage.model.CoverageReportStat
import projektor.parser.coverage.model.CoverageReportStats
import projektor.persistence.objectstore.CoverageStatsDocument
import projektor.persistence.objectstore.TestRunIndex
import projektor.persistence.objectstore.toStoredPrecision
import projektor.repository.testrun.matchesBranchType
import projektor.server.api.PublicId
import projektor.server.api.repository.BranchType
import projektor.server.api.repository.coverage.RepositoryCoverageTimeline
import projektor.server.api.repository.coverage.RepositoryCoverageTimelineEntry
import projektor.server.api.repository.coverage.RepositoryCurrentCoverage
import java.math.BigDecimal
import java.time.Instant

class RepositoryCoverageObjectStoreRepository(
    private val store: JsonObjectStore,
    private val testRunIndex: TestRunIndex,
) : RepositoryCoverageRepository {
    override suspend fun fetchRepositoryCoverageTimeline(
        branchType: BranchType,
        repoName: String,
        projectName: String?,
    ): RepositoryCoverageTimeline? {
        val timelineEntries =
            testRunIndex.readRepositoryEntries(repoName)
                .filter { it.matchesBranchType(branchType) && it.matchesProject(projectName) }
                .mapNotNull { entry ->
                    entry.coverageStats?.let { coverageStats ->
                        RepositoryCoverageTimelineEntry(
                            publicId = entry.publicId,
                            createdTimestamp = entry.createdTimestamp,
                            coverageStats = coverageStats.toReportStats().toCoverageStats(null),
                        )
                    }
                }

        return if (timelineEntries.isNotEmpty()) RepositoryCoverageTimeline(timelineEntries) else null
    }

    override suspend fun coverageExists(
        repoName: String,
        projectName: String?,
    ): Boolean = testRunIndex.readRepositoryEntries(repoName).any { it.matchesProject(projectName) && it.coverageStats != null }

    override suspend fun saveLastKnownCoverageIfNewer(
        repoName: String,
        projectName: String?,
        branchName: String?,
        coveredPercentage: BigDecimal,
        testRunPublicId: PublicId,
        createdTimestamp: Instant,
    ) {
        withContext(Dispatchers.IO) {
            val newCoverage =
                CurrentCoverageDocument(
                    repoName = repoName,
                    branchName = branchName,
                    coveredPercentage = coveredPercentage,
                    testRunPublicId = testRunPublicId.id,
                    createdTimestamp = createdTimestamp.toStoredPrecision(),
                )

            store.update<CurrentCoverageDocument>(currentCoverageKey(repoName, projectName)) { current ->
                if (current == null || current.createdTimestamp < newCoverage.createdTimestamp) newCoverage else null
            }
        }
    }

    override suspend fun fetchLastKnownCoverage(
        repoName: String,
        projectName: String?,
    ): RepositoryCurrentCoverage? =
        withContext(Dispatchers.IO) {
            store.read<CurrentCoverageDocument>(currentCoverageKey(repoName, projectName))?.let {
                RepositoryCurrentCoverage(
                    id = it.testRunPublicId,
                    createdTimestamp = it.createdTimestamp,
                    coveredPercentage = it.coveredPercentage,
                    repo = it.repoName,
                    project = projectName,
                    branch = it.branchName,
                )
            }
        }

    private fun currentCoverageKey(
        repoName: String,
        projectName: String?,
    ) = "${TestRunIndex.repositoryPrefix(repoName)}current-coverage/${TestRunIndex.projectKeySegment(projectName)}.json"

    data class CurrentCoverageDocument(
        val repoName: String,
        val branchName: String?,
        val coveredPercentage: BigDecimal,
        val testRunPublicId: String,
        val createdTimestamp: Instant,
    )
}

private fun CoverageStatsDocument.toReportStats() =
    CoverageReportStats(
        statementStat = CoverageReportStat(statementCovered, statementMissed),
        lineStat = CoverageReportStat(lineCovered, lineMissed),
        branchStat = CoverageReportStat(branchCovered, branchMissed),
    )
