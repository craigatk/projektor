package projektor.error

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jooq.DSLContext
import projektor.database.generated.Tables
import projektor.database.generated.tables.daos.ResultsProcessingFailureDao
import projektor.server.api.PublicId
import projektor.server.api.error.FailureBodyType
import projektor.server.api.error.ResultsProcessingFailure
import java.time.LocalDateTime
import java.time.ZoneOffset
import projektor.database.generated.tables.pojos.ResultsProcessingFailure as ResultsProcessingFailureDB

class ProcessingFailureDatabaseRepository(private val dslContext: DSLContext) : ProcessingFailureRepository {
    private val resultsProcessingFailureDao = ResultsProcessingFailureDao(dslContext.configuration())

    override suspend fun recordProcessingFailure(
        publicId: PublicId,
        body: String,
        bodyType: FailureBodyType,
        failureMessage: String?,
    ): ResultsProcessingFailure {
        val resultsProcessingFailureDB = ResultsProcessingFailureDB()
        resultsProcessingFailureDB.publicId = publicId.id
        resultsProcessingFailureDB.body = body
        resultsProcessingFailureDB.bodyType = bodyType.name
        resultsProcessingFailureDB.failureMessage = failureMessage
        resultsProcessingFailureDB.createdTimestamp = LocalDateTime.now()
        resultsProcessingFailureDao.insert(resultsProcessingFailureDB)

        return ResultsProcessingFailure(
            id = publicId.id,
            body = body,
            bodyType = bodyType,
            failureMessage = failureMessage,
            createdTimestamp = resultsProcessingFailureDB.createdTimestamp.toInstant(ZoneOffset.UTC),
        )
    }

    override suspend fun fetchRecentProcessingFailures(failureCount: Int): List<ResultsProcessingFailure> =
        withContext(Dispatchers.IO) {
            dslContext
                .select(Tables.RESULTS_PROCESSING_FAILURE.PUBLIC_ID.`as`("id"))
                .select(Tables.RESULTS_PROCESSING_FAILURE.fields().toList())
                .from(Tables.RESULTS_PROCESSING_FAILURE)
                .orderBy(Tables.RESULTS_PROCESSING_FAILURE.CREATED_TIMESTAMP.desc().nullsLast())
                .limit(failureCount)
                .fetchInto(ResultsProcessingFailure::class.java)
        }
}
