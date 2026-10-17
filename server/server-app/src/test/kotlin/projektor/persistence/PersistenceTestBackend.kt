package projektor.persistence

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.koin.core.Koin
import org.koin.dsl.koinApplication
import projektor.database.DataSourceConfig
import projektor.objectstore.ObjectStoreClient
import projektor.objectstore.ObjectStoreConfig
import projektor.objectstore.json.JsonObjectStore
import projektor.persistence.objectstore.createObjectStorePersistenceModule
import java.io.Closeable
import java.util.UUID

/**
 * A persistence backend for tests that run against every backend. Provides the backend's repositories
 * through [koin] without starting the global Koin instance.
 */
interface PersistenceTestBackend : Closeable {
    val name: String
    val koin: Koin
}

/**
 * The real Postgres test database, shared across tests (see the database-persistence skill),
 * with a connection pool per test.
 */
class DatabaseTestBackend : PersistenceTestBackend {
    override val name = "database"

    private val dataSource: HikariDataSource

    override val koin: Koin

    init {
        val dataSourceConfig =
            DataSourceConfig(
                System.getenv("DB_URL") ?: "jdbc:postgresql://localhost:5433/projektordb",
                System.getenv("DB_USERNAME") ?: "testuser",
                System.getenv("DB_PASSWORD") ?: "testpass",
                "public",
                2,
            )

        val hikariConfig = HikariConfig()
        hikariConfig.username = dataSourceConfig.username
        hikariConfig.password = dataSourceConfig.password
        hikariConfig.jdbcUrl = dataSourceConfig.jdbcUrl
        hikariConfig.schema = dataSourceConfig.schema
        hikariConfig.maximumPoolSize = dataSourceConfig.maximumPoolSize

        dataSource = HikariDataSource(hikariConfig)
        DataSourceConfig.flywayMigrate(dataSource, dataSourceConfig)

        koin = koinApplication { modules(createDatabasePersistenceModule(dataSource, DSL.using(dataSource, SQLDialect.POSTGRES))) }.koin
    }

    override fun close() {
        dataSource.close()
    }
}

/**
 * The local docker-compose object store, with a fresh key prefix per test so each test starts empty.
 */
class ObjectStoreTestBackend : PersistenceTestBackend {
    override val name = "object-store"

    override val koin: Koin

    init {
        val client = ObjectStoreClient(ObjectStoreConfig("http://localhost:9000", "minio_access_key", "minio_secret_key"))
        client.createBucketIfNotExists(BUCKET_NAME)

        koin =
            koinApplication {
                modules(createObjectStorePersistenceModule(JsonObjectStore(client, BUCKET_NAME, "repository-test-${UUID.randomUUID()}")))
            }.koin
    }

    override fun close() {}

    companion object {
        private const val BUCKET_NAME = "persistencetesting"
    }
}
