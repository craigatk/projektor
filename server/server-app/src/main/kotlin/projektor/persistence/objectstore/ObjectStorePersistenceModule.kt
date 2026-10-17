package projektor.persistence.objectstore

import org.koin.dsl.module
import projektor.attachment.AttachmentObjectStoreRepository
import projektor.attachment.AttachmentRepository
import projektor.coverage.CoverageObjectStoreRepository
import projektor.coverage.CoverageRepository
import projektor.error.ProcessingFailureObjectStoreRepository
import projektor.error.ProcessingFailureRepository
import projektor.incomingresults.processing.ResultsProcessingObjectStoreRepository
import projektor.incomingresults.processing.ResultsProcessingRepository
import projektor.metadata.TestRunMetadataObjectStoreRepository
import projektor.metadata.TestRunMetadataRepository
import projektor.objectstore.json.JsonObjectStore
import projektor.performance.PerformanceResultsObjectStoreRepository
import projektor.performance.PerformanceResultsRepository
import projektor.quality.CodeQualityReportObjectStoreRepository
import projektor.quality.CodeQualityReportRepository
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
 * Not yet complete: the repositories that query across test runs (repository timelines, previous runs,
 * organization coverage, Slack channels) and the scheduler lock come in later phases of the
 * object-store persistence plan, so this backend can't be selected with PERSISTENCE_TYPE yet.
 */
fun createObjectStorePersistenceModule(store: JsonObjectStore) =
    module {
        single { store }
        single { TestRunDocumentStore(get()) }

        single<TestRunRepository> { TestRunObjectStoreRepository(get()) }
        single<TestSuiteRepository> { TestSuiteObjectStoreRepository(get()) }
        single<TestCaseRepository> { TestCaseObjectStoreRepository(get()) }
        single<TestRunMetadataRepository> { TestRunMetadataObjectStoreRepository(get()) }
        single<TestRunSystemAttributesRepository> { TestRunSystemAttributesObjectStoreRepository(get()) }
        single<AttachmentRepository> { AttachmentObjectStoreRepository(get()) }
        single<ResultsProcessingRepository> { ResultsProcessingObjectStoreRepository(get(), get()) }
        single<ProcessingFailureRepository> { ProcessingFailureObjectStoreRepository(get()) }
        single<PerformanceResultsRepository> { PerformanceResultsObjectStoreRepository(get()) }
        single<CodeQualityReportRepository> { CodeQualityReportObjectStoreRepository(get()) }
        single<CoverageRepository> { CoverageObjectStoreRepository(get()) }
    }
