package projektor.error

import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.HttpStatusCode
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.incomingresults.randomPublicId
import projektor.server.api.PublicId
import projektor.server.api.error.FailureBodyType
import projektor.server.api.error.ResultsProcessingFailure
import strikt.api.expectThat
import strikt.assertions.containsExactly
import strikt.assertions.isEqualTo
import strikt.assertions.map
import java.time.LocalDateTime
import projektor.database.generated.tables.pojos.ResultsProcessingFailure as ResultsProcessingFailureDB

class ProcessingFailureApplicationTest : ApplicationTestCase() {
    @Test
    fun `should fetch processing failures`() =
        projektorTestApplication {
            // /failures/recent is a global "most recent across the server" view, the test database
            // isn't truncated between runs, and parallel test forks record their own processing
            // failures into the same table. Timestamp this test's failures slightly in the future
            // so they're deterministically the most recent rows, rather than scanning a large window
            // of every failure ever recorded (which grows without bound and eventually hangs).
            val baseTimestamp = LocalDateTime.now().plusMinutes(5)

            val olderPublicIds = (1..5).map { randomPublicId() }
            val recentPublicIds = (1..5).map { randomPublicId() }

            olderPublicIds.forEachIndexed { idx, publicId ->
                insertProcessingFailure(publicId, "older-body", "older-failure", baseTimestamp.plusSeconds(idx.toLong()))
            }
            recentPublicIds.forEachIndexed { idx, publicId ->
                insertProcessingFailure(
                    publicId,
                    "recent-body-$publicId",
                    "recent-failure-$publicId",
                    baseTimestamp.plusMinutes(1).plusSeconds(idx.toLong()),
                )
            }

            val response = client.get("/failures/recent?count=5")

            expectThat(response.status).isEqualTo(HttpStatusCode.OK)

            val failures: List<ResultsProcessingFailure> = objectMapper.readValue(response.bodyAsText())

            expectThat(failures)
                .map(ResultsProcessingFailure::id)
                .containsExactly(recentPublicIds.reversed().map(PublicId::id))

            expectThat(failures.first()) {
                get { body }.isEqualTo("recent-body-${recentPublicIds.last()}")
                get { bodyType }.isEqualTo(FailureBodyType.COVERAGE)
                get { failureMessage }.isEqualTo("recent-failure-${recentPublicIds.last()}")
            }
        }

    private fun insertProcessingFailure(
        publicId: PublicId,
        body: String,
        failureMessage: String,
        createdTimestamp: LocalDateTime,
    ) {
        val failureDB = ResultsProcessingFailureDB()
        failureDB.publicId = publicId.id
        failureDB.body = body
        failureDB.bodyType = FailureBodyType.COVERAGE.name
        failureDB.failureMessage = failureMessage
        failureDB.createdTimestamp = createdTimestamp
        resultsProcessingFailureDao.insert(failureDB)
    }
}
