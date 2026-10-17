package projektor.persistence

import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import projektor.attachment.AttachmentRepository
import projektor.compare.PreviousTestRunRepository
import projektor.compare.PreviousTestRunService
import projektor.coverage.CoverageRepository
import projektor.coverage.CoverageService
import projektor.error.ProcessingFailureRepository
import projektor.error.ProcessingFailureService
import projektor.incomingresults.processing.ResultsProcessingRepository
import projektor.metadata.TestRunMetadataRepository
import projektor.metrics.MetricsService
import projektor.notification.slack.RepositorySlackChannelRepository
import projektor.organization.coverage.OrganizationCoverageRepository
import projektor.performance.PerformanceResultsRepository
import projektor.quality.CodeQualityReportRepository
import projektor.repository.coverage.RepositoryCoverageRepository
import projektor.repository.performance.RepositoryPerformanceRepository
import projektor.repository.testrun.RepositoryTestRunRepository
import projektor.testcase.TestCaseRepository
import projektor.testrun.TestRunRepository
import projektor.testrun.attributes.TestRunSystemAttributesRepository
import projektor.testsuite.TestSuiteRepository
import java.util.TimeZone

/**
 * Base for repository tests that run against every persistence backend. Each test class is abstract,
 * with a subclass per backend that implements [createBackend].
 *
 * Tests seed and check data only through the repository interfaces (see [TestRunGenerator]), and generate
 * random public IDs and repo names since the database backend is shared across tests.
 */
abstract class RepositoryTestCase {
    init {
        // Like the other database test cases, since the database backend's timestamp conversions assume the JVM runs in UTC
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    }

    protected abstract fun createBackend(): PersistenceTestBackend

    protected lateinit var backend: PersistenceTestBackend
    protected lateinit var testRunGenerator: TestRunGenerator

    @BeforeEach
    fun setUpBackend() {
        backend = createBackend()
        testRunGenerator = TestRunGenerator(this)
    }

    @AfterEach
    fun closeBackend() {
        backend.close()
    }

    val testRunRepository: TestRunRepository get() = backend.koin.get()
    val testSuiteRepository: TestSuiteRepository get() = backend.koin.get()
    val testCaseRepository: TestCaseRepository get() = backend.koin.get()
    val testRunMetadataRepository: TestRunMetadataRepository get() = backend.koin.get()
    val testRunSystemAttributesRepository: TestRunSystemAttributesRepository get() = backend.koin.get()
    val attachmentRepository: AttachmentRepository get() = backend.koin.get()
    val resultsProcessingRepository: ResultsProcessingRepository get() = backend.koin.get()
    val processingFailureRepository: ProcessingFailureRepository get() = backend.koin.get()
    val performanceResultsRepository: PerformanceResultsRepository get() = backend.koin.get()
    val codeQualityReportRepository: CodeQualityReportRepository get() = backend.koin.get()
    val coverageRepository: CoverageRepository get() = backend.koin.get()
    val repositoryTestRunRepository: RepositoryTestRunRepository get() = backend.koin.get()
    val repositoryCoverageRepository: RepositoryCoverageRepository get() = backend.koin.get()
    val repositoryPerformanceRepository: RepositoryPerformanceRepository get() = backend.koin.get()
    val previousTestRunRepository: PreviousTestRunRepository get() = backend.koin.get()
    val organizationCoverageRepository: OrganizationCoverageRepository get() = backend.koin.get()
    val repositorySlackChannelRepository: RepositorySlackChannelRepository get() = backend.koin.get()

    val coverageService: CoverageService
        get() =
            CoverageService(
                coverageRepository,
                MetricsService(SimpleMeterRegistry()),
                PreviousTestRunService(previousTestRunRepository),
                ProcessingFailureService(processingFailureRepository),
            )
}
