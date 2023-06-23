package projektor

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import org.koin.test.get
import org.koin.test.inject
import projektor.attachment.AttachmentConfig
import projektor.attachment.AttachmentService
import projektor.auth.AuthConfig
import projektor.compare.PreviousTestRunService
import projektor.coverage.CoverageRepository
import projektor.coverage.CoverageService
import projektor.database.DataSourceConfig
import projektor.database.generated.tables.daos.CodeCoverageGroupDao
import projektor.database.generated.tables.daos.CodeCoverageRunDao
import projektor.database.generated.tables.daos.CodeCoverageStatsDao
import projektor.database.generated.tables.daos.GitMetadataDao
import projektor.database.generated.tables.daos.ResultsMetadataDao
import projektor.database.generated.tables.daos.ResultsProcessingDao
import projektor.database.generated.tables.daos.ResultsProcessingFailureDao
import projektor.database.generated.tables.daos.TestCaseDao
import projektor.database.generated.tables.daos.TestFailureDao
import projektor.database.generated.tables.daos.TestRunAttachmentDao
import projektor.database.generated.tables.daos.TestRunDao
import projektor.database.generated.tables.daos.TestRunSystemAttributesDao
import projektor.database.generated.tables.daos.TestSuiteDao
import projektor.database.generated.tables.daos.TestSuiteGroupDao
import projektor.error.ProcessingFailureService
import projektor.message.MessageConfig
import projektor.metrics.InfluxMetricsConfig
import projektor.metrics.MetricsService
import projektor.metrics.createRegistry
import projektor.notification.NotificationConfig
import java.util.TimeZone

open class DatabaseRepositoryTestCase : KoinTest {
    init {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    }

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

    val attachmentConfig = AttachmentConfig(
        url = "http://localhost:9000",
        bucketName = "attachmentstesting",
        autoCreateBucket = true,
        accessKey = "minio_access_key",
        secretKey = "minio_secret_key",
        maxSizeMB = null
    )
    var attachmentsEnabled = false
    lateinit var attachmentService: AttachmentService

    @BeforeEach
    fun setup() {
        val hikariConfig = HikariConfig()

        val dataSourceConfig = DataSourceConfig(
            System.getenv("DB_URL") ?: "jdbc:postgresql://localhost:5433/projektordb",
            System.getenv("DB_USERNAME") ?: "testuser",
            System.getenv("DB_PASSWORD") ?: "testpass",
            "public",
            3
        )

        hikariConfig.username = dataSourceConfig.username
        hikariConfig.password = dataSourceConfig.password
        hikariConfig.jdbcUrl = dataSourceConfig.jdbcUrl
        hikariConfig.schema = dataSourceConfig.schema
        hikariConfig.maximumPoolSize = dataSourceConfig.maximumPoolSize

        dataSource = HikariDataSource(hikariConfig)
        DataSourceConfig.flywayMigrate(dataSource, dataSourceConfig)

        dslContext = DSL.using(dataSource, SQLDialect.POSTGRES)

        val metricsConfig = InfluxMetricsConfig(
            false,
            "fakedb",
            "http://localhost",
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
                    MessageConfig(listOf()),
                    NotificationConfig(null),
                    null,
                    null
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

        val coverageRepository: CoverageRepository by inject()
        val previousTestRunService: PreviousTestRunService by inject()
        val processingFailureService: ProcessingFailureService by inject()
        val metricsService: MetricsService by inject()
        coverageService = CoverageService(coverageRepository, metricsService, previousTestRunService, processingFailureService)

        testRunDBGenerator = TestRunDBGenerator(
            testRunDao,
            testSuiteGroupDao,
            testSuiteDao,
            testCaseDao,
            testFailureDao,
            testRunSystemAttributesDao,
            gitMetadataDao,
            resultsMetadataDao,
            coverageService,
            attachmentDao
        )

        if (attachmentsEnabled) {
            attachmentService = AttachmentService(
                attachmentConfig,
                get()
            )
            attachmentService.conditionallyCreateBucketIfNotExists()
        }
    }

    @AfterEach
    fun closeKoin() {
        stopKoin()
    }

    @AfterEach
    fun closeDataSource() {
        dataSource.close()
    }
}
