package projektor.incomingresults.processing

import java.sql.Timestamp
import java.time.LocalDateTime
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jooq.DSLContext
import org.simpleflatmapper.jdbc.JdbcMapperFactory
import projektor.database.generated.Tables
import projektor.database.generated.tables.daos.ResultsProcessingDao
import projektor.database.generated.tables.pojos.ResultsProcessing as ResultsProcessingDB
import projektor.server.api.PublicId
import projektor.server.api.results.ResultsProcessing
import projektor.server.api.results.ResultsProcessingStatus

class ResultsProcessingDatabaseRepository(private val dslContext: DSLContext) : ResultsProcessingRepository {
    private val resultsProcessingDao = ResultsProcessingDao(dslContext.configuration())

    private val resultsProcessingMapper = JdbcMapperFactory.newInstance()
            .addKeys("public_id")
            .ignorePropertyNotFound()
            .newMapper(ResultsProcessing::class.java)

    override suspend fun createResultsProcessing(publicId: PublicId): ResultsProcessing =
            withContext(Dispatchers.IO) {
                val createdTimestamp = LocalDateTime.now()

                val resultsProcessingDB = ResultsProcessingDB()
                        .setPublicId(publicId.id)
                        .setStatus(ResultsProcessingStatus.RECEIVED.name)
                        .setCreatedTimestamp(Timestamp.valueOf(createdTimestamp))
                resultsProcessingDao.insert(resultsProcessingDB)

                ResultsProcessing(publicId.id, ResultsProcessingStatus.RECEIVED, createdTimestamp, null)
            }

    override suspend fun updateResultsProcessingStatus(publicId: PublicId, newStatus: ResultsProcessingStatus): Boolean =
            withContext(Dispatchers.IO) {
                dslContext.update(Tables.RESULTS_PROCESSING)
                        .set(Tables.RESULTS_PROCESSING.STATUS, newStatus.name)
                        .where(Tables.RESULTS_PROCESSING.PUBLIC_ID.eq(publicId.id))
                        .execute() > 0
            }

    override suspend fun recordResultsProcessingError(publicId: PublicId, errorMessage: String?): Boolean =
            withContext(Dispatchers.IO) {
                dslContext.update(Tables.RESULTS_PROCESSING)
                        .set(Tables.RESULTS_PROCESSING.STATUS, ResultsProcessingStatus.ERROR.name)
                        .set(Tables.RESULTS_PROCESSING.ERROR_MESSAGE, errorMessage)
                        .where(Tables.RESULTS_PROCESSING.PUBLIC_ID.eq(publicId.id))
                        .execute() > 0
            }

    override suspend fun fetchResultsProcessing(publicId: PublicId): ResultsProcessing? =
            withContext(Dispatchers.IO) {
                val resultSet = dslContext
                        .select(Tables.RESULTS_PROCESSING.fields().toList())
                        .select(Tables.RESULTS_PROCESSING.PUBLIC_ID.`as`("id"))
                        .from(Tables.RESULTS_PROCESSING)
                        .where(Tables.RESULTS_PROCESSING.PUBLIC_ID.eq(publicId.id))
                        .limit(1)
                        .fetchResultSet()

                resultSet.use { resultsProcessingMapper.stream(it).findFirst().orElse(null) }
            }
}
