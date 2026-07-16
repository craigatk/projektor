package projektor.route

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get

data class OAuthDiscoveryError(val error: String, val error_description: String)

private val notSupported =
    OAuthDiscoveryError(
        error = "invalid_request",
        error_description = "OAuth is not supported by this server",
    )

// The MCP server at /mcp does not require authentication. Some MCP clients probe these
// well-known endpoints before connecting to check whether OAuth is available; a bare 404
// with no body is spec-legal but has been observed to crash such clients when they attempt
// to JSON-parse the empty response body. Returning a small JSON body alongside the 404
// keeps discovery spec-compliant (still "not found") while avoiding that parse failure.
fun Route.mcpOAuthDiscovery() {
    get("/.well-known/oauth-protected-resource") {
        call.respond(HttpStatusCode.NotFound, notSupported)
    }
    get("/.well-known/oauth-protected-resource/mcp") {
        call.respond(HttpStatusCode.NotFound, notSupported)
    }
    get("/.well-known/oauth-authorization-server") {
        call.respond(HttpStatusCode.NotFound, notSupported)
    }
}
