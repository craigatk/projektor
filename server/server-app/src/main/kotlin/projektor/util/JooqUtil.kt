package projektor.util

import org.jooq.Field

fun addPrefixToFields(prefix: String, fields: List<Field<*>>): List<Field<*>> {
    return fields.map { it.`as`("${prefix}${it.name}") }
}
