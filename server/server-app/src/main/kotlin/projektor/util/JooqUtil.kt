package projektor.util

import org.jooq.Field
import org.jooq.impl.TableImpl

fun TableImpl<*>.addPrefixToFields(prefix: String): List<Field<*>> {
    return fields().map { it.`as`("${prefix}${it.name}") }
}
