package projektor

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.test.KoinTest
import projektor.database.DataSourceConfig
import projektor.database.generated.tables.daos.*

open class DatabaseRepositoryTestCase : KoinTest {
    lateinit var dataSource: HikariDataSource
    lateinit var dslContext: DSLContext
    lateinit var testRunDao: TestRunDao
    lateinit var testSuiteDao: TestSuiteDao
    lateinit var testCaseDao: TestCaseDao
    lateinit var testFailureDao: TestFailureDao
    lateinit var testRunDBGenerator: TestRunDBGenerator
    lateinit var resultsProcessingDao: ResultsProcessingDao

    @BeforeTest
    fun setup() {
        val hikariConfig = HikariConfig()

        hikariConfig.username = System.getenv("DB_USERNAME") ?: "testuser"
        hikariConfig.password = System.getenv("DB_PASSWORD") ?: "testpass"
        hikariConfig.jdbcUrl = System.getenv("DB_URL") ?: "jdbc:postgresql://localhost:5433/projektordb"
        hikariConfig.maximumPoolSize = 3

        dataSource = HikariDataSource(hikariConfig)
        DataSourceConfig.flywayMigrate(dataSource)

        dslContext = DSL.using(dataSource, SQLDialect.POSTGRES)

        startKoin {
            modules(createAppModule(dataSource, dslContext))
        }

        testRunDao = TestRunDao(dslContext.configuration())
        testSuiteDao = TestSuiteDao(dslContext.configuration())
        testCaseDao = TestCaseDao(dslContext.configuration())
        testFailureDao = TestFailureDao(dslContext.configuration())
        resultsProcessingDao = ResultsProcessingDao(dslContext.configuration())

        testRunDBGenerator = TestRunDBGenerator(testRunDao, testSuiteDao, testCaseDao, testFailureDao)
    }

    @AfterTest
    fun closeDataSource() {
        stopKoin()

        dataSource.close()
    }
}
