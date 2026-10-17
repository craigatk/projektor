package projektor.persistence.objectstore

import kotlinx.coroutines.runBlocking
import org.apache.commons.lang3.RandomStringUtils
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import projektor.incomingresults.model.GitMetadata
import projektor.incomingresults.model.GroupedResults
import projektor.incomingresults.model.GroupedTestSuites
import projektor.incomingresults.model.ResultsMetadata
import projektor.incomingresults.randomPublicId
import projektor.parser.coverage.model.CoverageReport
import projektor.server.api.PublicId
import projektor.server.api.attachments.Attachment
import projektor.server.api.repository.BranchSearch
import projektor.server.api.repository.BranchType
import strikt.api.expectThat
import strikt.assertions.isGreaterThan
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import projektor.incomingresults.model.PerformanceResult as IncomingPerformanceResult

/**
 * Checks that the object-store backend's queries across test runs, backed by its indexes, return the same
 * results as the database backend for the same set of test runs.
 */
class ObjectStoreIndexParityTest : ObjectStoreParityTestCase() {
    private val orgName = RandomStringUtils.randomAlphabetic(12).lowercase()
    private val repoA = "$orgName/repo-a"
    private val repoB = "$orgName/repo-b"
    private val nestedRepo = "$orgName/subgroup/repo-c"

    private lateinit var runs: List<RunSpec>

    @BeforeEach
    fun saveRuns() {
        val baseline = listOf(resultsXmlLoader.durationRegressionBaseline())
        val coverage = listOf(coverageReport("server", 60, 40, 50, 10, 3, 4))

        runs =
            listOf(
                RunSpec(repo = null, created = "2020-01-05T10:00:00Z", xml = listOf(resultsXmlLoader.passing())),
                RunSpec(repoA, created = "2020-01-10T10:00:00Z", group = "g1", coverage = coverage, xml = baseline),
                RunSpec(repoA, created = "2020-01-20T10:00:00Z", coverage = listOf(), xml = baseline + resultsXmlLoader.failing()),
                RunSpec(
                    repoA,
                    created = "2020-02-01T10:00:00Z",
                    branch = "feature-x",
                    isMainBranch = false,
                    pullRequestNumber = 10,
                    commitSha = "c3",
                    coverage = listOf(coverageReport("server", 70, 30, 55, 5, 5, 2)),
                    performance = listOf(performanceResult("api")),
                    xml = listOf(resultsXmlLoader.failing()),
                ),
                RunSpec(
                    repoA,
                    project = "p1",
                    created = "2020-02-05T10:00:00Z",
                    coverage = coverage,
                    performance = listOf(performanceResult("p1-api")),
                    xml = baseline,
                ),
                RunSpec(repoA, created = "2020-02-10T10:00:00Z", ci = false, xml = baseline + resultsXmlLoader.failing()),
                RunSpec(repoA, created = "2020-02-15T10:00:00Z", ci = null, xml = baseline),
                RunSpec(
                    repoA,
                    created = "2020-03-01T10:00:00Z",
                    group = "g2",
                    pullRequestNumber = 11,
                    commitSha = "c7",
                    coverage = listOf(coverageReport("server", 80, 20, 58, 2, 6, 1), coverageReport("ui", 10, 10, 20, 5, 1, 1)),
                    // One result per run, since the database timeline only returns one result for each run
                    performance = listOf(performanceResult("api")),
                    xml = listOf(resultsXmlLoader.durationRegressionRegressed()),
                ),
                // Created at the same time as the previous run, which the database breaks ties for by ID
                RunSpec(
                    repoA,
                    created = "2020-03-01T10:00:00Z",
                    pullRequestNumber = 11,
                    commitSha = "c8",
                    xml = baseline + resultsXmlLoader.failing(),
                ),
                RunSpec(repoB, created = "2020-01-15T10:00:00Z", coverage = coverage, xml = baseline),
                RunSpec(
                    repoB,
                    project = "p2",
                    created = "2020-02-20T10:00:00Z",
                    branch = "other",
                    isMainBranch = false,
                    coverage = coverage,
                    xml = baseline,
                ),
                RunSpec(nestedRepo, created = "2020-03-05T10:00:00Z", coverage = coverage, xml = baseline),
            )

        runs.forEach { save(it) }

        onBoth { backend ->
            backend.testRunSystemAttributesRepository.pin(runs[3].publicId)
            backend.attachmentRepository.addAttachment(runs[1].publicId, Attachment("one.png", "object-1", 1L))
            backend.attachmentRepository.addAttachment(runs[4].publicId, Attachment("two.png", "object-2", 2L))
            backend.attachmentRepository.addAttachment(runs[3].publicId, Attachment("pinned.png", "object-3", 3L))
        }
    }

