package projektor

import ch.qos.logback.classic.Logger
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.zaxxer.hikari.HikariDataSource
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.testing.TestApplicationResponse
import io.micrometer.core.instrument.simple.SimpleMeterRegistry
import io.opentelemetry.api.GlobalOpenTelemetry
import io.opentelemetry.sdk.OpenTelemetrySdk
import io.opentelemetry.sdk.testing.exporter.InMemorySpanExporter
import io.opentelemetry.sdk.trace.SdkTracerProvider
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor
import org.awaitility.kotlin.await
import org.awaitility.kotlin.until
import org.awaitility.kotlin.untilNotNull
import org.jooq.DSLContext
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.koin.ktor.ext.get
import org.slf4j.LoggerFactory
import projektor.compare.PreviousTestRunService
import projektor.coverage.CoverageRepository
import projektor.coverage.CoverageService
import projektor.database.generated.tables.daos.CodeCoverageFileDao
import projektor.database.generated.tables.daos.CodeCoverageGroupDao
import projektor.database.generated.tables.daos.CodeCoverageRunDao
import projektor.database.generated.tables.daos.CodeCoverageStatsDao
import projektor.database.generated.tables.daos.CodeQualityReportDao
import projektor.database.generated.tables.daos.GitMetadataDao
import projektor.database.generated.tables.daos.GitRepositoryDao
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
import projektor.database.generated.tables.pojos.TestRun
import projektor.error.ProcessingFailureService
import projektor.metrics.MetricsService
import projektor.parser.ResultsXmlLoader
import projektor.server.api.PublicId
import projektor.server.api.results.ResultsProcessingStatus
import projektor.server.api.results.SaveResultsResponse
import projektor.server.example.coverage.CloverXmlLoader
import strikt.api.expectThat
import strikt.assertions.isEqualTo
import java.math.BigDecimal
import java.util.TimeZone
import kotlin.test.assertNotNull

open class ApplicationTestCase {
    init {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    }

    val cloverXmlLoader = CloverXmlLoader()
    val resultsXmlLoader = ResultsXmlLoader()

    val objectMapper: ObjectMapper = ObjectMapper()
        .registerKotlinModule()
        .registerModule(JavaTimeModule())
        .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    val exporter = InMemorySpanExporter.create()
    val tracerProvider = SdkTracerProvider
        .builder()
        .addSpanProcessor(SimpleSpanProcessor.create(exporter))
        .build()

    lateinit var dataSource: HikariDataSource
    lateinit var dslContext: DSLContext
    lateinit var testRunDao: TestRunDao
    lateinit var testSuiteGroupDao: TestSuiteGroupDao
    lateinit var testSuiteDao: TestSuiteDao
    lateinit var testCaseDao: TestCaseDao
    lateinit var testFailureDao: TestFailureDao
    lateinit var attachmentDao: TestRunAttachmentDao
    lateinit var testRunSystemAttributesDao: TestRunSystemAttributesDao
    lateinit var resultsProcessingDao: ResultsProcessingDao
    lateinit var resultsProcessingFailureDao: ResultsProcessingFailureDao
    lateinit var gitMetadataDao: GitMetadataDao
    lateinit var resultsMetadataDao: ResultsMetadataDao
    lateinit var testRunDBGenerator: TestRunDBGenerator

    lateinit var coverageRunDao: CodeCoverageRunDao
    lateinit var coverageGroupDao: CodeCoverageGroupDao
    lateinit var coverageFileDao: CodeCoverageFileDao
    lateinit var coverageStatsDao: CodeCoverageStatsDao
    lateinit var coverageService: CoverageService

    lateinit var gitRepositoryDao: GitRepositoryDao

    lateinit var codeQualityReportDao: CodeQualityReportDao

    lateinit var application: Application

    protected var databaseSchema: String = "public"

    protected var publishToken: String? = null

    protected var attachmentsEnabled: Boolean = false
    protected var attachmentsMaxSizeMB: BigDecimal? = null
    protected var attachmentsAccessKey = "minio_access_key"
    protected var attachmentsBucketName = "attachmentstesting"
    protected var attachmentsAutoCreateBucket = true

    protected var attachmentCleanupMaxAgeDays: Int? = null
    protected var reportCleanupMaxAgeDays: Int? = null

    protected var metricsEnabled: Boolean = false
    protected var metricsPort: Int = 0
    protected var metricsUsername: String? = null
    protected var metricsPassword: String? = null

    protected var globalMessages: String? = null

    protected var gitHubBaseUrl: String? = null

    protected var serverBaseUrl: String? = null
    protected var gitHubApiUrl: String? = null
    protected var gitHubAppId: String? = null
    protected var gitHubPrivateKeyEncoded: String? = null

    protected val meterRegistry = SimpleMeterRegistry()

    @BeforeEach
    fun setupTelemetry() {
        // Needed when running the full test suite as the global telemetry
        // instance may be set by another test and it can only be set once.
        GlobalOpenTelemetry.resetForTest()

        OpenTelemetrySdk.builder()
            .setTracerProvider(tracerProvider)
            .buildAndRegisterGlobal()
    }

