package projektor.coverage

import projektor.database.generated.tables.pojos.CodeCoverageGroup
import projektor.database.generated.tables.pojos.CodeCoverageRun
import projektor.parser.coverage.model.CoverageReport
import projektor.parser.coverage.model.CoverageReportStats
import projektor.server.api.PublicId

interface CoverageRepository {
    suspend fun createOrGetCoverageRun(publicId: PublicId): CodeCoverageRun

    suspend fun addCoverageReport(coverageRun: CodeCoverageRun, coverageReport: CoverageReport): CodeCoverageGroup

    suspend fun fetchOverallStats(publicId: PublicId): CoverageReportStats

    suspend fun fetchCoverageList(publicId: PublicId): List<CoverageReport>

    suspend fun coverageExists(publicId: PublicId): Boolean
}