    @Test
    fun `repository test run queries should match`() {
        listOf(repoA, repoB, nestedRepo, "$orgName/missing").forEach { repoName ->
            listOf(null, "p1", "p2").forEach { projectName ->
                expectSameResult { it.repositoryTestRunRepository.fetchRepositoryTestRunTimeline(repoName, projectName) }
                expectSameResult { it.repositoryTestRunRepository.fetchRepositoryTestRunSummaries(repoName, projectName, 3) }
                expectSameResult { it.repositoryTestRunRepository.fetchRecentTestRunPublicIds(repoName, projectName, 3) }
                expectSameResult { it.repositoryTestRunRepository.fetchTestRunCount(repoName, projectName) }

                listOf(BranchType.MAINLINE, BranchType.ALL).forEach { branchType ->
                    listOf(2, 10).forEach { maxRuns ->
                        expectSameResult { backend ->
                            backend.repositoryTestRunRepository.fetchRepositoryFailingTestCases(repoName, projectName, maxRuns, branchType)
                                .map { listOf(it.publicId, it.testSuiteIdx, it.idx) }
                                .sortedBy { it.toString() }
                        }
                    }
                }
            }

            listOf(10, 11, 12).forEach { pullRequestNumber ->
                expectSameResult { it.repositoryTestRunRepository.fetchMostRecentTestRunPublicId(repoName, pullRequestNumber) }
                expectSameResult { it.repositoryTestRunRepository.fetchMostRecentTestRunPublicIdWithCoverage(repoName, pullRequestNumber) }
            }

            expectSameResult { it.testRunRepository.findTestRunWithGroup("g1", repoName) }
            expectSameResult { it.testRunRepository.findTestRunWithGroup("g2", repoName) }
            expectSameResult { it.testRunRepository.findTestRunWithGroup("missing", repoName) }
        }

        // Guard against the comparisons passing because both backends return nothing
        val timeline = runBlocking { objectStore.repositoryTestRunRepository.fetchRepositoryTestRunTimeline(repoA, null) }
        expectThat(timeline?.timelineEntries?.size ?: 0).isGreaterThan(3)
    }

    @Test
    fun `previous test run queries should match`() {
        runs.forEach { run ->
            expectSameResult { it.previousTestRunRepository.findPreviousMainBranchRunWithCoverage(run.publicId) }
            expectSameResult { it.previousTestRunRepository.findPreviousMainBranchRun(run.publicId) }
        }

        val branchSearches =
            listOf(
                null,
                BranchSearch(),
                BranchSearch(branchType = BranchType.MAINLINE),
                BranchSearch(branchType = BranchType.ALL),
                BranchSearch(branchName = "feature-x"),
                BranchSearch(branchName = "main", branchType = BranchType.MAINLINE),
            )

        listOf(repoA, repoB, nestedRepo).forEach { repoName ->
            listOf(null, "p1", "p2").forEach { projectName ->
                branchSearches.forEach { branchSearch ->
                    expectSameResult { it.previousTestRunRepository.findMostRecentRun(repoName, projectName, branchSearch) }
                    expectSameResult { it.previousTestRunRepository.findMostRecentRunWithCoverage(repoName, projectName, branchSearch) }
                }
            }
        }
    }

