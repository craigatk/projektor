package projektor.util

import org.jooq.DSLContext
import projektor.database.generated.Tables.TEST_RUN
import projektor.server.api.PublicId
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

// Timestamp columns are stored without a time zone. Interprets them in the JVM's default time zone,
// the same as JDBC's Timestamp.toInstant() (and the SimpleFlatMapper mapping this replaced).
fun LocalDateTime.toInstantInDefaultZone(): Instant = atZone(ZoneId.systemDefault()).toInstant()

/**
 * Takes a Postgres advisory lock on the key until the current transaction ends, serializing transactions that
 * change the same data (e.g. appends to the same test run) without needing a row to lock.
 */
fun DSLContext.lockUntilTransactionEnds(key: String) {
    fetch("select pg_advisory_xact_lock(hashtext(?))", key)
}

// Resolves the internal test run row ID so it doesn't need to leak out through the repository interfaces.
// Throws NoDataFoundException if no test run exists with this public ID.
fun fetchTestRunId(
    dslContext: DSLContext,
    publicId: PublicId,
): Long =
    dslContext
        .select(TEST_RUN.ID)
        .from(TEST_RUN)
        .where(TEST_RUN.PUBLIC_ID.eq(publicId.id))
        .fetchSingle(TEST_RUN.ID)!!
