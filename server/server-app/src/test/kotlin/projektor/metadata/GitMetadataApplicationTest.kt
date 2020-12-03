package projektor.metadata

import io.ktor.http.*
import io.ktor.server.testing.*
import io.ktor.util.*
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.incomingresults.randomPublicId
import projektor.server.api.metadata.TestRunGitMetadata
import strikt.api.expectThat
import strikt.assertions.*
import kotlin.test.assertNotNull

@KtorExperimentalAPI
class GitMetadataApplicationTest : ApplicationTestCase() {
    @Test
    fun `should get Git metadata for test run`() {
        val publicId = randomPublicId()
        val anotherPublicId = randomPublicId()

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/run/$publicId/metadata/git") {
                val testRun = testRunDBGenerator.createSimpleTestRun(publicId)
                testRunDBGenerator.addGitMetadata(testRun, "projektor/projektor", true, "main", null)

                val anotherTestRun = testRunDBGenerator.createSimpleTestRun(anotherPublicId)
                testRunDBGenerator.addGitMetadata(anotherTestRun, "projektor/another", true, "main", null)
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)

                val gitMetadata = objectMapper.readValue(response.content, TestRunGitMetadata::class.java)
                assertNotNull(gitMetadata)

                expectThat(gitMetadata) {
                    get { repoName }.isEqualTo("projektor/projektor")
                    get { orgName }.isEqualTo("projektor")
                    get { branchName }.isEqualTo("main")
                    get { projectName }.isNull()
                    get { isMainBranch }.isTrue()
                }
            }
        }
    }

    @Test
    fun `should get Git metadata for test run with project name`() {
        val publicId = randomPublicId()
        val anotherPublicId = randomPublicId()

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/run/$publicId/metadata/git") {
                val testRun = testRunDBGenerator.createSimpleTestRun(publicId)
                testRunDBGenerator.addGitMetadata(testRun, "projektor/projektor", true, "main", "my-project")

                val anotherTestRun = testRunDBGenerator.createSimpleTestRun(anotherPublicId)
                testRunDBGenerator.addGitMetadata(anotherTestRun, "projektor/another", true, "main", null)
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)

                val gitMetadata = objectMapper.readValue(response.content, TestRunGitMetadata::class.java)
                assertNotNull(gitMetadata)

                expectThat(gitMetadata) {
                    get { repoName }.isEqualTo("projektor/projektor")
                    get { orgName }.isEqualTo("projektor")
                    get { branchName }.isEqualTo("main")
                    get { projectName }.isEqualTo("my-project")
                    get { isMainBranch }.isTrue()
                }
            }
        }
    }

    @Test
    fun `when GitHub base URL should return it`() {
        val publicId = randomPublicId()

        gitHubBaseUrl = "http://git.localhost:8080/"

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/run/$publicId/metadata/git") {
                val testRun = testRunDBGenerator.createSimpleTestRun(publicId)
                testRunDBGenerator.addGitMetadata(testRun, "projektor/projektor", false, "feature/branch", null)
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)

                val gitMetadata = objectMapper.readValue(response.content, TestRunGitMetadata::class.java)
                assertNotNull(gitMetadata)

                expectThat(gitMetadata) {
                    get { gitHubBaseUrl }.isNotNull().isEqualTo("http://git.localhost:8080/")
                    get { repoName }.isEqualTo("projektor/projektor")
                    get { orgName }.isEqualTo("projektor")
                    get { branchName }.isEqualTo("feature/branch")
                    get { isMainBranch }.isFalse()
                    get { projectName }.isNull()
                }
            }
        }
    }

    @Test
    fun `when no Git metadata for test run should return 204`() {
        val publicId = randomPublicId()

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/run/$publicId/metadata/git") {
                testRunDBGenerator.createSimpleTestRun(publicId)
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.NoContent)
            }
        }
    }
}
