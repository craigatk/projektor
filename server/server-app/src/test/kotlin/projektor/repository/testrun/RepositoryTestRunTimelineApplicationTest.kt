package projektor.repository.testrun

import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import org.apache.commons.lang3.RandomStringUtils
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.createTestRun
import projektor.incomingresults.randomPublicId
import projektor.server.api.repository.RepositoryTestRunTimeline
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import java.math.BigDecimal
import kotlin.test.assertNotNull

class RepositoryTestRunTimelineApplicationTest : ApplicationTestCase() {
    @Test
    fun `should fetch test run timeline without project name`() {
        val orgName = RandomStringUtils.randomAlphabetic(12)
        val repoName = "$orgName/repo"

        val firstRunPublicId = randomPublicId()
        val secondRunPublicId = randomPublicId()
        val thirdRunPublicId = randomPublicId()

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/repo/$repoName/timeline") {
                val firstTestRun = createTestRun(firstRunPublicId, 20, BigDecimal("10.001"))
                testRunDao.insert(firstTestRun)
                testRunDBGenerator.addGitMetadata(firstTestRun, repoName, true, "main", null, null, null)
                testRunDBGenerator.addResultsMetadata(firstTestRun, true)

                val secondTestRun = createTestRun(secondRunPublicId, 30, BigDecimal("15.001"))
                testRunDao.insert(secondTestRun)
                testRunDBGenerator.addGitMetadata(secondTestRun, repoName, true, "main", null, null, null)
                testRunDBGenerator.addResultsMetadata(secondTestRun, true)

                val thirdTestRun = createTestRun(thirdRunPublicId, 45, BigDecimal("25.001"))
                testRunDao.insert(thirdTestRun)
                testRunDBGenerator.addGitMetadata(thirdTestRun, repoName, true, "main", null, null, null)
                testRunDBGenerator.addResultsMetadata(thirdTestRun, true)
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)

                val testRunTimeline = objectMapper.readValue(response.content, RepositoryTestRunTimeline::class.java)
                assertNotNull(testRunTimeline)

                expectThat(testRunTimeline.timelineEntries).hasSize(3)

                val firstEntry = testRunTimeline.timelineEntries[0]
                expectThat(firstEntry) {
                    get { publicId }.isEqualTo(firstRunPublicId.id)
                    get { totalTestCount }.isEqualTo(20)
                    get { cumulativeDuration }.isEqualTo(BigDecimal("10.001"))
                    get { testAverageDuration }.isEqualTo(BigDecimal("0.500"))
                }

                val secondEntry = testRunTimeline.timelineEntries[1]
                expectThat(secondEntry) {
                    get { publicId }.isEqualTo(secondRunPublicId.id)
                    get { totalTestCount }.isEqualTo(30)
                    get { cumulativeDuration }.isEqualTo(BigDecimal("15.001"))
                    get { testAverageDuration }.isEqualTo(BigDecimal("0.500"))
                }

                val thirdEntry = testRunTimeline.timelineEntries[2]
                expectThat(thirdEntry) {
                    get { publicId }.isEqualTo(thirdRunPublicId.id)
                    get { totalTestCount }.isEqualTo(45)
                    get { cumulativeDuration }.isEqualTo(BigDecimal("25.001"))
                    get { testAverageDuration }.isEqualTo(BigDecimal("0.556"))
                }
            }
        }
    }

    @Test
    fun `should fetch test run timeline with project name`() {
        val orgName = RandomStringUtils.randomAlphabetic(12)
        val repoName = "$orgName/repo"
        val projectName = "my-project"

        val firstRunPublicId = randomPublicId()
        val secondRunPublicId = randomPublicId()
        val thirdRunPublicId = randomPublicId()

        withTestApplication(::createTestApplication) {
            handleRequest(HttpMethod.Get, "/repo/$repoName/project/$projectName/timeline") {
                val firstTestRun = createTestRun(firstRunPublicId, 20, BigDecimal("10.001"))
                testRunDao.insert(firstTestRun)
                testRunDBGenerator.addGitMetadata(firstTestRun, repoName, true, "main", projectName, null, null)
                testRunDBGenerator.addResultsMetadata(firstTestRun, true)

                val secondTestRun = createTestRun(secondRunPublicId, 30, BigDecimal("15.001"))
                testRunDao.insert(secondTestRun)
                testRunDBGenerator.addGitMetadata(secondTestRun, repoName, true, "main", projectName, null, null)
                testRunDBGenerator.addResultsMetadata(secondTestRun, true)

                val thirdTestRun = createTestRun(thirdRunPublicId, 45, BigDecimal("25.001"))
                testRunDao.insert(thirdTestRun)
                testRunDBGenerator.addGitMetadata(thirdTestRun, repoName, true, "main", projectName, null, null)
                testRunDBGenerator.addResultsMetadata(thirdTestRun, true)
            }.apply {
                expectThat(response.status()).isEqualTo(HttpStatusCode.OK)

                val testRunTimeline = objectMapper.readValue(response.content, RepositoryTestRunTimeline::class.java)
                assertNotNull(testRunTimeline)

                expectThat(testRunTimeline.timelineEntries).hasSize(3)

                val firstEntry = testRunTimeline.timelineEntries[0]
                expectThat(firstEntry) {
                    get { publicId }.isEqualTo(firstRunPublicId.id)
                }

                val secondEntry = testRunTimeline.timelineEntries[1]
                expectThat(secondEntry) {
                    get { publicId }.isEqualTo(secondRunPublicId.id)
                }

                val thirdEntry = testRunTimeline.timelineEntries[2]
                expectThat(thirdEntry) {
                    get { publicId }.isEqualTo(thirdRunPublicId.id)
                }
            }
        }
    }
}
