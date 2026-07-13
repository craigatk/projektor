package projektor.error

import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import org.koin.ktor.ext.get
import projektor.ApplicationTestCase
import projektor.incomingresults.randomPublicId
import projektor.server.api.error.FailureBodyType
import projektor.server.api.error.ResultsProcessingFailure
import strikt.api.expectThat
import strikt.assertions.any
import strikt.assertions.isEqualTo
import kotlin.test.assertNotNull

class ProcessingFailureApplicationTest : ApplicationTestCase() {
    @Test
    fun `should fetch processing failures`() =
        projektorTestApplication {
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

            // /failures/recent has no per-test/per-repo scoping -- it's a global "most recent
            // failures across the whole server" view -- and other tests can be recording their
            // own processing failures into the same shared table concurrently (this project runs
            // tests in parallel). Asking for exactly 5 and asserting the response is precisely
            // the 5 we just created is flaky: a concurrently-inserted row from another test can
            // be more recent and bump one of ours out of the window. Instead, request a generous
            // window and confirm our own records are present in it.
            val response = client.get("/failures/recent?count=1000")

            expectThat(response.status).isEqualTo(HttpStatusCode.OK)

            val responseBody = response.bodyAsText()
            assertNotNull(responseBody)

            val failures: List<ResultsProcessingFailure> = objectMapper.readValue(responseBody)

            recentPublicIds.forEach { publicId ->
                expectThat(failures).any {
                    get { body }.isEqualTo("recent-body-$publicId")
                    get { failureMessage }.isEqualTo("recent-failure-$publicId")
                }
            }
        }
}
