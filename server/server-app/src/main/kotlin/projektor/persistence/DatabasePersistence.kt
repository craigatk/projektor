package projektor.persistence

import com.zaxxer.hikari.HikariDataSource
import io.micrometer.core.instrument.MeterRegistry
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider
import org.jooq.DSLContext
import org.koin.dsl.module
import projektor.attachment.AttachmentDatabaseRepository
import projektor.attachment.AttachmentRepository
import projektor.compare.PreviousTestRunDatabaseRepository
import projektor.compare.PreviousTestRunRepository
import projektor.coverage.CoverageDatabaseRepository
import projektor.coverage.CoverageRepository
import projektor.database.DataSourceConfig
import projektor.error.ProcessingFailureDatabaseRepository
import projektor.error.ProcessingFailureRepository
import projektor.incomingresults.processing.ResultsProcessingDatabaseRepository
import projektor.incomingresults.processing.ResultsProcessingRepository
import projektor.metadata.TestRunMetadataDatabaseRepository
import projektor.metadata.TestRunMetadataRepository
import projektor.notification.slack.RepositorySlackChannelDatabaseRepository
import projektor.notification.slack.RepositorySlackChannelRepository
import projektor.organization.coverage.OrganizationCoverageDatabaseRepository
import projektor.organization.coverage.OrganizationCoverageRepository
import projektor.performance.PerformanceResultsDatabaseRepository
import projektor.performance.PerformanceResultsRepository
import projektor.quality.CodeQualityReportDatabaseRepository
import projektor.quality.CodeQualityReportRepository
import projektor.repository.coverage.RepositoryCoverageDatabaseRepository
import projektor.repository.coverage.RepositoryCoverageRepository
import projektor.repository.performance.RepositoryPerformanceDatabaseRepository
import projektor.repository.performance.RepositoryPerformanceRepository
import projektor.repository.testrun.RepositoryTestRunDatabaseRepository
import projektor.repository.testrun.RepositoryTestRunRepository
import projektor.schedule.SchedulerLock
import projektor.testcase.TestCaseDatabaseRepository
import projektor.testcase.TestCaseRepository
import projektor.testrun.TestRunDatabaseRepository
import projektor.testrun.TestRunRepository
import projektor.testrun.attributes.TestRunSystemAttributesDatabaseRepository
import projektor.testrun.attributes.TestRunSystemAttributesRepository
import projektor.testsuite.TestSuiteDatabaseRepository
import projektor.testsuite.TestSuiteRepository
import javax.sql.DataSource

class DatabasePersistence(
    private val dataSource: HikariDataSource,
    dslContext: DSLContext,
    instrumentation: PersistenceInstrumentation,
) : Persistence {
    override val koinModule = createDatabasePersistenceModule(dataSource, dslContext, instrumentation)

    override val attachmentRepository: AttachmentRepository =
        instrumentation.instrument<AttachmentRepository>(
            AttachmentDatabaseRepository(dslContext),
        )

    override fun close() {
        dataSource.close()
    }

    companion object {
        fun create(
            dataSourceConfig: DataSourceConfig,
            metricRegistry: MeterRegistry,
            instrumentation: PersistenceInstrumentation,
        ): DatabasePersistence {
            val dataSource = DataSourceConfig.createDataSource(dataSourceConfig, metricRegistry)
            DataSourceConfig.flywayMigrate(dataSource, dataSourceConfig)
            val dslContext = DataSourceConfig.createDSLContext(dataSource, dataSourceConfig)

            return DatabasePersistence(dataSource, dslContext, instrumentation)
        }
    }
}

fun createDatabasePersistenceModule(
    dataSource: DataSource,
    dslContext: DSLContext,
    instrumentation: PersistenceInstrumentation,
) = module {
    single { dslContext }

    instrumentedSingle<TestCaseRepository>(instrumentation) { TestCaseDatabaseRepository(get()) }
    instrumentedSingle<TestSuiteRepository>(instrumentation) { TestSuiteDatabaseRepository(get()) }
    instrumentedSingle<TestRunRepository>(instrumentation) { TestRunDatabaseRepository(get()) }
    instrumentedSingle<TestRunSystemAttributesRepository>(instrumentation) { TestRunSystemAttributesDatabaseRepository(get()) }
    instrumentedSingle<AttachmentRepository>(instrumentation) { AttachmentDatabaseRepository(get()) }
    instrumentedSingle<ResultsProcessingRepository>(instrumentation) { ResultsProcessingDatabaseRepository(get(), get()) }
    instrumentedSingle<PreviousTestRunRepository>(instrumentation) { PreviousTestRunDatabaseRepository(get()) }
    instrumentedSingle<TestRunMetadataRepository>(instrumentation) { TestRunMetadataDatabaseRepository(get()) }
    instrumentedSingle<ProcessingFailureRepository>(instrumentation) { ProcessingFailureDatabaseRepository(get()) }

    instrumentedSingle<CoverageRepository>(instrumentation) { CoverageDatabaseRepository(get()) }

    instrumentedSingle<OrganizationCoverageRepository>(instrumentation) { OrganizationCoverageDatabaseRepository(get()) }

    instrumentedSingle<RepositoryCoverageRepository>(instrumentation) { RepositoryCoverageDatabaseRepository(get()) }
    instrumentedSingle<RepositoryPerformanceRepository>(instrumentation) { RepositoryPerformanceDatabaseRepository(get()) }
    instrumentedSingle<RepositoryTestRunRepository>(instrumentation) { RepositoryTestRunDatabaseRepository(get()) }

    instrumentedSingle<PerformanceResultsRepository>(instrumentation) { PerformanceResultsDatabaseRepository(get()) }

    instrumentedSingle<CodeQualityReportRepository>(instrumentation) { CodeQualityReportDatabaseRepository(get()) }

    instrumentedSingle<RepositorySlackChannelRepository>(instrumentation) { RepositorySlackChannelDatabaseRepository(get()) }

    single { SchedulerLock(JdbcTemplateLockProvider(dataSource)) }
}
