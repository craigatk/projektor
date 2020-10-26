package projektor.error

import org.jooq.DSLContext
import projektor.database.generated.tables.daos.ProcessingFailureDao
import projektor.database.generated.tables.pojos.ProcessingFailure
import projektor.server.api.PublicId
import java.time.LocalDateTime

class ProcessingFailureDatabaseRepository(dslContext: DSLContext) : ProcessingFailureRepository {
    private val processingFailureDao = ProcessingFailureDao(dslContext.configuration())

    override suspend fun recordProcessingFailure(publicId: PublicId, body: String, bodyType: FailureBodyType, failure: String?) {
        val processingFailure = ProcessingFailure()
        processingFailure.publicId = publicId.id
        processingFailure.body = body
        processingFailure.bodyType = bodyType.name
        processingFailure.failure = failure
        processingFailure.createdTimestamp = LocalDateTime.now()

        processingFailureDao.insert(processingFailure)
    }
}
