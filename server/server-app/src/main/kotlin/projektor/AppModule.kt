package projektor

import com.zaxxer.hikari.HikariDataSource
import io.micrometer.core.instrument.MeterRegistry
import org.jooq.DSLContext
import org.koin.dsl.module
import projektor.attachment.AttachmentDatabaseRepository
import projektor.attachment.AttachmentRepository
import projektor.attachment.AttachmentService
import projektor.auth.AuthConfig
import projektor.auth.AuthService
import projektor.badge.CoverageBadgeService
import projektor.compare.PreviousTestRunDatabaseRepository
import projektor.compare.PreviousTestRunRepository
import projektor.compare.PreviousTestRunService
import projektor.coverage.CoverageDatabaseRepository
import projektor.coverage.CoverageRepository
import projektor.coverage.CoverageService
import projektor.error.ProcessingFailureDatabaseRepository
import projektor.error.ProcessingFailureRepository
import projektor.error.ProcessingFailureService
import projektor.incomingresults.AppendTestResultsService
import projektor.incomingresults.GroupedResultsConverter
import projektor.incomingresults.GroupedTestResultsService
import projektor.incomingresults.TestResultsProcessingService
import projektor.incomingresults.TestResultsService
import projektor.incomingresults.processing.ResultsProcessingDatabaseRepository
import projektor.incomingresults.processing.ResultsProcessingRepository
import projektor.message.MessageConfig
import projektor.message.MessageService
import projektor.metadata.TestRunMetadataDatabaseRepository
import projektor.metadata.TestRunMetadataRepository
import projektor.metadata.TestRunMetadataService
import projektor.metrics.MetricsService
import projektor.notification.NotificationConfig
import projektor.notification.badge.SvgCoverageBadgeCreator
import projektor.notification.github.GitHubPullRequestCommentService
import projektor.notification.github.comment.GitHubCommentService
import projektor.organization.coverage.OrganizationCoverageDatabaseRepository
import projektor.organization.coverage.OrganizationCoverageRepository
import projektor.organization.coverage.OrganizationCoverageService
import projektor.parser.grouped.GroupedResultsParser
import projektor.parser.performance.PerformanceResultsParser
import projektor.performance.PerformanceResultsDatabaseRepository
import projektor.performance.PerformanceResultsRepository
import projektor.performance.PerformanceResultsService
import projektor.quality.CodeQualityReportDatabaseRepository
import projektor.quality.CodeQualityReportRepository
import projektor.repository.coverage.RepositoryCoverageDatabaseRepository
import projektor.repository.coverage.RepositoryCoverageRepository
import projektor.repository.coverage.RepositoryCoverageService
import projektor.repository.performance.RepositoryPerformanceDatabaseRepository
import projektor.repository.performance.RepositoryPerformanceRepository
import projektor.repository.performance.RepositoryPerformanceService
import projektor.repository.testrun.RepositoryTestRunDatabaseRepository
import projektor.repository.testrun.RepositoryTestRunRepository
import projektor.repository.testrun.RepositoryTestRunService
import projektor.results.processor.TestResultsProcessor
import projektor.schedule.Scheduler
import projektor.schedule.SchedulerLock
import projektor.testcase.TestCaseDatabaseRepository
import projektor.testcase.TestCaseRepository
import projektor.testcase.TestCaseService
import projektor.testrun.TestRunDatabaseRepository
import projektor.testrun.TestRunRepository
import projektor.testrun.TestRunService
import projektor.testrun.attributes.TestRunSystemAttributesDatabaseRepository
import projektor.testrun.attributes.TestRunSystemAttributesRepository
import projektor.testrun.attributes.TestRunSystemAttributesService
import projektor.testsuite.TestSuiteDatabaseRepository
import projektor.testsuite.TestSuiteRepository
import projektor.testsuite.TestSuiteService

fun createAppModule(
    dataSource: HikariDataSource,
    authConfig: AuthConfig,
    dslContext: DSLContext,
    metricRegistry: MeterRegistry,
    messageConfig: MessageConfig,
    notificationConfig: NotificationConfig,
    gitHubCommentService: GitHubCommentService?,
    attachmentService: AttachmentService?
) = module {
    single { dataSource }
    single { TestResultsProcessor() }
    single { dslContext }
    single { metricRegistry }
    single { MetricsService(metricRegistry) }
    single { AuthService(authConfig) }

    single<TestCaseRepository> { TestCaseDatabaseRepository(get()) }
    single<TestSuiteRepository> { TestSuiteDatabaseRepository(get()) }
    single<TestRunRepository> { TestRunDatabaseRepository(get()) }
    single<TestRunSystemAttributesRepository> { TestRunSystemAttributesDatabaseRepository(get()) }
    single<AttachmentRepository> { AttachmentDatabaseRepository(get()) }
    single<ResultsProcessingRepository> { ResultsProcessingDatabaseRepository(get(), get()) }
    single<PreviousTestRunRepository> { PreviousTestRunDatabaseRepository(get()) }
    single<TestRunMetadataRepository> { TestRunMetadataDatabaseRepository(get()) }
    single<ProcessingFailureRepository> { ProcessingFailureDatabaseRepository(get()) }

    single<CoverageRepository> { CoverageDatabaseRepository(get()) }

    single<OrganizationCoverageRepository> { OrganizationCoverageDatabaseRepository(get()) }

    single<RepositoryCoverageRepository> { RepositoryCoverageDatabaseRepository(get()) }
    single<RepositoryPerformanceRepository> { RepositoryPerformanceDatabaseRepository(get()) }
    single<RepositoryTestRunRepository> { RepositoryTestRunDatabaseRepository(get()) }

    single<PerformanceResultsRepository> { PerformanceResultsDatabaseRepository(get()) }
    single { PerformanceResultsService(get()) }

    single { AppendTestResultsService(get(), get()) }
    single { CoverageService(get(), get(), get(), get()) }
    single { GroupedResultsParser() }
    single { PerformanceResultsParser() }
    single { GroupedResultsConverter(get(), get(), get()) }
    single { GroupedTestResultsService(get(), get(), get(), get(), get(), get(), get(), get(), get(), get()) }
    single { MessageService(messageConfig) }
    single { PreviousTestRunService(get()) }
    single { ProcessingFailureService(get()) }
    single { TestCaseService(get(), attachmentService) }
    single { TestSuiteService(get()) }
    single { TestResultsProcessingService(get()) }
    single { TestResultsService(get(), get(), get(), get()) }
    single { TestRunService(get()) }
    single { TestRunSystemAttributesService(get()) }
    single { TestRunMetadataService(get()) }

    single { SchedulerLock(dataSource) }
    single { Scheduler(get()) }

    single { OrganizationCoverageService(get(), get()) }

    single { RepositoryCoverageService(get()) }
    single { RepositoryPerformanceService(get()) }
    single { RepositoryTestRunService(get()) }

    single<CodeQualityReportRepository> { CodeQualityReportDatabaseRepository(get()) }

    single { GitHubPullRequestCommentService(notificationConfig, gitHubCommentService) }

    single { CoverageBadgeService(get(), get(), get()) }
    single { SvgCoverageBadgeCreator() }
}
