package projektor.util

import org.jooq.DSLContext
import org.jooq.Field
import org.jooq.impl.TableImpl
import projektor.database.generated.Tables.TEST_RUN
import projektor.server.api.PublicId

fun TableImpl<*>.addPrefixToFields(prefix: String): List<Field<*>> {
    return fields().map { it.`as`("${prefix}${it.name}") }
}

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
