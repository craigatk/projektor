package projektor

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.util.KtorExperimentalAPI
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import projektor.auth.AuthConfig
import projektor.compare.PreviousTestRunDatabaseRepository
import projektor.compare.PreviousTestRunService
import projektor.coverage.CoverageDatabaseRepository
import projektor.coverage.CoverageService
import projektor.database.DataSourceConfig
import projektor.database.generated.tables.daos.*
import projektor.message.MessageConfig
import projektor.metrics.InfluxMetricsConfig
import projektor.metrics.createRegistry

open class DatabaseRepositoryTestCase : KoinTest {
    lateinit var dataSource: HikariDataSource
    lateinit var dslContext: DSLContext
    lateinit var testRunDao: TestRunDao
    lateinit var testSuiteGroupDao: TestSuiteGroupDao
    lateinit var testSuiteDao: TestSuiteDao
    lateinit var testCaseDao: TestCaseDao
    lateinit var testFailureDao: TestFailureDao
    lateinit var testRunDBGenerator: TestRunDBGenerator
    lateinit var resultsProcessingDao: ResultsProcessingDao
    lateinit var resultsProcessingFailureDao: ResultsProcessingFailureDao
    lateinit var attachmentDao: TestRunAttachmentDao
    lateinit var gitMetadataDao: GitMetadataDao
    lateinit var resultsMetadataDao: ResultsMetadataDao
    lateinit var testRunSystemAttributesDao: TestRunSystemAttributesDao

    lateinit var coverageRunDao: CodeCoverageRunDao
    lateinit var coverageGroupDao: CodeCoverageGroupDao
    lateinit var coverageStatsDao: CodeCoverageStatsDao
    lateinit var coverageService: CoverageService

    @KtorExperimentalAPI
    @BeforeEach
    fun setup() {
        val hikariConfig = HikariConfig()

        val dataSourceConfig = DataSourceConfig(
            System.getenv("DB_URL") ?: "jdbc:postgresql://localhost:5433/projektordb",
            System.getenv("DB_USERNAME") ?: "testuser",
            System.getenv("DB_PASSWORD") ?: "testpass",
            "public"
        )

        hikariConfig.username = dataSourceConfig.username
        hikariConfig.password = dataSourceConfig.password
        hikariConfig.jdbcUrl = dataSourceConfig.jdbcUrl
        hikariConfig.schema = dataSourceConfig.schema
        hikariConfig.maximumPoolSize = 3

        dataSource = HikariDataSource(hikariConfig)
        DataSourceConfig.flywayMigrate(dataSource, dataSourceConfig)

        dslContext = DSL.using(dataSource, SQLDialect.POSTGRES)

        val metricsConfig = InfluxMetricsConfig(
            false,
            "",
            "",
            null,
            null,
            false,
            10,
            "test"
        )

        startKoin {
            modules(
                createAppModule(
                    dataSource,
                    AuthConfig(null),
                    dslContext,
                    createRegistry(metricsConfig),
                    MessageConfig(listOf())
                )
            )
        }

        testRunDao = TestRunDao(dslContext.configuration())
        testSuiteGroupDao = TestSuiteGroupDao(dslContext.configuration())
        testSuiteDao = TestSuiteDao(dslContext.configuration())
        testCaseDao = TestCaseDao(dslContext.configuration())
        testFailureDao = TestFailureDao(dslContext.configuration())
        resultsProcessingDao = ResultsProcessingDao(dslContext.configuration())
        resultsProcessingFailureDao = ResultsProcessingFailureDao(dslContext.configuration())
        attachmentDao = TestRunAttachmentDao(dslContext.configuration())
        gitMetadataDao = GitMetadataDao(dslContext.configuration())
        resultsMetadataDao = ResultsMetadataDao(dslContext.configuration())
        testRunSystemAttributesDao = TestRunSystemAttributesDao(dslContext.configuration())

        coverageRunDao = CodeCoverageRunDao(dslContext.configuration())
        coverageGroupDao = CodeCoverageGroupDao(dslContext.configuration())
        coverageStatsDao = CodeCoverageStatsDao(dslContext.configuration())

        val coverageRepository = CoverageDatabaseRepository(dslContext)
        val previousTestRunService = PreviousTestRunService(PreviousTestRunDatabaseRepository(dslContext))
        coverageService = CoverageService(coverageRepository, previousTestRunService)

        testRunDBGenerator = TestRunDBGenerator(
            testRunDao,
            testSuiteGroupDao,
            testSuiteDao,
            testCaseDao,
            testFailureDao,
            testRunSystemAttributesDao,
            gitMetadataDao,
            resultsMetadataDao,
            coverageService
        )
    }

    @AfterEach
    fun closeDataSource() {
        stopKoin()

        dataSource.close()
    }
}
