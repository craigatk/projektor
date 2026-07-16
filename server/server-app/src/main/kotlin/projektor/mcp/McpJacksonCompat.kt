package projektor.mcp

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import com.fasterxml.jackson.databind.introspect.ClassIntrospector
import kotlinx.serialization.json.JsonObject

private const val MCP_TYPES_PACKAGE = "io.modelcontextprotocol.kotlin.sdk.types"

// The app's shared ContentNegotiation is configured for the REST API (SNAKE_CASE, via
// projektor.Application), but that same Jackson instance also ends up serializing the MCP SDK's
// own response types (see ProjektorMcpServer.kt) because ContentNegotiation just uses whichever
// registered converter's serialize() call succeeds first, and Jackson (being reflection-based)
// always succeeds -- silently producing spec-incompatible JSON-RPC output (protocol_version
// instead of protocolVersion, explicit nulls for absent optional fields, etc), even though the
// SDK's own McpJson converter is also registered.
//
// Rather than fight ContentNegotiation's converter selection (attempted and reverted -- reordering
// broke the REST API, and route-scoping ContentNegotiation isn't supported/didn't stay scoped),
// this makes Jackson itself render the SDK's types correctly: every class in [MCP_TYPES_PACKAGE]
// gets camelCase naming and null-omission via a mixin, with a couple of classes overriding specific
// fields the SDK serializes non-camelCase (matching the JSON-RPC spec, e.g. "_meta", "$schema").
@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy::class)
@JsonInclude(JsonInclude.Include.NON_NULL)
private interface McpTypeMixin

@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy::class)
@JsonInclude(JsonInclude.Include.NON_NULL)
private interface WithMetaMixin {
    @get:JsonProperty("_meta")
    val meta: JsonObject?
}

@JsonNaming(PropertyNamingStrategies.LowerCamelCaseStrategy::class)
@JsonInclude(JsonInclude.Include.NON_NULL)
private interface ToolSchemaMixin {
    @get:JsonProperty("\$schema")
    val schema: String?

    @get:JsonProperty("\$defs")
    val defs: JsonObject?
}

private object McpTypesMixInResolver : ClassIntrospector.MixInResolver {
    override fun findMixInClassFor(cls: Class<*>): Class<*>? {
        if (cls.packageName != MCP_TYPES_PACKAGE) return null
        return when (cls.simpleName) {
            "WithMeta" -> WithMetaMixin::class.java
            "ToolSchema" -> ToolSchemaMixin::class.java
            else -> McpTypeMixin::class.java
        }
    }

    override fun copy(): ClassIntrospector.MixInResolver = this
}

fun ObjectMapper.registerMcpTypeCompatibility(): ObjectMapper = setMixInResolver(McpTypesMixInResolver)
