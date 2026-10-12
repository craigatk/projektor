package projektor.notification.slack

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.test.inject
import projektor.DatabaseRepositoryTestCase
import projektor.TestSuiteData
import projektor.compare.PreviousTestRunService
import projektor.coverage.CoverageService
import projektor.incomingresults.model.GitMetadata
import projektor.incomingresults.randomPublicId
import projektor.notification.NotificationConfig
import projektor.repository.testrun.RepositoryTestRunService
import projektor.server.api.PublicId
import projektor.server.example.coverage.JacocoXmlLoader
import projektor.testrun.TestRunRepository
import projektor.util.randomOrgAndRepo
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.hasSize
import strikt.assertions.isEmpty
import strikt.assertions.isEqualTo
import strikt.assertions.isNull
import kotlin.test.assertNotNull

class SlackMainlineNotificationServiceTest : DatabaseRepositoryTestCase() {
    private val wireMockServer = WireMockServer(wireMockConfig().dynamicPort())
    private val slackWireMockStubber = SlackWireMockStubber(WireMock(wireMockServer))

    private val channelName = "build-alerts"

    @BeforeEach
    fun startWireMock() {
        wireMockServer.start()
        wireMockServer.resetAll()
        slackWireMockStubber.stubPostMessageSuccess()
    }

    @AfterEach
    fun stopWireMock() {
        wireMockServer.stop()
    }

    private fun createService(
        slackNotificationConfig: SlackNotificationConfig = SlackNotificationConfig("url", "token"),
    ): SlackMainlineNotificationService {
        val repositorySlackChannelRepository by inject<RepositorySlackChannelRepository>()
        val previousTestRunService by inject<PreviousTestRunService>()
        val repositoryTestRunService by inject<RepositoryTestRunService>()

        val slackNotificationService =
            SlackNotificationService(SlackClient(SlackClientConfig("http://localhost:${wireMockServer.port()}", "xoxb-token")))

        return SlackMainlineNotificationService(
            NotificationConfig("http://localhost:8080"),
            slackNotificationConfig,
            slackNotificationService,
            RepositorySlackChannelService(repositorySlackChannelRepository, null),
            previousTestRunService,
            repositoryTestRunService,
        )
    }

    private fun mapChannel(
        repoName: String,
        projectName: String? = null,
    ) {
        val repositorySlackChannelRepository by inject<RepositorySlackChannelRepository>()

        runBlocking { repositorySlackChannelRepository.upsertChannelName(repoName, projectName, channelName) }
    }

    private fun notify(
        service: SlackMainlineNotificationService,
        publicId: PublicId,
        repoName: String,
        projectName: String? = null,
        isMainBranch: Boolean = true,
        ci: Boolean? = true,
        withCoverage: Boolean = false,
    ): String? {
        val testRunRepository by inject<TestRunRepository>()
        val coverageService by inject<CoverageService>()

        return runBlocking {
            val testRunSummary = testRunRepository.fetchTestRunSummary(publicId)
            assertNotNull(testRunSummary)

            val coverage = if (withCoverage) coverageService.getCoverageWithPreviousRunComparison(publicId) else null

            service.notifyIfNeeded(
                testRunSummary,
                GitMetadata(repoName = repoName, projectName = projectName, branchName = "main", isMainBranch = isMainBranch),
                ci,
                coverage,
            )
        }
    }

    @Test
    fun `should notify when first mainline run fails`() {
        val repoName = randomOrgAndRepo()
        val publicId = randomPublicId()
        mapChannel(repoName)

        testRunDBGenerator.createSimpleFailingTestRunInRepo(publicId, repoName, true, null)

        expectThat(notify(createService(), publicId, repoName)).isEqualTo(channelName)

        val requestBodies = slackWireMockStubber.findPostMessageRequestBodies()
        expectThat(requestBodies).hasSize(1)
        expectThat(requestBodies[0])
            .contains("\"channel\":\"$channelName\"")
            .contains("Mainline went red")
            .contains("http://localhost:8080/tests/${publicId.id}/")
    }

    @Test
    fun `should notify when mainline goes from passing to failing but not when it stays failing`() {
        val repoName = randomOrgAndRepo()
        val passingPublicId = randomPublicId()
        val firstFailingPublicId = randomPublicId()
        val secondFailingPublicId = randomPublicId()
        mapChannel(repoName)
        val service = createService()

        testRunDBGenerator.createSimpleTestRunInRepo(passingPublicId, repoName, true, null)
        expectThat(notify(service, passingPublicId, repoName)).isNull()

        testRunDBGenerator.createSimpleFailingTestRunInRepo(firstFailingPublicId, repoName, true, null)
        expectThat(notify(service, firstFailingPublicId, repoName)).isEqualTo(channelName)

        testRunDBGenerator.createSimpleFailingTestRunInRepo(secondFailingPublicId, repoName, true, null)
        expectThat(notify(service, secondFailingPublicId, repoName)).isNull()

        val requestBodies = slackWireMockStubber.findPostMessageRequestBodies()
        expectThat(requestBodies).hasSize(1)
        expectThat(requestBodies[0]).contains(firstFailingPublicId.id)
    }

