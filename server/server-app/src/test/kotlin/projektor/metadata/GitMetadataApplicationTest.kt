package projektor.metadata

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.HttpStatusCode
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.ApplicationTestCaseConfig
import projektor.incomingresults.randomPublicId
import projektor.server.api.metadata.TestRunGitMetadata
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isFalse
import strikt.assertions.isNotNull
import strikt.assertions.isNull
import strikt.assertions.isTrue
import kotlin.test.assertNotNull

class GitMetadataApplicationTest : ApplicationTestCase() {
    @Test
    fun `should get Git metadata for test run`() =
        projektorTestApplication(
            ApplicationTestCaseConfig(),
        ) {
            val publicId = randomPublicId()
            val anotherPublicId = randomPublicId()

            val testRun = testRunDBGenerator.createSimpleTestRun(publicId)
            testRunDBGenerator.addGitMetadata(testRun, "projektor/projektor", true, "main", null, null, null)

            val anotherTestRun = testRunDBGenerator.createSimpleTestRun(anotherPublicId)
            testRunDBGenerator.addGitMetadata(anotherTestRun, "projektor/another", true, "main", null, null, null)

            val response = client.get("/run/$publicId/metadata/git")
            expectThat(response.status).isEqualTo(HttpStatusCode.OK)

            val gitMetadata = objectMapper.readValue(response.bodyAsText(), TestRunGitMetadata::class.java)
            assertNotNull(gitMetadata)

            expectThat(gitMetadata) {
                get { repoName }.isEqualTo("projektor/projektor")
                get { orgName }.isEqualTo("projektor")
                get { branchName }.isEqualTo("main")
                get { projectName }.isNull()
                get { isMainBranch }.isTrue()
            }
        }

    @Test
    fun `should get Git metadata for test run with pull request number and commit SHA`() =
        projektorTestApplication(
            ApplicationTestCaseConfig(),
        ) {
            val publicId = randomPublicId()
            val anotherPublicId = randomPublicId()

            val testRun = testRunDBGenerator.createSimpleTestRun(publicId)
            testRunDBGenerator.addGitMetadata(testRun, "projektor/projektor", true, "main", null, 4, "commitSHA")

            val anotherTestRun = testRunDBGenerator.createSimpleTestRun(anotherPublicId)
            testRunDBGenerator.addGitMetadata(anotherTestRun, "projektor/another", true, "main", null, null, null)

            val response = client.get("/run/$publicId/metadata/git")
            expectThat(response.status).isEqualTo(HttpStatusCode.OK)

            val gitMetadata = objectMapper.readValue(response.bodyAsText(), TestRunGitMetadata::class.java)
            assertNotNull(gitMetadata)

            expectThat(gitMetadata) {
                get { repoName }.isEqualTo("projektor/projektor")
                get { orgName }.isEqualTo("projektor")
                get { branchName }.isEqualTo("main")
                get { projectName }.isNull()
                get { isMainBranch }.isTrue()
                get { pullRequestNumber }.isEqualTo(4)
                get { commitSha }.isEqualTo("commitSHA")
            }
        }

    @Test
    fun `should get Git metadata for test run with project name`() =
        projektorTestApplication(
            ApplicationTestCaseConfig(),
        ) {
            val publicId = randomPublicId()
            val anotherPublicId = randomPublicId()

            val testRun = testRunDBGenerator.createSimpleTestRun(publicId)
            testRunDBGenerator.addGitMetadata(testRun, "projektor/projektor", true, "main", "my-project", null, null)

            val anotherTestRun = testRunDBGenerator.createSimpleTestRun(anotherPublicId)
            testRunDBGenerator.addGitMetadata(anotherTestRun, "projektor/another", true, "main", null, null, null)

            val response = client.get("/run/$publicId/metadata/git")
            expectThat(response.status).isEqualTo(HttpStatusCode.OK)

            val gitMetadata = objectMapper.readValue(response.bodyAsText(), TestRunGitMetadata::class.java)
            assertNotNull(gitMetadata)

            expectThat(gitMetadata) {
                get { repoName }.isEqualTo("projektor/projektor")
                get { orgName }.isEqualTo("projektor")
                get { branchName }.isEqualTo("main")
                get { projectName }.isEqualTo("my-project")
                get { isMainBranch }.isTrue()
            }
        }

    @Test
    fun `when GitHub base URL should return it`() =
        projektorTestApplication(
            ApplicationTestCaseConfig(
                gitHubBaseUrl = "http://git.localhost:8080/",
            ),
        ) {
            val publicId = randomPublicId()

            val testRun = testRunDBGenerator.createSimpleTestRun(publicId)
            testRunDBGenerator.addGitMetadata(testRun, "projektor/projektor", false, "feature/branch", null, null, null)

            val response = client.get("/run/$publicId/metadata/git")
            expectThat(response.status).isEqualTo(HttpStatusCode.OK)

            val gitMetadata = objectMapper.readValue(response.bodyAsText(), TestRunGitMetadata::class.java)
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

    @Test
    fun `when no Git metadata for test run should return 204`() =
        projektorTestApplication(
            ApplicationTestCaseConfig(),
        ) {
            val publicId = randomPublicId()

            testRunDBGenerator.createSimpleTestRun(publicId)

            val response = client.get("/run/$publicId/metadata/git")
            expectThat(response.status).isEqualTo(HttpStatusCode.NoContent)
        }
}
