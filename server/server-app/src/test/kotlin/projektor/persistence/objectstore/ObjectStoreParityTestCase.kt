package projektor.persistence.objectstore

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.koin.core.Koin
import org.koin.dsl.koinApplication
import projektor.DatabaseRepositoryTestCase
import projektor.attachment.AttachmentRepository
import projektor.compare.PreviousTestRunRepository
import projektor.coverage.CoverageRepository
import projektor.incomingresults.processing.ResultsProcessingRepository
import projektor.metadata.TestRunMetadataRepository
import projektor.notification.slack.RepositorySlackChannelRepository
import projektor.objectstore.ObjectStoreClient
import projektor.objectstore.ObjectStoreConfig
import projektor.objectstore.json.JsonObjectStore
import projektor.organization.coverage.OrganizationCoverageRepository
import projektor.parser.ResultsXmlLoader
import projektor.parser.coverage.model.CoverageReport
import projektor.parser.coverage.model.CoverageReportStat
import projektor.parser.coverage.model.CoverageReportStats
import projektor.performance.PerformanceResultsRepository
import projektor.persistence.createDatabasePersistenceModule
import projektor.quality.CodeQualityReportRepository
import projektor.repository.coverage.RepositoryCoverageRepository
import projektor.repository.performance.RepositoryPerformanceRepository
import projektor.repository.testrun.RepositoryTestRunRepository
import projektor.results.processor.TestResultsProcessor
import projektor.server.api.TestRunSummary
import projektor.server.api.coverage.CoverageFile
import projektor.server.api.coverage.CoverageStat
import projektor.server.api.coverage.CoverageStats
import projektor.testcase.TestCaseRepository
import projektor.testrun.TestRunRepository
import projektor.testrun.attributes.TestRunSystemAttributesRepository
import projektor.testsuite.TestSuiteRepository
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import strikt.assertions.isGreaterThan
import java.time.Instant
import java.util.UUID

/**
 * Base for tests that save the same data to both the database and object-store backends (under the same
 * public IDs, since the backends are independent) and check that fetches return equal results.
 */
abstract class ObjectStoreParityTestCase : DatabaseRepositoryTestCase() {
    protected val resultsXmlLoader = ResultsXmlLoader()
    private val testResultsProcessor = TestResultsProcessor()

    protected lateinit var database: Repositories
    protected lateinit var objectStore: Repositories

    @BeforeEach
    fun createBackends() {
        database = Repositories(koinApplication { modules(createDatabasePersistenceModule(dataSource, dslContext)) }.koin)

        val objectStoreClient = ObjectStoreClient(ObjectStoreConfig("http://localhost:9000", "minio_access_key", "minio_secret_key"))
        objectStoreClient.createBucketIfNotExists(BUCKET_NAME)
        val jsonObjectStore = JsonObjectStore(objectStoreClient, BUCKET_NAME, "parity-${UUID.randomUUID()}")
        objectStore = Repositories(koinApplication { modules(createObjectStorePersistenceModule(jsonObjectStore)) }.koin)
    }

    protected fun <T> expectSameResult(
        ignoringCreatedTimestamp: Boolean = false,
        fetch: suspend (Repositories) -> T,
    ) {
        val databaseResult = runBlocking { fetch(database) }
        val objectStoreResult = runBlocking { fetch(objectStore) }

        if (ignoringCreatedTimestamp && databaseResult is TestRunSummary && objectStoreResult is TestRunSummary) {
            expectThat(
                objectStoreResult.copy(createdTimestamp = Instant.EPOCH),
            ).isEqualTo(databaseResult.copy(createdTimestamp = Instant.EPOCH))
        } else {
            expectThat(objectStoreResult).isEqualTo(databaseResult)
        }
    }

    /**
     * Runs the same change against both backends.
     */
    protected fun onBoth(change: suspend (Repositories) -> Unit) {
        runBlocking {
            change(database)
            change(objectStore)
        }
    }

    // Drops test suites without test cases, like GroupedResultsConverter does before results are saved
    protected fun parse(vararg resultsXml: String) =
        resultsXml
            .flatMap { testResultsProcessor.parseResultsBlob(it) }
            .filter { !it.testCases.isNullOrEmpty() }
            .also { expectThat(it.size).isGreaterThan(0) }

    protected fun coverageReport(
        name: String,
        statementCovered: Int,
        statementMissed: Int,
        lineCovered: Int,
        lineMissed: Int,
        branchCovered: Int,
        branchMissed: Int,
    ) = CoverageReport(
        name,
        CoverageReportStats(
            statementStat = CoverageReportStat(statementCovered, statementMissed),
            lineStat = CoverageReportStat(lineCovered, lineMissed),
            branchStat = CoverageReportStat(branchCovered, branchMissed),
        ),
        null,
    )

    protected fun coverageFile(
        fileName: String,
        filePath: String?,
        missedLines: List<Int>,
        partialLines: List<Int>,
    ) = CoverageFile(
        fileName = fileName,
        directoryName = "dir",
        missedLines = missedLines.toTypedArray(),
        partialLines = partialLines.toTypedArray(),
        stats =
            CoverageStats(
                statementStat = CoverageStat(10, missedLines.size),
                lineStat = CoverageStat(20, missedLines.size),
                branchStat = CoverageStat(3, partialLines.size),
            ),
        filePath = filePath,
    )

    protected class Repositories(koin: Koin) {
        val testRunRepository: TestRunRepository = koin.get()
        val testSuiteRepository: TestSuiteRepository = koin.get()
        val testCaseRepository: TestCaseRepository = koin.get()
        val testRunMetadataRepository: TestRunMetadataRepository = koin.get()
        val testRunSystemAttributesRepository: TestRunSystemAttributesRepository = koin.get()
        val attachmentRepository: AttachmentRepository = koin.get()
        val resultsProcessingRepository: ResultsProcessingRepository = koin.get()
        val performanceResultsRepository: PerformanceResultsRepository = koin.get()
        val codeQualityReportRepository: CodeQualityReportRepository = koin.get()
        val coverageRepository: CoverageRepository = koin.get()
        val repositoryTestRunRepository: RepositoryTestRunRepository = koin.get()
        val repositoryCoverageRepository: RepositoryCoverageRepository = koin.get()
        val repositoryPerformanceRepository: RepositoryPerformanceRepository = koin.get()
        val previousTestRunRepository: PreviousTestRunRepository = koin.get()
        val organizationCoverageRepository: OrganizationCoverageRepository = koin.get()
        val repositorySlackChannelRepository: RepositorySlackChannelRepository = koin.get()
    }

    companion object {
        private const val BUCKET_NAME = "persistenceparity"
    }
}
