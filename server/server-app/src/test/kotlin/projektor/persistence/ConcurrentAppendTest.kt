package projektor.persistence

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test
import projektor.TestSuiteData
import projektor.incomingresults.AppendTestResultsService
import projektor.incomingresults.model.GroupedResults
import projektor.incomingresults.model.GroupedTestSuites
import projektor.incomingresults.randomPublicId
import projektor.parser.coverage.payload.CoverageFilePayload
import projektor.server.api.PublicId
import projektor.server.example.coverage.CloverXmlLoader
import strikt.api.expectThat
import strikt.assertions.hasSize
import strikt.assertions.isEqualTo
import strikt.assertions.isNotNull
import java.util.concurrent.CyclicBarrier

/**
 * Appends that arrive at the same time, like parallel CI jobs publishing results to the same group,
 * should end up with the same data as when they arrive one after another.
 */
abstract class ConcurrentAppendTest : RepositoryTestCase() {
    private val cloverXmlLoader = CloverXmlLoader()

    private val coveragePayloads
        get() =
            listOf(cloverXmlLoader.coverageFilesTable(), cloverXmlLoader.coverageGraph(), cloverXmlLoader.coverageTable())
                .map { CoverageFilePayload(it, "/src") }

    @Test
    fun `concurrent coverage appends to the same group should match sequential appends`() {
        val sequentialPublicId = randomPublicId()
        testRunGenerator.createSimpleTestRun(sequentialPublicId)
        coveragePayloads.forEach { payload -> runBlocking { coverageService.appendCoverage(sequentialPublicId, listOf(payload)) } }
        val expectedCoverage = fetchCoverageSnapshot(sequentialPublicId)
        expectThat(expectedCoverage.groups).hasSize(1)

        repeat(ATTEMPTS) {
            val concurrentPublicId = randomPublicId()
            testRunGenerator.createSimpleTestRun(concurrentPublicId)

            runConcurrently(coveragePayloads) { payload -> coverageService.appendCoverage(concurrentPublicId, listOf(payload)) }

            expectThat(fetchCoverageSnapshot(concurrentPublicId)).isEqualTo(expectedCoverage)
        }
    }

    @Test
    fun `concurrent test suite appends to the same test run should keep every test suite`() {
        val appendTestResultsService = AppendTestResultsService(testRunRepository, testSuiteRepository, resultsProcessingRepository)
        val appendCount = 4

        repeat(ATTEMPTS) {
            val publicId = randomPublicId()
            testRunGenerator.createGroupedTestRun(
                publicId,
                listOf(TestSuiteGroupData("Group", null, listOf(TestSuiteData("Initial", listOf("passing"), listOf(), listOf())))),
            )

            runConcurrently((1..appendCount).toList()) { appendIdx ->
                val testSuites =
                    listOf(
                        TestRunGenerator.toParsedTestSuite(
                            TestSuiteData("Appended$appendIdx", listOf("passing"), listOf("failing"), listOf()),
                        ),
                    )
                appendTestResultsService.appendGroupedTestRun(
                    publicId,
                    GroupedResults(listOf(GroupedTestSuites(testSuites, "Group", null, null)), listOf(), null, null, null, null),
                )
            }

            val testSuites = runBlocking { testSuiteRepository.fetchTestSuitesWithCases(publicId) }
            expectThat(testSuites.map { it.className }.toSet()).isEqualTo(setOf("Initial") + (1..appendCount).map { "Appended$it" })
            expectThat(testSuites.map { it.idx }.distinct()).hasSize(appendCount + 1)

            // The summary covers every appended test suite, not just the ones a particular append saw
            expectThat(runBlocking { testRunRepository.fetchTestRunSummary(publicId) }).isNotNull().and {
                get { totalTestCount }.isEqualTo(1 + appendCount * 2)
                get { totalFailureCount }.isEqualTo(appendCount)
            }
        }
    }

    @Test
    fun `concurrent appends to a large test run should keep every test suite and recalculate the summary from all of them`() {
        val appendTestResultsService = AppendTestResultsService(testRunRepository, testSuiteRepository, resultsProcessingRepository)
        val publicId = randomPublicId()
        // More test suites than fit in one test case chunk, so recalculating the summary reads several chunks
        val initialSuiteCount = 250
        val appendCount = 6
        val suitesPerAppend = 30

        testRunGenerator.createGroupedTestRun(
            publicId,
            listOf(
                TestSuiteGroupData(
                    "Group",
                    null,
                    (1..initialSuiteCount).map { TestSuiteData("Initial$it", listOf("passing"), listOf(), listOf()) },
                ),
            ),
        )

        runConcurrently((1..appendCount).toList()) { appendIdx ->
            val testSuites =
                (1..suitesPerAppend).map { suiteIdx ->
                    TestRunGenerator.toParsedTestSuite(
                        TestSuiteData("Appended$appendIdx-$suiteIdx", listOf("passing"), listOf("failing"), listOf()),
                    )
                }
            appendTestResultsService.appendGroupedTestRun(
                publicId,
                GroupedResults(listOf(GroupedTestSuites(testSuites, "Group", null, null)), listOf(), null, null, null, null),
            )
        }

        val expectedSuiteCount = initialSuiteCount + appendCount * suitesPerAppend
        val testSuites = runBlocking { testSuiteRepository.fetchTestSuitesWithCases(publicId) }
        expectThat(testSuites.map { it.idx }.distinct()).hasSize(expectedSuiteCount)
        expectThat(testSuites.sumOf { it.testCases.orEmpty().size }).isEqualTo(initialSuiteCount + appendCount * suitesPerAppend * 2)

        expectThat(runBlocking { testRunRepository.fetchTestRunSummary(publicId) }).isNotNull().and {
            get { totalTestCount }.isEqualTo(initialSuiteCount + appendCount * suitesPerAppend * 2)
            get { totalFailureCount }.isEqualTo(appendCount * suitesPerAppend)
        }
    }

    private fun fetchCoverageSnapshot(publicId: PublicId): CoverageSnapshot =
        runBlocking {
            val groups = coverageRepository.fetchCoverageList(publicId)

            CoverageSnapshot(
                groups = groups.map { it.name to it.totalStats.lineStat },
                files =
                    groups.flatMap { group ->
                        coverageRepository.fetchCoverageFiles(publicId, group.name)
                            .map {
                                listOf(
                                    group.name,
                                    it.filePath,
                                    it.fileName,
                                    it.missedLines.toList(),
                                    it.stats.lineStat.covered,
                                    it.stats.lineStat.missed,
                                )
                            }
                    }.sortedBy { it.toString() },
            )
        }

    private data class CoverageSnapshot(val groups: List<Pair<String, Any>>, val files: List<List<Any?>>)

    // Starts every operation at the same moment to make overlapping reads and writes likely
    private fun <T> runConcurrently(
        items: List<T>,
        operation: suspend (T) -> Unit,
    ) {
        val barrier = CyclicBarrier(items.size)

        runBlocking {
            items.map { item ->
                async(Dispatchers.IO) {
                    barrier.await()
                    operation(item)
                }
            }.awaitAll()
        }
    }

    companion object {
        private const val ATTEMPTS = 5
    }
}

class ConcurrentAppendDatabaseTest : ConcurrentAppendTest() {
    // Like production's pool, enough connections for the concurrent appends
    override fun createBackend() = DatabaseTestBackend(maximumPoolSize = 10)
}

class ConcurrentAppendObjectStoreTest : ConcurrentAppendTest() {
    override fun createBackend() = ObjectStoreTestBackend()
}
