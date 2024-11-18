package projektor.testrun.attributes

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.HttpStatusCode
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.database.generated.tables.pojos.TestRunSystemAttributes
import projektor.incomingresults.randomPublicId
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isNotNull
import strikt.assertions.isTrue
import kotlin.test.assertNotNull

class TestRunSystemAttributesApplicationTest : ApplicationTestCase() {
    @Test
    fun `should fetch test run attributes`() =
        projektorTestApplication {
            val publicId = randomPublicId()

            testRunDBGenerator.createTestRun(
                publicId,
                listOf(),
            )

            testRunSystemAttributesDao.insert(TestRunSystemAttributes(publicId.id, true))

            val response = client.get("/run/$publicId/attributes")

            expectThat(response.status).isEqualTo(HttpStatusCode.OK)

            val attributesResponse = objectMapper.readValue(response.bodyAsText(), TestRunSystemAttributes::class.java)
            assertNotNull(attributesResponse)

            expectThat(attributesResponse.pinned).isTrue()
        }

    @Test
    fun `when system attributes does not exist should return 404`() =
        projektorTestApplication {
            val response = client.get("/run/iddoesnotexist/attributes")

            expectThat(response.status).isEqualTo(HttpStatusCode.NotFound)
        }

    @Test
    fun `should pin test run`() =
        projektorTestApplication {
            val publicId = randomPublicId()

            testRunDBGenerator.createTestRun(
                publicId,
                listOf(),
            )

            testRunSystemAttributesDao.insert(TestRunSystemAttributes(publicId.id, false))

            val response = client.post("/run/$publicId/attributes/pin")

            expectThat(response.status).isEqualTo(HttpStatusCode.OK)

            expectThat(testRunSystemAttributesDao.fetchOneByTestRunPublicId(publicId.id))
                .isNotNull()
                .and { get { pinned }.isTrue() }
        }

    @Test
    fun `when trying to pin a test run that does not exist should return 404`() =
        projektorTestApplication {
            val response = client.post("/run/doesnotexist/attributes/pin")

            expectThat(response.status).isEqualTo(HttpStatusCode.NotFound)
        }

    @Test
    fun `should unpin test run`() =
        projektorTestApplication {
            val publicId = randomPublicId()

            testRunDBGenerator.createTestRun(
                publicId,
                listOf(),
            )

            testRunSystemAttributesDao.insert(TestRunSystemAttributes(publicId.id, true))

            val response = client.post("/run/$publicId/attributes/unpin")

            expectThat(response.status).isEqualTo(HttpStatusCode.OK)

            expectThat(testRunSystemAttributesDao.fetchOneByTestRunPublicId(publicId.id))
                .isNotNull()
                .and { get { pinned }.isFalse() }
        }

    @Test
    fun `when trying to unpin a test run that does not exist should return 404`() =
        projektorTestApplication {
            val response = client.post("/run/doesnotexist/attributes/unpin")

            expectThat(response.status).isEqualTo(HttpStatusCode.NotFound)
        }
}
