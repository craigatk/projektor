package projektor

import com.zaxxer.hikari.HikariDataSource
import io.ktor.util.KtorExperimentalAPI
import io.micrometer.core.instrument.MeterRegistry
import org.jooq.DSLContext
import org.koin.dsl.module
import projektor.attachment.AttachmentDatabaseRepository
import projektor.attachment.AttachmentRepository
import projektor.auth.AuthConfig
import projektor.auth.AuthService
import projektor.compare.PreviousTestRunDatabaseRepository
import projektor.compare.PreviousTestRunRepository
import projektor.compare.PreviousTestRunService
import projektor.coverage.CoverageDatabaseRepository
import projektor.coverage.CoverageRepository
import projektor.coverage.CoverageService
import projektor.incomingresults.GroupedResultsConverter
import projektor.incomingresults.GroupedTestResultsService
import projektor.incomingresults.TestResultsProcessingService
import projektor.incomingresults.TestResultsService
import projektor.incomingresults.processing.ResultsProcessingDatabaseRepository
import projektor.incomingresults.processing.ResultsProcessingRepository
import projektor.message.MessageConfig
import projektor.message.MessageService
import projektor.metrics.MetricsService
import projektor.parser.grouped.GroupedResultsParser
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

@KtorExperimentalAPI
fun createAppModule(
    dataSource: HikariDataSource,
    authConfig: AuthConfig,
    dslContext: DSLContext,
    metricRegistry: MeterRegistry,
    messageConfig: MessageConfig
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
    single<ResultsProcessingRepository> { ResultsProcessingDatabaseRepository(get()) }
    single<PreviousTestRunRepository> { PreviousTestRunDatabaseRepository(get()) }

    single<CoverageRepository> { CoverageDatabaseRepository(get()) }

    single { CoverageService(get(), get()) }
    single { GroupedResultsParser() }
    single { GroupedResultsConverter(get(), get()) }
    single { GroupedTestResultsService(get(), get(), get(), get(), get()) }
    single { MessageService(messageConfig) }
    single { PreviousTestRunService(get()) }
    single { TestCaseService(get()) }
    single { TestSuiteService(get()) }
    single { TestResultsProcessingService(get()) }
    single { TestResultsService(get(), get(), get(), get()) }
    single { TestRunService(get()) }
    single { TestRunSystemAttributesService(get()) }
    single { SchedulerLock(dataSource) }
    single { Scheduler(get()) }
}
