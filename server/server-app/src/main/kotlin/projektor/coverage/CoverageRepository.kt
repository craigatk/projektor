package projektor.coverage

import projektor.parser.coverage.model.CoverageReport
import projektor.parser.coverage.model.CoverageReportStats
import projektor.server.api.PublicId
import projektor.server.api.coverage.CoverageFile

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
     * Appends the report to the coverage group with the report's name. If the group doesn't exist yet, adds it
     * with the report's stats and files. Otherwise combines the report's files with the group's files (see
     * [combineCoverageFiles]) and updates the group's line stats to match (zeroing its statement and branch stats).
     *
     * Atomic with respect to concurrent appends to the same test run.
     */
    suspend fun appendCoverageReport(
        publicId: PublicId,
        coverageReport: CoverageReport,
        incomingCoverageFiles: List<CoverageFile>,
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
