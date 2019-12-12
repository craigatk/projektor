package projektor

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.PropertyNamingStrategy
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.application.*
import io.ktor.features.CORS
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.jackson.jackson
import io.ktor.routing.*
import io.ktor.util.KtorExperimentalAPI
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.koin.Logger.SLF4JLogger
import org.koin.ktor.ext.Koin
import org.koin.ktor.ext.inject
import org.slf4j.event.Level
import projektor.database.DataSourceConfig
import projektor.incomingresults.TestResultsService
import projektor.route.*
import projektor.testcase.TestCaseService
import projektor.testrun.TestRunService
import projektor.testsuite.TestSuiteService

@KtorExperimentalAPI
fun Application.main() {
    val dataSource = DataSourceConfig.createDataSource(environment.config)
    DataSourceConfig.flywayMigrate(dataSource)
    val dslContext = DSL.using(dataSource, SQLDialect.POSTGRES)
    val appModule = createAppModule(dataSource, dslContext)

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

    val testResultsService: TestResultsService by inject()
    val testRunService: TestRunService by inject()
    val testCaseService: TestCaseService by inject()
    val testSuiteService: TestSuiteService by inject()

    routing {
        results(testResultsService)
        testCases(testCaseService)
        testSuites(testSuiteService)
        testRuns(testRunService)
        ui()
    }
}
