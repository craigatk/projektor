package projektor.coverage

import projektor.database.generated.tables.pojos.CodeCoverageGroup
import projektor.database.generated.tables.pojos.CodeCoverageRun
import projektor.parser.coverage.model.CoverageReport
import projektor.server.api.PublicId

interface CoverageRepository {
    suspend fun createOrGetCoverageRun(publicId: PublicId): CodeCoverageRun

    suspend fun addCoverageReport(coverageRun: CodeCoverageRun, coverageReport: CoverageReport): CodeCoverageGroup
}