package projektor.organization

import io.ktor.http.*
import io.ktor.server.testing.*
import io.ktor.util.*
import java.math.BigDecimal
import kotlin.test.assertNotNull
import org.apache.commons.lang3.RandomStringUtils
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.incomingresults.randomPublicId
import projektor.server.api.organization.OrganizationCoverage
import projektor.server.example.coverage.JacocoXmlLoader
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull

@KtorExperimentalAPI
class OrganizationCoverageApplicationTest : ApplicationTestCase() {
    @Test
    fun `when three repos in org should find their coverage data`() {
        val orgName = RandomStringUtils.randomAlphabetic(12)

        val publicId1 = randomPublicId()
        val repo1 = "$orgName/repo1"

        val publicId2 = randomPublicId()
        val repo2 = "$orgName/repo2"

        val publicId3 = randomPublicId()
        val repo3 = "$orgName/repo3"

        val anotherPublicId = randomPublicId()
        val anotherRepo = "another-org/repo"

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/org/$orgName/coverage") {

                testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
                        publicId = publicId1,
                        coverageText = JacocoXmlLoader().serverApp(),
                        repoName = repo1
                )

                testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(
                        publicId = publicId2,
                        coverageText = JacocoXmlLoader().jacocoXmlParser(),
                        repoName = repo2
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

                expectThat(organizationCoverage.repositories).hasSize(3)

                val repositoryData1 = organizationCoverage.repositories.find { it.repoName == repo1 }
                assertNotNull(repositoryData1)

                expectThat(repositoryData1.publicId).isEqualTo(publicId1)

                expectThat(repositoryData1.coverage).isNotNull().and {
                    get { overallStats }.get { lineStat }.get { coveredPercentage }.isEqualTo(BigDecimal("97.44"))
                }

                val repositoryData2 = organizationCoverage.repositories.find { it.repoName == repo2 }
                assertNotNull(repositoryData2)

                expectThat(repositoryData2.publicId).isEqualTo(publicId2)

                expectThat(repositoryData2.coverage).isNotNull().and {
                    get { overallStats }.get { lineStat }.get { coveredPercentage }.isEqualTo(BigDecimal("92.86"))
                }

                val repositoryData3 = organizationCoverage.repositories.find { it.repoName == repo3 }
                assertNotNull(repositoryData3)

                expectThat(repositoryData3.publicId).isEqualTo(publicId3)

                expectThat(repositoryData3.coverage).isNotNull().and {
                    get { overallStats }.get { lineStat }.get { coveredPercentage }.isEqualTo(BigDecimal("92.31"))
                }
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
