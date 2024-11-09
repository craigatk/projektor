package projektor.error

import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.HttpStatusCode
import io.ktor.test.dispatcher.*
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.koin.ktor.ext.get
import projektor.ApplicationTestCase
import projektor.incomingresults.randomPublicId
import projektor.server.api.error.FailureBodyType
import projektor.server.api.error.ResultsProcessingFailure
import strikt.api.expectThat
import strikt.assertions.any
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import kotlin.test.assertNotNull

class ProcessingFailureApplicationTest : ApplicationTestCase() {
    override fun autoStartServer(): Boolean = true

    @Test
    fun `should fetch processing failures`() =
        testSuspend {
            val tooOldPublicIds = (1..5).map { randomPublicId() }
            val recentPublicIds = (1..5).map { randomPublicId() }

            val processingFailureService: ProcessingFailureService = getApplication().get()

            tooOldPublicIds.forEach { publicId ->
                runBlocking {
                    processingFailureService.recordProcessingFailure(
                        publicId = publicId,
                        body = "too-old",
                        bodyType = FailureBodyType.COVERAGE,
                        e = IllegalArgumentException("too-old"),
                    )
                }
            }

            recentPublicIds.forEach { publicId ->
                runBlocking {
                    processingFailureService.recordProcessingFailure(
                        publicId = publicId,
                        body = "recent-body-$publicId",
                        bodyType = FailureBodyType.COVERAGE,
                        e = IllegalArgumentException("recent-failure-$publicId"),
                    )
                }
            }

            val response = testClient.get("/failures/recent?count=5")

            expectThat(response.status).isEqualTo(HttpStatusCode.OK)

            val responseBody = response.bodyAsText()
            assertNotNull(responseBody)

            val failures: List<ResultsProcessingFailure> = objectMapper.readValue(responseBody)
            expectThat(failures).hasSize(5)
                .any {
                    get { body }.isEqualTo("recent-body-${recentPublicIds[0]}")
                    get { failureMessage }.isEqualTo("recent-failure-${recentPublicIds[0]}")
                }
                .any {
                    get { body }.isEqualTo("recent-body-${recentPublicIds[1]}")
                    get { failureMessage }.isEqualTo("recent-failure-${recentPublicIds[1]}")
                }
                .any {
                    get { body }.isEqualTo("recent-body-${recentPublicIds[2]}")
                    get { failureMessage }.isEqualTo("recent-failure-${recentPublicIds[2]}")
                }
                .any {
                    get { body }.isEqualTo("recent-body-${recentPublicIds[3]}")
                    get { failureMessage }.isEqualTo("recent-failure-${recentPublicIds[3]}")
                }
                .any {
                    get { body }.isEqualTo("recent-body-${recentPublicIds[4]}")
                    get { failureMessage }.isEqualTo("recent-failure-${recentPublicIds[4]}")
                }
        }
}
