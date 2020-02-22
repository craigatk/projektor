package projektor

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.features.CORS
import io.ktor.features.CachingHeaders
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.http.CacheControl
import io.ktor.http.ContentType
import io.ktor.http.content.CachingOptions
import io.ktor.jackson.jackson
import io.ktor.routing.routing
import io.ktor.util.KtorExperimentalAPI
import org.koin.Logger.SLF4JLogger
import org.koin.ktor.ext.Koin
import org.koin.ktor.ext.inject
import org.slf4j.event.Level
import projektor.asset.AssetStoreConfig
import projektor.asset.AssetStoreService
import projektor.auth.AuthConfig
import projektor.auth.AuthService
import projektor.database.DataSourceConfig
import projektor.incomingresults.GroupedTestResultsService
import projektor.incomingresults.TestResultsProcessingService
import projektor.incomingresults.TestResultsService
import projektor.route.*
import projektor.testcase.TestCaseService
import projektor.testrun.TestRunService
import projektor.testsuite.TestSuiteService

@KtorExperimentalAPI
fun Application.main() {
    val applicationConfig = environment.config

    val authConfig = AuthConfig.createAuthConfig(applicationConfig)

    val dataSourceConfig = DataSourceConfig.createDataSourceConfig(applicationConfig)
    val dataSource = DataSourceConfig.createDataSource(dataSourceConfig)
    DataSourceConfig.flywayMigrate(dataSource, dataSourceConfig)
    val dslContext = DataSourceConfig.createDSLContext(dataSource, dataSourceConfig)

    val assetStoreService = if (AssetStoreConfig.assetStoreEnabled(applicationConfig)) AssetStoreService(AssetStoreConfig.createAssetConfig(applicationConfig)) else null

    val appModule = createAppModule(dataSource, authConfig, dslContext)

    install(CORS) {
        anyHost()
    }
    install(ContentNegotiation) {
        jackson {
            registerModule(JavaTimeModule())
            propertyNamingStrategy = PropertyNamingStrategy.SNAKE_CASE
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        }
    }
    install(CallLogging) {
        level = Level.INFO
    }
    install(Koin) {
        SLF4JLogger()
        modules(appModule)
    }
    install(CachingHeaders) {
        options { outgoingContent ->
            val oneDayInSeconds = 24 * 60 * 60
            when (outgoingContent.contentType?.withoutParameters()) {
                ContentType.Application.Json -> CachingOptions(CacheControl.MaxAge(maxAgeSeconds = oneDayInSeconds))
                ContentType.Application.JavaScript -> CachingOptions(CacheControl.MaxAge(maxAgeSeconds = 7 * oneDayInSeconds))
                else -> null
            }
        }
    }

    val authService: AuthService by inject()
    val testResultsService: TestResultsService by inject()
    val groupedTestResultsService: GroupedTestResultsService by inject()
    val testResultsProcessingService: TestResultsProcessingService by inject()
    val testRunService: TestRunService by inject()
    val testCaseService: TestCaseService by inject()
    val testSuiteService: TestSuiteService by inject()

    routing {
        assetStore(assetStoreService)
        health()
        results(testResultsService, groupedTestResultsService, testResultsProcessingService, authService)
        testCases(testCaseService)
        testSuites(testSuiteService)
        testRuns(testRunService)
        ui()
    }
}
