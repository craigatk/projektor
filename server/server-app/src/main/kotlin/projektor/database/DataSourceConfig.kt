package projektor.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.config.ApplicationConfig
import io.ktor.util.KtorExperimentalAPI
import javax.sql.DataSource
import org.flywaydb.core.Flyway

@KtorExperimentalAPI
object DataSourceConfig {
    fun createDataSource(applicationConfig: ApplicationConfig): HikariDataSource {
        val hikariConfig = HikariConfig()
        hikariConfig.username = applicationConfig.property("ktor.datasource.username").getString()
        hikariConfig.password = applicationConfig.property("ktor.datasource.password").getString()
        hikariConfig.jdbcUrl = applicationConfig.property("ktor.datasource.jdbcUrl").getString()
        hikariConfig.maximumPoolSize = 10

        return HikariDataSource(hikariConfig)
    }

    fun flywayMigrate(dataSource: DataSource) {
        val flyway = Flyway.configure()
                .dataSource(dataSource)
                .load()

        flyway.migrate()
    }
}
