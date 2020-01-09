package projektor.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.config.ApplicationConfig
import io.ktor.util.KtorExperimentalAPI
import javax.sql.DataSource
import org.flywaydb.core.Flyway
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.conf.MappedSchema
import org.jooq.conf.RenderMapping
import org.jooq.conf.Settings
import org.jooq.impl.DSL

@KtorExperimentalAPI
data class DataSourceConfig(val jdbcUrl: String, val username: String, val password: String, val schema: String) {
    companion object {
        fun createDataSourceConfig(applicationConfig: ApplicationConfig) = DataSourceConfig(
                applicationConfig.property("ktor.datasource.jdbcUrl").getString(),
                applicationConfig.property("ktor.datasource.username").getString(),
                applicationConfig.property("ktor.datasource.password").getString(),
                applicationConfig.property("ktor.datasource.schema").getString()
        )

        fun createDataSource(dataSourceConfig: DataSourceConfig): HikariDataSource {
            val hikariConfig = HikariConfig()
            hikariConfig.username = dataSourceConfig.username
            hikariConfig.password = dataSourceConfig.password
            hikariConfig.jdbcUrl = dataSourceConfig.jdbcUrl
            hikariConfig.schema = dataSourceConfig.schema
            hikariConfig.maximumPoolSize = 10

            return HikariDataSource(hikariConfig)
        }

        fun flywayMigrate(dataSource: DataSource) {
            val flyway = Flyway.configure()
                    .dataSource(dataSource)
                    .load()

            flyway.migrate()
        }

        fun createDSLContext(dataSource: DataSource, dataSourceConfig: DataSourceConfig): DSLContext {
            val settings = Settings()
                    .withRenderMapping(RenderMapping()
                            .withSchemata(
                                    MappedSchema().withInput("public")
                                            .withOutput(dataSourceConfig.schema)
                            )
                    )

            return DSL.using(dataSource, SQLDialect.POSTGRES, settings)
        }
    }
}
