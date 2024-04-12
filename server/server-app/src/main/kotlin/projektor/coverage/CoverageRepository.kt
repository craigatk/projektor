package projektor.coverage

import projektor.database.generated.tables.pojos.CodeCoverageGroup
import projektor.database.generated.tables.pojos.CodeCoverageRun
import projektor.parser.coverage.model.CoverageReport
import projektor.parser.coverage.model.CoverageReportStats
import projektor.server.api.PublicId
import projektor.server.api.coverage.CoverageFile
import projektor.server.api.coverage.CoverageStat

interface CoverageRepository {
    suspend fun createOrGetCoverageRun(publicId: PublicId): CodeCoverageRun

    suspend fun addCoverageReport(
        coverageRun: CodeCoverageRun,
        coverageReport: CoverageReport,
    ): CodeCoverageGroup

    suspend fun upsertCoverageGroup(
        coverageRun: CodeCoverageRun,
        coverageReport: CoverageReport,
        newLineStat: CoverageStat,
    ): Pair<CodeCoverageGroup, CoverageGroupStatus>

    suspend fun insertCoverageFiles(
        coverageReportFiles: List<CoverageFile>,
        coverageRun: CodeCoverageRun,
        coverageGroup: CodeCoverageGroup,
    )

    suspend fun upsertCoverageFiles(
        coverageReportFiles: List<CoverageFile>,
        coverageRun: CodeCoverageRun,
        coverageGroup: CodeCoverageGroup,
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
