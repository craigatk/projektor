package projektor.api

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.test.dispatcher.*
import org.apache.commons.lang3.RandomStringUtils
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.incomingresults.randomPublicId
import projektor.server.api.organization.OrganizationCurrentCoverage
import projektor.server.example.coverage.JacocoXmlLoader
import strikt.api.expectThat
import strikt.assertions.doesNotContain
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import kotlin.test.assertNotNull

class ApiOrganizationApplicationTest : ApplicationTestCase() {
    override fun autoStartServer() = true

    @Test
    fun `when three repos in org should find their coverage data`() =
        testSuspend {
            val orgName = RandomStringUtils.randomAlphabetic(12)

            val publicId1 = randomPublicId()
            val repo1 = "$orgName/repo1"
            val olderRunRepo1 = randomPublicId()
            val otherProjectRepo1 = randomPublicId()

            val publicId2 = randomPublicId()
            val repo2 = "$orgName/repo2"
            val olderRunRepo2 = randomPublicId()

            val publicId3 = randomPublicId()
            val repo3 = "$orgName/repo3"
            val olderRunRepo3 = randomPublicId()

            val anotherPublicId = randomPublicId()
            val anotherRepo = "another-org/repo"

            val noCodeCoveragePublicId = randomPublicId()
            val noCodeCoverageRepo = "$orgName/no-coverage"

            testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
                publicId = olderRunRepo1,
                coverageText = JacocoXmlLoader().serverApp(),
                repoName = repo1,
                projectName = "proj1",
            )
            testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
                publicId = publicId1,
                coverageText = JacocoXmlLoader().serverApp(),
                repoName = repo1,
                projectName = "proj1",
            )
            testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
                publicId = otherProjectRepo1,
                coverageText = JacocoXmlLoader().serverAppReduced(),
                repoName = repo1,
                projectName = "proj2",
            )

            testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
                publicId = olderRunRepo2,
                coverageText = JacocoXmlLoader().jacocoXmlParser(),
                repoName = repo2,
            )
            testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
                publicId = publicId2,
                coverageText = JacocoXmlLoader().jacocoXmlParser(),
                repoName = repo2,
            )

            testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
                publicId = olderRunRepo3,
                coverageText = JacocoXmlLoader().junitResultsParser(),
                repoName = repo3,
            )
            testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
                publicId = publicId3,
                coverageText = JacocoXmlLoader().junitResultsParser(),
                repoName = repo3,
            )

            testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
                publicId = anotherPublicId,
                coverageText = JacocoXmlLoader().serverAppReduced(),
                repoName = anotherRepo,
            )

            testRunDBGenerator.createSimpleTestRunInRepo(
                publicId = noCodeCoveragePublicId,
                repoName = noCodeCoverageRepo,
                ci = true,
                projectName = null,
            )

            val response = testClient.get("/api/v1/org/$orgName/coverage/current")

            expectThat(response.status).isEqualTo(HttpStatusCode.OK)

            val organizationCoverage = objectMapper.readValue(response.bodyAsText(), OrganizationCurrentCoverage::class.java)
            assertNotNull(organizationCoverage)

            expectThat(organizationCoverage.repositories).hasSize(4)

            val repoNames = organizationCoverage.repositories.map { it.repo }

            val repositoryDatas1 = organizationCoverage.repositories.filter { it.repo == repo1 }
            expectThat(repositoryDatas1).hasSize(2)

            val repo1DataProj1 = repositoryDatas1.find { it.project == "proj1" }
            assertNotNull(repo1DataProj1)
            expectThat(repo1DataProj1.id).isEqualTo(publicId1.id)
            expectThat(repo1DataProj1.coveredPercentage).isEqualTo(JacocoXmlLoader.serverAppLineCoveragePercentage)
            expectThat(repo1DataProj1.repo).isEqualTo(repo1)
            expectThat(repo1DataProj1.branch).isEqualTo("main")

            val repo1DataProj2 = repositoryDatas1.find { it.project == "proj2" }
            assertNotNull(repo1DataProj2)
            expectThat(repo1DataProj2.id).isEqualTo(otherProjectRepo1.id)
            expectThat(repo1DataProj2.coveredPercentage).isEqualTo(JacocoXmlLoader.serverAppReducedLineCoveragePercentage)
            expectThat(repo1DataProj2.repo).isEqualTo(repo1)
            expectThat(repo1DataProj2.branch).isEqualTo("main")

            val repositoryData2 = organizationCoverage.repositories.find { it.repo == repo2 }
            assertNotNull(repositoryData2)

            expectThat(repositoryData2.id).isEqualTo(publicId2.id)
            expectThat(repositoryData2.coveredPercentage).isEqualTo(JacocoXmlLoader.jacocoXmlParserLineCoveragePercentage)
            expectThat(repositoryData2.repo).isEqualTo(repo2)
            expectThat(repositoryData2.branch).isEqualTo("main")

            val repositoryData3 = organizationCoverage.repositories.find { it.repo == repo3 }
            assertNotNull(repositoryData3)

            expectThat(repositoryData3.id).isEqualTo(publicId3.id)
            expectThat(repositoryData3.coveredPercentage).isEqualTo(JacocoXmlLoader.junitResultsParserLineCoveragePercentage)
            expectThat(repositoryData3.repo).isEqualTo(repo3)
            expectThat(repositoryData3.branch).isEqualTo("main")

            expectThat(repoNames).doesNotContain(noCodeCoverageRepo)
        }
}