    @Test
    fun `should not notify when repo and project is not mapped to a channel`() {
        val repoName = randomOrgAndRepo()
        val publicId = randomPublicId()
        mapChannel(repoName, "other-project")

        testRunDBGenerator.createSimpleFailingTestRunInRepo(publicId, repoName, true, "my-project")

        expectThat(notify(createService(), publicId, repoName, projectName = "my-project")).isNull()
        expectThat(slackWireMockStubber.findPostMessageRequestBodies()).isEmpty()
    }

    @Test
    fun `should notify channel mapped to project`() {
        val repoName = randomOrgAndRepo()
        val publicId = randomPublicId()
        mapChannel(repoName, "my-project")

        testRunDBGenerator.createSimpleFailingTestRunInRepo(publicId, repoName, true, "my-project")

        expectThat(notify(createService(), publicId, repoName, projectName = "my-project")).isEqualTo(channelName)

        val requestBodies = slackWireMockStubber.findPostMessageRequestBodies()
        expectThat(requestBodies).hasSize(1)
        expectThat(requestBodies[0]).contains("$repoName (my-project)")
    }

    @Test
    fun `should not notify for runs that are not on mainline or not in CI`() {
        val repoName = randomOrgAndRepo()
        val publicId = randomPublicId()
        mapChannel(repoName)
        val service = createService()

        testRunDBGenerator.createSimpleFailingTestRunInRepo(publicId, repoName, true, null)

        expectThat(notify(service, publicId, repoName, isMainBranch = false)).isNull()
        expectThat(notify(service, publicId, repoName, ci = false)).isNull()
        expectThat(notify(service, publicId, repoName, ci = null)).isNull()
        expectThat(slackWireMockStubber.findPostMessageRequestBodies()).isEmpty()
    }

    @Test
    fun `should notify when coverage drops on mainline`() {
        val repoName = randomOrgAndRepo()
        val previousPublicId = randomPublicId()
        val thisPublicId = randomPublicId()
        mapChannel(repoName)
        val service = createService()

        testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(previousPublicId, JacocoXmlLoader().serverApp(), repoName)
        expectThat(notify(service, previousPublicId, repoName, withCoverage = true)).isNull()

        testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(thisPublicId, JacocoXmlLoader().serverAppReduced(), repoName)
        expectThat(notify(service, thisPublicId, repoName, withCoverage = true)).isEqualTo(channelName)

        val requestBodies = slackWireMockStubber.findPostMessageRequestBodies()
        expectThat(requestBodies).hasSize(1)
        expectThat(requestBodies[0])
            .contains("Coverage dropped")
            .contains("http://localhost:8080/tests/${thisPublicId.id}/coverage")
    }

    @Test
    fun `should not notify when coverage stays the same or increases`() {
        val repoName = randomOrgAndRepo()
        val previousPublicId = randomPublicId()
        val samePublicId = randomPublicId()
        val increasedPublicId = randomPublicId()
        mapChannel(repoName)
        val service = createService()

        testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(previousPublicId, JacocoXmlLoader().serverAppReduced(), repoName)
        testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(samePublicId, JacocoXmlLoader().serverAppReduced(), repoName)
        expectThat(notify(service, samePublicId, repoName, withCoverage = true)).isNull()

        testRunDBGenerator.createTestRunWithCoverageAndGitMetadata(increasedPublicId, JacocoXmlLoader().serverApp(), repoName)
        expectThat(notify(service, increasedPublicId, repoName, withCoverage = true)).isNull()

        expectThat(slackWireMockStubber.findPostMessageRequestBodies()).isEmpty()
    }

    @Test
    fun `should notify about a flaky test only on the run where it reaches the flaky threshold`() {
        val repoName = randomOrgAndRepo()
        mapChannel(repoName)
        val service = createService(SlackNotificationConfig("url", "token", flakyFailureThreshold = 3))

        val failingTestSuiteDataList =
            listOf(TestSuiteData("projektor.failingTestSuite", listOf("passing1"), listOf("failing1"), listOf()))

        val publicIds = (1..4).map { randomPublicId() }

        val notifiedChannels =
            publicIds.map { publicId ->
                testRunDBGenerator.createTestRunInRepo(publicId, failingTestSuiteDataList, repoName, true, null)

                notify(service, publicId, repoName)
            }

        // First run: mainline went red. Third run: test reached flaky threshold. Others: nothing new.
        expectThat(notifiedChannels).isEqualTo(listOf(channelName, null, channelName, null))

        val requestBodies = slackWireMockStubber.findPostMessageRequestBodies()
        expectThat(requestBodies).hasSize(2)

        val flakyRequestBody = requestBodies.first { it.contains(publicIds[2].id) }
        expectThat(flakyRequestBody)
            .contains("New flaky tests")
            .contains("failing1")
            .contains("http://localhost:8080/repository/$repoName/tests/flaky")
        expectThat(flakyRequestBody).not().contains("Mainline went red")
    }
}
