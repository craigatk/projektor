package projektor.error

import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
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

    @Test
    fun `should fetch processing failures`() {
        val tooOldPublicIds = (1..5).map { randomPublicId() }
        val recentPublicIds = (1..5).map { randomPublicId() }

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/failures/recent?count=5") {
                val processingFailureService: ProcessingFailureService = application.get()

                tooOldPublicIds.forEach { publicId ->
                    runBlocking {
                        processingFailureService.recordProcessingFailure(
                            publicId = publicId,
                            body = "too-old",
                            bodyType = FailureBodyType.COVERAGE,
                            e = IllegalArgumentException("too-old")
                        )
                    }
                }

                recentPublicIds.forEach { publicId ->
                    runBlocking {
                        processingFailureService.recordProcessingFailure(
                            publicId = publicId,
                            body = "recent-body-$publicId",
                            bodyType = FailureBodyType.COVERAGE,
                            e = IllegalArgumentException("recent-failure-$publicId")
                        )
                    }
                }
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)

                val responseBody = response.content
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
    }
}
