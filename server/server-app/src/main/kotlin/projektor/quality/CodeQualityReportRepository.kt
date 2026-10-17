package projektor.quality

import projektor.server.api.PublicId
import projektor.server.api.quality.CodeQualityReport

interface CodeQualityReportRepository {
    suspend fun insertCodeQualityReports(
        publicId: PublicId,
        codeQualityReports: List<CodeQualityReport>,
    )

    suspend fun fetchCodeQualityReports(publicId: PublicId): List<CodeQualityReport>
}
