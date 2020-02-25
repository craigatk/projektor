package projektor

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.config.ApplicationConfig
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
import kotlinx.coroutines.ObsoleteCoroutinesApi
import org.koin.Logger.SLF4JLogger
import org.koin.ktor.ext.Koin
import org.koin.ktor.ext.inject
import org.slf4j.event.Level
import projektor.attachment.AttachmentRepository
import projektor.attachment.AttachmentService
import projektor.attachment.AttachmentStoreConfig
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

@ObsoleteCoroutinesApi
@KtorExperimentalAPI
fun Application.main() {
    val applicationConfig = environment.config

    val authConfig = AuthConfig.createAuthConfig(applicationConfig)

    val dataSourceConfig = DataSourceConfig.createDataSourceConfig(applicationConfig)
    val dataSource = DataSourceConfig.createDataSource(dataSourceConfig)
    DataSourceConfig.flywayMigrate(dataSource, dataSourceConfig)
    val dslContext = DataSourceConfig.createDSLContext(dataSource, dataSourceConfig)

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

    val attachmentRepository: AttachmentRepository by inject()
    val attachmentService = conditionallyCreateAttachmentService(applicationConfig, attachmentRepository)

    routing {
        attachments(attachmentService, authService)
        health()
        results(testResultsService, groupedTestResultsService, testResultsProcessingService, authService)
        testCases(testCaseService)
        testSuites(testSuiteService)
        testRuns(testRunService)
        ui()
    }
}

@KtorExperimentalAPI
private fun conditionallyCreateAttachmentService(applicationConfig: ApplicationConfig, attachmentRepository: AttachmentRepository): AttachmentService? =
    if (AttachmentStoreConfig.attachmentStoreEnabled(applicationConfig))
        AttachmentService(AttachmentStoreConfig.createAttachmentStoreConfig(applicationConfig), attachmentRepository)
    else
        null