    @Test
    fun `repository coverage and performance queries should match`() {
        listOf(repoA, repoB, nestedRepo, "$orgName/missing").forEach { repoName ->
            listOf(null, "p1", "p2").forEach { projectName ->
                expectSameResult { it.repositoryCoverageRepository.coverageExists(repoName, projectName) }
                expectSameResult { it.repositoryPerformanceRepository.fetchTestTimelineEntries(repoName, projectName) }

                listOf(BranchType.MAINLINE, BranchType.ALL).forEach { branchType ->
                    expectSameResult { it.repositoryCoverageRepository.fetchRepositoryCoverageTimeline(branchType, repoName, projectName) }
                }
            }
        }

        expectSameResult { backend ->
            backend.organizationCoverageRepository.findReposWithCoverage(orgName).sortedBy { "${it.repoName} ${it.projectName}" }
        }
        expectSameResult { it.organizationCoverageRepository.findReposWithCoverage("$orgName-missing") }

        val coverageTimeline =
            runBlocking { objectStore.repositoryCoverageRepository.fetchRepositoryCoverageTimeline(BranchType.ALL, repoA, null) }
        expectThat(coverageTimeline?.timelineEntries?.size ?: 0).isGreaterThan(1)
    }

    @Test
    fun `last known coverage and Slack channels should match`() {
        listOf(null, "p1").forEach { projectName ->
            expectSameResult { it.repositoryCoverageRepository.fetchLastKnownCoverage(repoA, projectName) }

            onBoth {
                it.repositoryCoverageRepository.saveLastKnownCoverageIfNewer(
                    repoA,
                    projectName,
                    "main",
                    BigDecimal("81.25"),
                    runs[2].publicId,
                    Instant.parse("2020-01-20T10:00:00.123456789Z"),
                )
            }
            expectSameResult { it.repositoryCoverageRepository.fetchLastKnownCoverage(repoA, projectName) }

            // Older coverage doesn't replace newer coverage
            onBoth {
                it.repositoryCoverageRepository.saveLastKnownCoverageIfNewer(
                    repoA,
                    projectName,
                    "main",
                    BigDecimal("50"),
                    runs[1].publicId,
                    Instant.parse("2020-01-10T10:00:00Z"),
                )
            }
            expectSameResult { it.repositoryCoverageRepository.fetchLastKnownCoverage(repoA, projectName) }

            onBoth {
                it.repositoryCoverageRepository.saveLastKnownCoverageIfNewer(
                    repoA,
                    projectName,
                    "main",
                    BigDecimal("90.5"),
                    runs[7].publicId,
                    Instant.parse("2020-03-01T10:00:00Z"),
                )
            }
            expectSameResult { it.repositoryCoverageRepository.fetchLastKnownCoverage(repoA, projectName) }

            expectSameResult { it.repositorySlackChannelRepository.fetchChannelName(repoA, projectName) }
            expectSameResult { it.repositorySlackChannelRepository.deleteChannelName(repoA, projectName) }
            onBoth { it.repositorySlackChannelRepository.upsertChannelName(repoA, projectName, "#first") }
            onBoth { it.repositorySlackChannelRepository.upsertChannelName(repoA, projectName, "#second") }
            expectSameResult { it.repositorySlackChannelRepository.fetchChannelName(repoA, projectName) }
            expectSameResult { it.repositorySlackChannelRepository.fetchChannelName(repoB, projectName) }
            expectSameResult { it.repositorySlackChannelRepository.deleteChannelName(repoA, projectName) }
            expectSameResult { it.repositorySlackChannelRepository.fetchChannelName(repoA, projectName) }
        }
    }

    @Test
    fun `test case history and duration baselines should match`() {
        runs.forEach { run ->
            listOf(2, 10).forEach { maxRuns ->
                expectSameResult { backend ->
                    // The database returns medians at a scale that depends on the Postgres driver's transfer mode,
                    // so compare them by value
                    backend.testCaseRepository.fetchTestCaseDurationBaselines(run.publicId, maxRuns)
                        .let { baselines ->
                            baselines.copy(
                                baselines =
                                    baselines.baselines
                                        .map { it.copy(medianDuration = it.medianDuration.stripTrailingZeros()) }
                                        .sortedWith(compareBy({ it.testSuiteIdx }, { it.testCaseIdx })),
                            )
                        }
                }
            }

            val testCases = runBlocking { database.testCaseRepository.fetchTestCases(run.publicId) }
            testCases.forEach { testCase ->
                listOf(3, 10).forEach { maxRuns ->
                    expectSameResult {
                        it.testCaseRepository.fetchTestCaseHistory(
                            run.publicId,
                            testCase.testSuiteIdx,
                            testCase.idx,
                            maxRuns,
                        )
                    }
                }
            }
        }

        val baselines = runBlocking { objectStore.testCaseRepository.fetchTestCaseDurationBaselines(runs[7].publicId, 10) }
        expectThat(baselines.baselines.size).isGreaterThan(0)
    }

