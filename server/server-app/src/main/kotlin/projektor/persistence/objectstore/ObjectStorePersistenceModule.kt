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
import projektor.quality.CodeQualityReportObjectStoreRepository
import projektor.quality.CodeQualityReportRepository
import projektor.repository.coverage.RepositoryCoverageObjectStoreRepository
import projektor.repository.coverage.RepositoryCoverageRepository
import projektor.repository.performance.RepositoryPerformanceObjectStoreRepository
import projektor.repository.performance.RepositoryPerformanceRepository
import projektor.repository.testrun.RepositoryTestRunObjectStoreRepository
import projektor.repository.testrun.RepositoryTestRunRepository
import projektor.testcase.TestCaseObjectStoreRepository
import projektor.testcase.TestCaseRepository
import projektor.testrun.TestRunObjectStoreRepository
import projektor.testrun.TestRunRepository
import projektor.testrun.attributes.TestRunSystemAttributesObjectStoreRepository
import projektor.testrun.attributes.TestRunSystemAttributesRepository
import projektor.testsuite.TestSuiteObjectStoreRepository
import projektor.testsuite.TestSuiteRepository

/**
 * Repositories for the object-store persistence backend.
 *
 * Not yet complete: the scheduler lock comes in phase 4 of the object-store persistence plan,
 * so this backend can't be selected with PERSISTENCE_TYPE yet.
 */
fun createObjectStorePersistenceModule(store: JsonObjectStore) =
    module {
        single { store }
        single { TestRunDocumentStore(get()) }
        single { TestRunIndex(get()) }

        single<TestRunRepository> { TestRunObjectStoreRepository(get(), get()) }
        single<TestSuiteRepository> { TestSuiteObjectStoreRepository(get()) }
        single<TestCaseRepository> { TestCaseObjectStoreRepository(get(), get()) }
        single<TestRunMetadataRepository> { TestRunMetadataObjectStoreRepository(get(), get()) }
        single<TestRunSystemAttributesRepository> { TestRunSystemAttributesObjectStoreRepository(get()) }
        single<AttachmentRepository> { AttachmentObjectStoreRepository(get()) }
        single<ResultsProcessingRepository> { ResultsProcessingObjectStoreRepository(get(), get()) }
        single<ProcessingFailureRepository> { ProcessingFailureObjectStoreRepository(get()) }
        single<PerformanceResultsRepository> { PerformanceResultsObjectStoreRepository(get(), get()) }
        single<CodeQualityReportRepository> { CodeQualityReportObjectStoreRepository(get()) }
        single<CoverageRepository> { CoverageObjectStoreRepository(get(), get()) }

        single<RepositoryTestRunRepository> { RepositoryTestRunObjectStoreRepository(get(), get()) }
        single<RepositoryCoverageRepository> { RepositoryCoverageObjectStoreRepository(get(), get()) }
        single<RepositoryPerformanceRepository> { RepositoryPerformanceObjectStoreRepository(get()) }
        single<PreviousTestRunRepository> { PreviousTestRunObjectStoreRepository(get(), get()) }
        single<OrganizationCoverageRepository> { OrganizationCoverageObjectStoreRepository(get()) }
        single<RepositorySlackChannelRepository> { RepositorySlackChannelObjectStoreRepository(get()) }
    }
