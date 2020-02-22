package projektor

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.zaxxer.hikari.HikariDataSource
import io.ktor.application.Application
import io.ktor.config.MapApplicationConfig
import io.ktor.util.KtorExperimentalAPI
import kotlin.test.AfterTest
import org.jooq.DSLContext
import org.koin.ktor.ext.get
import projektor.asset.AssetStoreConfig
import projektor.database.generated.tables.daos.*
import projektor.objectstore.ObjectStoreClient
import projektor.objectstore.ObjectStoreConfig
import projektor.parser.ResultsXmlLoader

@KtorExperimentalAPI
open class ApplicationTestCase {
    val resultsXmlLoader = ResultsXmlLoader()

    val objectMapper: ObjectMapper = ObjectMapper()
            .registerKotlinModule()
            .registerModule(JavaTimeModule())
            .setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    lateinit var dataSource: HikariDataSource
    lateinit var dslContext: DSLContext
    lateinit var testRunDao: TestRunDao
    lateinit var testSuiteGroupDao: TestSuiteGroupDao
    lateinit var testSuiteDao: TestSuiteDao
    lateinit var testCaseDao: TestCaseDao
    lateinit var testFailureDao: TestFailureDao
    lateinit var testRunDBGenerator: TestRunDBGenerator
    lateinit var application: Application

    protected var databaseSchema: String = "public"

    protected var publishToken: String? = null

    protected var assetStoreEnabled: Boolean? = null
    protected val assetStoreConfig = AssetStoreConfig(
            "http://localhost:9000",
            "addassettestbucket",
            true,
            "minio_access_key",
            "minio_secret_key"
    )

    protected val objectStoreClient = ObjectStoreClient(ObjectStoreConfig(
            assetStoreConfig.url,
            assetStoreConfig.accessKey,
            assetStoreConfig.secretKey
    ))

    fun createTestApplication(application: Application) {
        val schema = databaseSchema

        (application.environment.config as MapApplicationConfig).apply {
            // Set here the properties
            put("ktor.datasource.username", System.getenv("DB_USERNAME") ?: "testuser")
            put("ktor.datasource.password", System.getenv("DB_PASSWORD") ?: "testpass")
            put("ktor.datasource.jdbcUrl", System.getenv("DB_URL") ?: "jdbc:postgresql://localhost:5433/projektordb")
            put("ktor.datasource.schema", schema)

            publishToken?.let { put("ktor.auth.publishToken", it) }

            assetStoreEnabled?.let {
                put("ktor.assetStore.enabled", it.toString())
                put("ktor.assetStore.url", assetStoreConfig.url)
                put("ktor.assetStore.bucketName", assetStoreConfig.bucketName)
                put("ktor.assetStore.autoCreateBucket", "true")
                put("ktor.assetStore.accessKey", assetStoreConfig.accessKey)
                put("ktor.assetStore.secretKey", assetStoreConfig.secretKey)
            }
        }

        application.main()

        dataSource = application.get()
        dslContext = application.get()
        testRunDao = TestRunDao(dslContext.configuration())
        testSuiteGroupDao = TestSuiteGroupDao(dslContext.configuration())
        testSuiteDao = TestSuiteDao(dslContext.configuration())
        testCaseDao = TestCaseDao(dslContext.configuration())
        testFailureDao = TestFailureDao(dslContext.configuration())
        testRunDBGenerator = TestRunDBGenerator(testRunDao, testSuiteGroupDao, testSuiteDao, testCaseDao, testFailureDao)

        this.application = application
    }

    @AfterTest
    fun closeDataSource() {
        dataSource.close()
    }
}
