package projektor.quality

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jooq.DSLContext
import org.simpleflatmapper.jdbc.JdbcMapperFactory
import projektor.database.generated.Tables.CODE_QUALITY_REPORT
import projektor.database.generated.Tables.TEST_RUN
import projektor.server.api.PublicId
import projektor.server.api.quality.CodeQualityReport
import kotlin.streams.toList

class CodeQualityReportDatabaseRepository(private val dslContext: DSLContext) : CodeQualityReportRepository {

    private val codeQualityMapper = JdbcMapperFactory.newInstance()
        .addKeys("id")
        .ignorePropertyNotFound()
        .newMapper(CodeQualityReport::class.java)

    override suspend fun fetchCodeQualityReports(publicId: PublicId): List<CodeQualityReport> =
        withContext(Dispatchers.IO) {
            val resultSet = dslContext
                .select(CODE_QUALITY_REPORT.fields().toList())
                .from(CODE_QUALITY_REPORT)
                .innerJoin(TEST_RUN).on(CODE_QUALITY_REPORT.TEST_RUN_ID.eq(TEST_RUN.ID))
                .where(TEST_RUN.PUBLIC_ID.eq(publicId.id))
                .fetchResultSet()

            resultSet.use { codeQualityMapper.stream(it).toList() }
        }
}
