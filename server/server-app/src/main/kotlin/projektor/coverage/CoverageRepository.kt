package projektor.coverage

import projektor.parser.coverage.model.CoverageReport
import projektor.parser.coverage.model.CoverageReportStats
import projektor.server.api.PublicId
import projektor.server.api.coverage.CoverageFile
import projektor.server.api.coverage.CoverageStat

/**
 * Coverage for a test run is made up of groups (one per coverage report), each with its own stats and files.
 * Groups are identified by the test run's public ID and the group (report) name.
 */
interface CoverageRepository {
    suspend fun createOrGetCoverageRun(publicId: PublicId)

    /**
     * Always adds a new coverage group with the report's stats and the given files.
     */
    suspend fun addCoverageReport(
        publicId: PublicId,
        coverageReport: CoverageReport,
        coverageReportFiles: List<CoverageFile>,
    )

    /**
     * Adds a new coverage group with the report's stats if one with the report's name doesn't exist yet.
     * Otherwise updates the existing group's line stats to newLineStat (and zeroes its statement and branch stats).
     */
    suspend fun upsertCoverageGroup(
        publicId: PublicId,
        coverageReport: CoverageReport,
        newLineStat: CoverageStat,
    ): CoverageGroupStatus

    suspend fun insertCoverageFiles(
        publicId: PublicId,
        groupName: String,
        coverageReportFiles: List<CoverageFile>,
    )

    /**
     * Inserts files whose path isn't in the group yet. For files already in the group, updates their missed lines
     * and line stats (and clears their partial lines and zeroes their statement and branch stats).
     */
    suspend fun upsertCoverageFiles(
        publicId: PublicId,
        groupName: String,
        coverageReportFiles: List<CoverageFile>,
    )

    suspend fun fetchOverallStats(publicId: PublicId): CoverageReportStats

    suspend fun fetchCoverageList(publicId: PublicId): List<CoverageReport>

    suspend fun coverageGroupExists(publicId: PublicId): Boolean

    suspend fun fetchCoverageFiles(
        publicId: PublicId,
        groupName: String,
    ): List<CoverageFile>

    suspend fun deleteCoverage(publicId: PublicId): Boolean
}
