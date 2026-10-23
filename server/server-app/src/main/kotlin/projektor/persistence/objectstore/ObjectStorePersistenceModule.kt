package projektor.persistence.objectstore

import org.koin.dsl.module
import projektor.attachment.AttachmentObjectStoreRepository
import projektor.attachment.AttachmentRepository
import projektor.compare.PreviousTestRunObjectStoreRepository
import projektor.compare.PreviousTestRunRepository
import projektor.coverage.CoverageObjectStoreRepository
import projektor.coverage.CoverageRepository
import projektor.error.ProcessingFailureObjectStoreRepository
import projektor.error.ProcessingFailureRepository
import projektor.incomingresults.processing.ResultsProcessingObjectStoreRepository
import projektor.incomingresults.processing.ResultsProcessingRepository
import projektor.metadata.TestRunMetadataObjectStoreRepository
import projektor.metadata.TestRunMetadataRepository
import projektor.notification.slack.RepositorySlackChannelObjectStoreRepository
import projektor.notification.slack.RepositorySlackChannelRepository
import projektor.objectstore.json.JsonObjectStore
import projektor.organization.coverage.OrganizationCoverageObjectStoreRepository
import projektor.organization.coverage.OrganizationCoverageRepository
import projektor.performance.PerformanceResultsObjectStoreRepository
import projektor.performance.PerformanceResultsRepository
import projektor.persistence.PersistenceInstrumentation
import projektor.persistence.instrumentedSingle
import projektor.quality.CodeQualityReportObjectStoreRepository
import projektor.quality.CodeQualityReportRepository
import projektor.repository.coverage.RepositoryCoverageObjectStoreRepository
import projektor.repository.coverage.RepositoryCoverageRepository
import projektor.repository.performance.RepositoryPerformanceObjectStoreRepository
import projektor.repository.performance.RepositoryPerformanceRepository
import projektor.repository.testrun.RepositoryTestRunObjectStoreRepository
import projektor.repository.testrun.RepositoryTestRunRepository
import projektor.schedule.SchedulerLock
import projektor.testcase.TestCaseObjectStoreRepository
import projektor.testcase.TestCaseRepository
import projektor.testrun.TestRunObjectStoreRepository
import projektor.testrun.TestRunRepository
import projektor.testrun.attributes.TestRunSystemAttributesObjectStoreRepository
import projektor.testrun.attributes.TestRunSystemAttributesRepository
import projektor.testsuite.TestSuiteObjectStoreRepository
import projektor.testsuite.TestSuiteRepository

/**
 * Repositories and scheduler lock for the object-store persistence backend.
 */
fun createObjectStorePersistenceModule(
    store: JsonObjectStore,
    instrumentation: PersistenceInstrumentation,
    testRunIndex: TestRunIndex = TestRunIndex(TestRunDocumentStore(store)),
) = module {
    single { store }
    single { testRunIndex.documentStore }
    single { testRunIndex }

    instrumentedSingle<TestRunRepository>(instrumentation) { TestRunObjectStoreRepository(get(), get()) }
    instrumentedSingle<TestSuiteRepository>(instrumentation) { TestSuiteObjectStoreRepository(get()) }
    instrumentedSingle<TestCaseRepository>(instrumentation) { TestCaseObjectStoreRepository(get(), get()) }
    instrumentedSingle<TestRunMetadataRepository>(instrumentation) { TestRunMetadataObjectStoreRepository(get(), get()) }
    instrumentedSingle<TestRunSystemAttributesRepository>(instrumentation) { TestRunSystemAttributesObjectStoreRepository(get()) }
    instrumentedSingle<AttachmentRepository>(instrumentation) { AttachmentObjectStoreRepository(get()) }
    instrumentedSingle<ResultsProcessingRepository>(instrumentation) { ResultsProcessingObjectStoreRepository(get(), get()) }
    instrumentedSingle<ProcessingFailureRepository>(instrumentation) { ProcessingFailureObjectStoreRepository(get()) }
    instrumentedSingle<PerformanceResultsRepository>(instrumentation) { PerformanceResultsObjectStoreRepository(get(), get()) }
    instrumentedSingle<CodeQualityReportRepository>(instrumentation) { CodeQualityReportObjectStoreRepository(get()) }
    instrumentedSingle<CoverageRepository>(instrumentation) { CoverageObjectStoreRepository(get(), get()) }

    instrumentedSingle<RepositoryTestRunRepository>(instrumentation) { RepositoryTestRunObjectStoreRepository(get(), get()) }
    instrumentedSingle<RepositoryCoverageRepository>(instrumentation) { RepositoryCoverageObjectStoreRepository(get(), get()) }
    instrumentedSingle<RepositoryPerformanceRepository>(instrumentation) { RepositoryPerformanceObjectStoreRepository(get()) }
    instrumentedSingle<PreviousTestRunRepository>(instrumentation) { PreviousTestRunObjectStoreRepository(get(), get()) }
    instrumentedSingle<OrganizationCoverageRepository>(instrumentation) { OrganizationCoverageObjectStoreRepository(get()) }
    instrumentedSingle<RepositorySlackChannelRepository>(instrumentation) { RepositorySlackChannelObjectStoreRepository(get()) }

    single { SchedulerLock(ObjectStoreLockProvider(get())) }
}
