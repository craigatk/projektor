package projektor.testrun.attributes

import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
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
    fun `should fetch test run attributes`() {
        val publicId = randomPublicId()

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/run/$publicId/attributes") {
                testRunDBGenerator.createTestRun(
                    publicId,
                    listOf(),
                )

                testRunSystemAttributesDao.insert(TestRunSystemAttributes(publicId.id, true))
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)

                val attributesResponse = objectMapper.readValue(response.content, TestRunSystemAttributes::class.java)
                assertNotNull(attributesResponse)

                expectThat(attributesResponse.pinned).isTrue()
            }
        }
    }

    @Test
    fun `when system attributes does not exist should return 404`() {
        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/run/iddoesnotexist/attributes") {
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.NotFound)
            }
        }
    }

    @Test
    fun `should pin test run`() {
        val publicId = randomPublicId()

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Post, "/run/$publicId/attributes/pin") {
                testRunDBGenerator.createTestRun(
                    publicId,
                    listOf(),
                )

                testRunSystemAttributesDao.insert(TestRunSystemAttributes(publicId.id, false))
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)

                expectThat(testRunSystemAttributesDao.fetchOneByTestRunPublicId(publicId.id))
                    .isNotNull()
                    .and { get { pinned }.isTrue() }
            }
        }
    }

    @Test
    fun `when trying to pin a test run that does not exist should return 404`() {
        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Post, "/run/doesnotexist/attributes/pin") {
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.NotFound)
            }
        }
    }

    @Test
    fun `should unpin test run`() {
        val publicId = randomPublicId()

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Post, "/run/$publicId/attributes/unpin") {
                testRunDBGenerator.createTestRun(
                    publicId,
                    listOf(),
                )

                testRunSystemAttributesDao.insert(TestRunSystemAttributes(publicId.id, true))
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)

                expectThat(testRunSystemAttributesDao.fetchOneByTestRunPublicId(publicId.id))
                    .isNotNull()
                    .and { get { pinned }.isFalse() }
            }
        }
    }

    @Test
    fun `when trying to unpin a test run that does not exist should return 404`() {
        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Post, "/run/doesnotexist/attributes/unpin") {
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.NotFound)
            }
        }
    }
}
