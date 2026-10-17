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
) : Persistence {
    override val koinModule = createDatabasePersistenceModule(dataSource, dslContext)

    override val attachmentRepository: AttachmentRepository = AttachmentDatabaseRepository(dslContext)

    override fun close() {
        dataSource.close()
    }

    companion object {
        fun create(
            dataSourceConfig: DataSourceConfig,
            metricRegistry: MeterRegistry,
        ): DatabasePersistence {
            val dataSource = DataSourceConfig.createDataSource(dataSourceConfig, metricRegistry)
            DataSourceConfig.flywayMigrate(dataSource, dataSourceConfig)
            val dslContext = DataSourceConfig.createDSLContext(dataSource, dataSourceConfig)

            return DatabasePersistence(dataSource, dslContext)
        }
    }
}

fun createDatabasePersistenceModule(
    dataSource: DataSource,
    dslContext: DSLContext,
) = module {
    single { dslContext }

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

    single<CodeQualityReportRepository> { CodeQualityReportDatabaseRepository(get()) }

    single<RepositorySlackChannelRepository> { RepositorySlackChannelDatabaseRepository(get()) }

    single { SchedulerLock(JdbcTemplateLockProvider(dataSource)) }
}
