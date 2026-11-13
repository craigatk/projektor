package projektor.quality

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jooq.DSLContext
import projektor.database.generated.Tables.CODE_QUALITY_REPORT
import projektor.database.generated.Tables.TEST_RUN
import projektor.database.generated.tables.daos.CodeQualityReportDao
import projektor.server.api.PublicId
import projektor.server.api.quality.CodeQualityReport
import projektor.util.fetchTestRunId
import projektor.database.generated.tables.pojos.CodeQualityReport as CodeQualityReportDB

class CodeQualityReportDatabaseRepository(private val dslContext: DSLContext) : CodeQualityReportRepository {
    private val codeQualityReportDao = CodeQualityReportDao(dslContext.configuration())

    override suspend fun insertCodeQualityReports(
        publicId: PublicId,
        codeQualityReports: List<CodeQualityReport>,
    ) = withContext(Dispatchers.IO) {
        val testRunId = fetchTestRunId(dslContext, publicId)

        codeQualityReports.forEach { codeQualityReport ->
            val codeQualityReportDB = CodeQualityReportDB()
            codeQualityReportDB.testRunId = testRunId
            codeQualityReportDB.idx = codeQualityReport.idx
            codeQualityReportDB.contents = codeQualityReport.contents
            codeQualityReportDB.fileName = codeQualityReport.fileName
            codeQualityReportDB.groupName = codeQualityReport.groupName

            codeQualityReportDao.insert(codeQualityReportDB)
        }
    }

    override suspend fun fetchCodeQualityReports(publicId: PublicId): List<CodeQualityReport> =
        withContext(Dispatchers.IO) {
            dslContext
                .select(
                    CODE_QUALITY_REPORT.IDX,
                    CODE_QUALITY_REPORT.CONTENTS,
                    CODE_QUALITY_REPORT.FILE_NAME,
                    CODE_QUALITY_REPORT.GROUP_NAME,
                )
                .from(CODE_QUALITY_REPORT)
                .innerJoin(TEST_RUN).on(CODE_QUALITY_REPORT.TEST_RUN_ID.eq(TEST_RUN.ID))
                .where(TEST_RUN.PUBLIC_ID.eq(publicId.id))
                .fetch { record ->
                    CodeQualityReport(
                        idx = record.get(CODE_QUALITY_REPORT.IDX),
                        contents = record.get(CODE_QUALITY_REPORT.CONTENTS),
                        fileName = record.get(CODE_QUALITY_REPORT.FILE_NAME),
                        groupName = record.get(CODE_QUALITY_REPORT.GROUP_NAME),
                    )
                }
        }
}
