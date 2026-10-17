package projektor

import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.ExtensionContext
import java.lang.annotation.Inherited
import java.sql.Connection
import java.sql.DriverManager

/**
 * Keeps tests that run test run or attachment cleanup from deleting other tests' data.
 *
 * Cleanup deletes every old enough test run (or attachment) in the database, and the test database is shared by test
 * classes running in parallel JVMs (see maxParallelForks), so a cleanup in one test class can delete a test run another
 * test class is in the middle of using. JUnit's own resource locks only work within one JVM, so this uses a Postgres
 * advisory lock instead:
 *
 * - [Mode.RUNS_CLEANUP] tests hold the lock exclusively, so they never run alongside each other or the tests below.
 * - [Mode.CREATES_CLEANUP_ELIGIBLE_TEST_RUNS] tests (ones that save test runs created more than 14 days ago, the shortest
 *   cleanup age the tests use) hold it shared, so they can still run in parallel with each other.
 *
 * The lock is held for the whole test, including its setup and teardown.
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
@ExtendWith(DatabaseCleanupLockExtension::class)
annotation class DatabaseCleanupLock(val mode: Mode) {
    enum class Mode {
        RUNS_CLEANUP,
        CREATES_CLEANUP_ELIGIBLE_TEST_RUNS,
    }
}

class DatabaseCleanupLockExtension : BeforeEachCallback, AfterEachCallback {
    override fun beforeEach(context: ExtensionContext) {
        val mode = findMode(context.requiredTestClass)
        val lockFunction = if (mode == DatabaseCleanupLock.Mode.RUNS_CLEANUP) "pg_advisory_lock" else "pg_advisory_lock_shared"

        // A dedicated connection, since an advisory lock is held by the database session that took it
        val connection =
            DriverManager.getConnection(
                System.getenv("DB_URL") ?: "jdbc:postgresql://localhost:5433/projektordb",
                System.getenv("DB_USERNAME") ?: "testuser",
                System.getenv("DB_PASSWORD") ?: "testpass",
            )
        connection.prepareStatement("select $lockFunction(?, ?)").use { statement ->
            statement.setInt(1, LOCK_CLASS_KEY)
            statement.setInt(2, LOCK_OBJECT_KEY)
            statement.executeQuery().close()
        }

        context.getStore(NAMESPACE).put(CONNECTION_KEY, connection)
    }

    // Closing the connection ends the session, which releases the lock
    override fun afterEach(context: ExtensionContext) {
        context.getStore(NAMESPACE).remove(CONNECTION_KEY, Connection::class.java)?.close()
    }

    private fun findMode(testClass: Class<*>): DatabaseCleanupLock.Mode =
        generateSequence(testClass) { it.superclass }
            .firstNotNullOf { it.getAnnotation(DatabaseCleanupLock::class.java) }
            .mode

    companion object {
        private val NAMESPACE = ExtensionContext.Namespace.create(DatabaseCleanupLockExtension::class.java)
        private const val CONNECTION_KEY = "connection"

        // The two-key form of advisory lock, which is a separate key space from the single-key locks the server takes
        private const val LOCK_CLASS_KEY = 7_355_608
        private const val LOCK_OBJECT_KEY = 1
    }
}