    @Test
    fun `test run cleanup queries should match for these test runs`() {
        val testRunIds = runs.map { it.publicId }.toSet()

        listOf("2019-12-31", "2020-01-10", "2020-01-11", "2020-02-12", "2020-04-01").map { LocalDate.parse(it) }.forEach { createdBefore ->
            // The database query covers every test run in the shared test database, so only compare these test runs
            expectSameResult { backend ->
                backend.testRunRepository.findTestRunsCreatedBeforeAndNotPinned(
                    createdBefore,
                ).filter { it in testRunIds }.sortedBy { it.id }
            }
            expectSameResult { backend ->
                backend.testRunRepository.findTestRunsCreatedBeforeAndNotPinnedWithAttachments(
                    createdBefore,
                ).filter { it in testRunIds }.sortedBy { it.id }
            }
        }
    }

    @Test
    fun `deleting test runs should remove them from the queries`() {
        onBoth { backend ->
            backend.testRunRepository.deleteTestRun(runs[7].publicId)
            backend.coverageRepository.deleteCoverage(runs[7].publicId)
            backend.coverageRepository.deleteCoverage(runs[4].publicId)
        }

        expectSameResult { it.repositoryTestRunRepository.fetchRepositoryTestRunTimeline(repoA, null) }
        expectSameResult { it.repositoryTestRunRepository.fetchMostRecentTestRunPublicId(repoA, 11) }
        expectSameResult { it.repositoryCoverageRepository.fetchRepositoryCoverageTimeline(BranchType.ALL, repoA, "p1") }
        expectSameResult { it.repositoryCoverageRepository.coverageExists(repoA, "p1") }
        expectSameResult { it.testRunRepository.findTestRunWithGroup("g2", repoA) }
        expectSameResult { it.previousTestRunRepository.findPreviousMainBranchRunWithCoverage(runs[8].publicId) }
        expectSameResult { backend ->
            backend.testRunRepository.findTestRunsCreatedBeforeAndNotPinned(LocalDate.parse("2020-04-01")).filter {
                it in
                    runs.map {
                            run ->
                        run.publicId
                    }
            }.sortedBy { it.id }
        }
    }

    private fun save(run: RunSpec) {
        val groupedResults =
            GroupedResults(
                groupedTestSuites = listOf(GroupedTestSuites(parse(*run.xml.toTypedArray()), "Group", null, null)),
                performanceResults = run.performance,
                metadata =
                    ResultsMetadata(
                        git = run.repo?.let { GitMetadata(it, run.project, run.branch, run.isMainBranch) },
                        ci = run.ci,
                        group = run.group,
                        createdTimestamp = Instant.parse(run.created),
                    ),
                wallClockDuration = null,
                coverageFiles = null,
                codeQualityReports = null,
            )

        onBoth { backend ->
            backend.testRunRepository.saveGroupedTestRun(run.publicId, groupedResults)
            run.performance.forEach { backend.performanceResultsRepository.savePerformanceResults(run.publicId, it) }
            run.coverage?.let { coverageReports ->
                backend.coverageRepository.createOrGetCoverageRun(run.publicId)
                coverageReports.forEach { backend.coverageRepository.addCoverageReport(run.publicId, it, listOf()) }
            }
            if (run.repo != null) {
                backend.testRunMetadataRepository.updateGitMetadata(run.publicId, run.pullRequestNumber, run.commitSha)
            }
        }
    }

    private fun performanceResult(name: String) =
        IncomingPerformanceResult(name, BigDecimal("10.5"), 100, BigDecimal("1.25"), BigDecimal("3"), BigDecimal("2.5"))

    private data class RunSpec(
        val repo: String?,
        val project: String? = null,
        val branch: String = "main",
        val isMainBranch: Boolean = true,
        val ci: Boolean? = true,
        val created: String,
        val group: String? = null,
        val pullRequestNumber: Int? = null,
        val commitSha: String? = null,
        // Null for no coverage, empty to start coverage without any groups
        val coverage: List<CoverageReport>? = null,
        val performance: List<IncomingPerformanceResult> = listOf(),
        val xml: List<String>,
        val publicId: PublicId = randomPublicId(),
    )
}
