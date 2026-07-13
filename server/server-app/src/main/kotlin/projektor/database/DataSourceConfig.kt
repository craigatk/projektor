package projektor.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import io.ktor.server.config.ApplicationConfig
import io.micrometer.core.instrument.MeterRegistry
import org.flywaydb.core.Flyway
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.conf.MappedSchema
import org.jooq.conf.RenderMapping
import org.jooq.conf.Settings
import org.jooq.impl.DSL
import java.net.URI
import java.net.URLDecoder
import javax.sql.DataSource

data class DataSourceConfig(
    val jdbcUrl: String,
    val username: String,
    val password: String,
    val schema: String,
    val maximumPoolSize: Int,
) {
    companion object {
        fun createDataSourceConfig(applicationConfig: ApplicationConfig): DataSourceConfig {
            val (jdbcUrl, username, password) =
                normalizeConnectionInfo(
                    applicationConfig.property("ktor.datasource.jdbcUrl").getString(),
                    applicationConfig.property("ktor.datasource.username").getString(),
                    applicationConfig.property("ktor.datasource.password").getString(),
                )

            return DataSourceConfig(
                jdbcUrl,
                username,
                password,
                applicationConfig.property("ktor.datasource.schema").getString(),
                applicationConfig.property("ktor.datasource.maximumPoolSize").getString().toInt(),
            )
        }

        // Some hosts (e.g. DigitalOcean's managed Postgres connection string, Heroku's DATABASE_URL)
        // provide the connection info as a `postgres://user:password@host:port/database?sslmode=require`
        // URI rather than the `jdbc:postgresql://` URL this app otherwise expects for DB_URL. Detect
        // and convert that format so either can be set directly as DB_URL without manual reformatting.
        internal fun normalizeConnectionInfo(
            jdbcUrl: String,
            username: String,
            password: String,
        ): Triple<String, String, String> =
            if (jdbcUrl.startsWith("postgres://") || jdbcUrl.startsWith("postgresql://")) {
                val uri = URI(jdbcUrl)

                val userInfoParts = uri.userInfo?.split(":", limit = 2)
                val parsedUsername = userInfoParts?.getOrNull(0)?.let { URLDecoder.decode(it, "UTF-8") } ?: username
                val parsedPassword = userInfoParts?.getOrNull(1)?.let { URLDecoder.decode(it, "UTF-8") } ?: password

                val hostAndPort = if (uri.port != -1) "${uri.host}:${uri.port}" else uri.host
                val query = uri.query?.let { "?$it" } ?: ""

                Triple("jdbc:postgresql://$hostAndPort${uri.path}$query", parsedUsername, parsedPassword)
            } else {
                Triple(jdbcUrl, username, password)
            }

        fun createDataSource(
            dataSourceConfig: DataSourceConfig,
            metricRegistry: MeterRegistry,
        ): HikariDataSource {
            val hikariConfig = HikariConfig()
            hikariConfig.username = dataSourceConfig.username
            hikariConfig.password = dataSourceConfig.password
            hikariConfig.jdbcUrl = dataSourceConfig.jdbcUrl
            hikariConfig.schema = dataSourceConfig.schema
            hikariConfig.maximumPoolSize = dataSourceConfig.maximumPoolSize

            val dataSource = HikariDataSource(hikariConfig)
            dataSource.metricRegistry = metricRegistry

            return dataSource
        }

        fun flywayMigrate(
            dataSource: DataSource,
            dataSourceConfig: DataSourceConfig,
        ) {
            val flyway =
                Flyway.configure()
                    .dataSource(dataSource)
                    .schemas(dataSourceConfig.schema)
                    .locations("classpath:db/migration")
                    .validateMigrationNaming(true)
                    .load()

            flyway.migrate()
        }

        fun createDSLContext(
            dataSource: DataSource,
            dataSourceConfig: DataSourceConfig,
        ): DSLContext {
            val settings =
                Settings()
                    .withRenderMapping(
                        RenderMapping()
                            .withSchemata(
                                MappedSchema().withInput("public")
                                    .withOutput(dataSourceConfig.schema),
                            ),
                    )

            return DSL.using(dataSource, SQLDialect.POSTGRES, settings)
        }
    }
}
