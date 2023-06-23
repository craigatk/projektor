package projektor.organization

import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import org.apache.commons.lang3.RandomStringUtils
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.incomingresults.randomPublicId
import projektor.server.api.organization.OrganizationCoverage
import projektor.server.example.coverage.JacocoXmlLoader
import projektor.server.example.coverage.JacocoXmlLoader.Companion.jacocoXmlParserLineCoveragePercentage
import projektor.server.example.coverage.JacocoXmlLoader.Companion.junitResultsParserLineCoveragePercentage
import projektor.server.example.coverage.JacocoXmlLoader.Companion.serverAppLineCoveragePercentage
import projektor.server.example.coverage.JacocoXmlLoader.Companion.serverAppReducedLineCoveragePercentage
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import kotlin.test.assertNotNull

class OrganizationCoverageApplicationTest : ApplicationTestCase() {
    @Test
    fun `when three repos in org should find their coverage data`() {
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

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/org/$orgName/coverage") {

                testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
                    publicId = olderRunRepo1,
                    coverageText = JacocoXmlLoader().serverApp(),
                    repoName = repo1,
                    projectName = "proj1"
                )
                testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
                    publicId = publicId1,
                    coverageText = JacocoXmlLoader().serverApp(),
                    repoName = repo1,
                    projectName = "proj1"
                )
                testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
                    publicId = otherProjectRepo1,
                    coverageText = JacocoXmlLoader().serverAppReduced(),
                    repoName = repo1,
                    projectName = "proj2"
                )

                testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
                    publicId = olderRunRepo2,
                    coverageText = JacocoXmlLoader().jacocoXmlParser(),
                    repoName = repo2
                )
                testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
                    publicId = publicId2,
                    coverageText = JacocoXmlLoader().jacocoXmlParser(),
                    repoName = repo2
                )

                testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
                    publicId = olderRunRepo3,
                    coverageText = JacocoXmlLoader().junitResultsParser(),
                    repoName = repo3
                )
                testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
                    publicId = publicId3,
                    coverageText = JacocoXmlLoader().junitResultsParser(),
                    repoName = repo3
                )

                testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
                    publicId = anotherPublicId,
                    coverageText = JacocoXmlLoader().serverAppReduced(),
                    repoName = anotherRepo
                )
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)

                val organizationCoverage = objectMapper.readValue(response.content, OrganizationCoverage::class.java)
                assertNotNull(organizationCoverage)

                expectThat(organizationCoverage.repositories).hasSize(4)

                val repositoryDatas1 = organizationCoverage.repositories.filter { it.repoName == repo1 }
                expectThat(repositoryDatas1).hasSize(2)

                val repo1DataProj1 = repositoryDatas1.find { it.projectName == "proj1" }
                assertNotNull(repo1DataProj1)
                expectThat(repo1DataProj1.publicId).isEqualTo(publicId1.id)
                expectThat(repo1DataProj1.coverage).isNotNull().and {
                    get { overallStats }.get { lineStat }.get { coveredPercentage }.isEqualTo(serverAppLineCoveragePercentage)
                }

                val repo1DataProj2 = repositoryDatas1.find { it.projectName == "proj2" }
                assertNotNull(repo1DataProj2)
                expectThat(repo1DataProj2.publicId).isEqualTo(otherProjectRepo1.id)
                expectThat(repo1DataProj2.coverage).isNotNull().and {
                    get { overallStats }.get { lineStat }.get { coveredPercentage }.isEqualTo(serverAppReducedLineCoveragePercentage)
                }

                val repositoryData2 = organizationCoverage.repositories.find { it.repoName == repo2 }
                assertNotNull(repositoryData2)

                expectThat(repositoryData2.publicId).isEqualTo(publicId2.id)

                expectThat(repositoryData2.coverage).isNotNull().and {
                    get { overallStats }.get { lineStat }.get { coveredPercentage }.isEqualTo(jacocoXmlParserLineCoveragePercentage)
                }

                val repositoryData3 = organizationCoverage.repositories.find { it.repoName == repo3 }
                assertNotNull(repositoryData3)

                expectThat(repositoryData3.publicId).isEqualTo(publicId3.id)

                expectThat(repositoryData3.coverage).isNotNull().and {
                    get { overallStats }.get { lineStat }.get { coveredPercentage }.isEqualTo(junitResultsParserLineCoveragePercentage)
                }
            }
        }
    }

    @Test
    fun `should only find main branch runs`() {
        val orgName = RandomStringUtils.randomAlphabetic(12)
        val repoName = "$orgName/repo"

        val mainBranchId = randomPublicId()
        val featureBranchId = randomPublicId()

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/org/$orgName/coverage") {

                testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
                    publicId = mainBranchId,
                    coverageText = JacocoXmlLoader().serverApp(),
                    repoName = repoName,
                    branchName = "main"
                )

                testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
                    publicId = featureBranchId,
                    coverageText = JacocoXmlLoader().serverApp(),
                    repoName = repoName,
                    branchName = "feature/branch"
                )
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)

                val organizationCoverage = objectMapper.readValue(response.content, OrganizationCoverage::class.java)
                assertNotNull(organizationCoverage)

                expectThat(organizationCoverage.repositories).hasSize(1)

                expectThat(organizationCoverage.repositories[0].publicId).isEqualTo(mainBranchId.id)
            }
        }
    }

    @Test
    fun `when no org with name should return 204 response`() {
        val orgName = RandomStringUtils.randomAlphabetic(12)

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/org/$orgName/coverage").apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.NoContent)
            }
        }
    }
}