    fun createTestApplication(application: Application) {
        val schema = databaseSchema

        (application.environment.config as MapApplicationConfig).apply {
            // Set here the properties
            put("ktor.datasource.username", System.getenv("DB_USERNAME") ?: "testuser")
            put("ktor.datasource.password", System.getenv("DB_PASSWORD") ?: "testpass")
            put("ktor.datasource.jdbcUrl", System.getenv("DB_URL") ?: "jdbc:postgresql://localhost:5433/projektordb")
            put("ktor.datasource.schema", schema)
            put("ktor.datasource.maximumPoolSize", "3")

            publishToken?.let { put("ktor.auth.publishToken", it) }

            if (attachmentsEnabled) {
                put("ktor.attachment.url", "http://localhost:9000")
                put("ktor.attachment.bucketName", attachmentsBucketName)
                put("ktor.attachment.autoCreateBucket", attachmentsAutoCreateBucket.toString())
                put("ktor.attachment.accessKey", attachmentsAccessKey)
                put("ktor.attachment.secretKey", "minio_secret_key")
                attachmentsMaxSizeMB?.let { put("ktor.attachment.maxSizeMB", it.toString()) }
            }

            attachmentCleanupMaxAgeDays?.let {
                put("ktor.cleanup.maxAttachmentAgeDays", it.toString())
            }

            reportCleanupMaxAgeDays?.let {
                put("ktor.cleanup.maxReportAgeDays", it.toString())
            }

            if (metricsEnabled) {
                put("ktor.metrics.influxdb.enabled", "true")
                put("ktor.metrics.influxdb.uri", "http://localhost:$metricsPort")
                put("ktor.metrics.influxdb.autoCreateDb", "true")
                put("ktor.metrics.influxdb.interval", "1")

                metricsUsername?.let { username -> put("ktor.metrics.influxdb.username", username) }
                metricsPassword?.let { password -> put("ktor.metrics.influxdb.password", password) }
            }

            globalMessages?.let { put("ktor.message.global", it) }

            gitHubBaseUrl?.let { put("ktor.versionControl.gitHubBaseUrl", it) }

            serverBaseUrl?.let { put("ktor.notification.serverBaseUrl", it) }
            gitHubApiUrl?.let { put("ktor.notification.gitHub.gitHubApiUrl", it) }
            gitHubAppId?.let { put("ktor.notification.gitHub.gitHubAppId", it) }
            gitHubPrivateKeyEncoded?.let { put("ktor.notification.gitHub.privateKey", it) }
        }

        val meterRegistryToUse = if (metricsEnabled) null else meterRegistry

        application.main(meterRegistry = meterRegistryToUse)

        dataSource = application.get()
        dslContext = application.get()
        testRunDao = TestRunDao(dslContext.configuration())
        testSuiteGroupDao = TestSuiteGroupDao(dslContext.configuration())
        testSuiteDao = TestSuiteDao(dslContext.configuration())
        testCaseDao = TestCaseDao(dslContext.configuration())
        testFailureDao = TestFailureDao(dslContext.configuration())
        attachmentDao = TestRunAttachmentDao(dslContext.configuration())
        testRunSystemAttributesDao = TestRunSystemAttributesDao(dslContext.configuration())
        resultsProcessingDao = ResultsProcessingDao(dslContext.configuration())
        resultsProcessingFailureDao = ResultsProcessingFailureDao(dslContext.configuration())
        gitMetadataDao = GitMetadataDao(dslContext.configuration())
        resultsMetadataDao = ResultsMetadataDao(dslContext.configuration())

        coverageRunDao = CodeCoverageRunDao(dslContext.configuration())
        coverageGroupDao = CodeCoverageGroupDao(dslContext.configuration())
        coverageFileDao = CodeCoverageFileDao(dslContext.configuration())
        coverageStatsDao = CodeCoverageStatsDao(dslContext.configuration())

        val coverageRepository: CoverageRepository = application.get()
        val previousTestRunService: PreviousTestRunService = application.get()
        val processingFailureService: ProcessingFailureService = application.get()
        val metricsService: MetricsService = application.get()
        coverageService = CoverageService(coverageRepository, metricsService, previousTestRunService, processingFailureService)

        gitRepositoryDao = GitRepositoryDao(dslContext.configuration())

        codeQualityReportDao = CodeQualityReportDao(dslContext.configuration())

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

        this.application = application
    }

    fun waitUntilTestRunHasAttachments(publicId: PublicId, attachmentCount: Int) {
        await until { attachmentDao.fetchByTestRunPublicId(publicId.id).size == attachmentCount }
    }

    fun getLogContents(): String {
        val appLogger = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME) as Logger
        val testAppender = appLogger.getAppender("test-appender") as TestLogAppender

        return testAppender.getLogContents()
    }

    fun waitForTestRunSaveToComplete(response: TestApplicationResponse): Pair<PublicId, TestRun> {
        expectThat(response.status()).isEqualTo(HttpStatusCode.OK)

        val resultsResponse = objectMapper.readValue(response.content, SaveResultsResponse::class.java)

        val publicId = resultsResponse.id
        assertNotNull(publicId)
        expectThat(resultsResponse.uri).isEqualTo("/tests/$publicId")

        await until { resultsProcessingDao.fetchOneByPublicId(publicId).status == ResultsProcessingStatus.SUCCESS.name }

        val testRun = await untilNotNull { testRunDao.fetchOneByPublicId(publicId) }
        assertNotNull(testRun)

        return Pair(PublicId(publicId), testRun)
    }

    protected fun loadTextFromFile(filename: String) = javaClass
        .getResourceAsStream("/$filename")
        .bufferedReader()
        .readText()

    @AfterEach
    fun closeDataSource() {
        dataSource.close()
    }
}
