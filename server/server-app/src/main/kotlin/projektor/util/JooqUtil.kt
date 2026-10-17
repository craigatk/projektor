package projektor.util

import org.jooq.DSLContext
import org.jooq.Field
import org.jooq.impl.TableImpl
import projektor.database.generated.Tables.TEST_RUN
import projektor.server.api.PublicId

fun TableImpl<*>.addPrefixToFields(prefix: String): List<Field<*>> {
    return fields().map { it.`as`("${prefix}${it.name}") }
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
