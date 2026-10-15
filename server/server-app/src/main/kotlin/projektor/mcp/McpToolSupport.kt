package projektor.mcp

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.types.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.types.TextContent
import io.modelcontextprotocol.kotlin.sdk.types.ToolAnnotations
import io.modelcontextprotocol.kotlin.sdk.types.ToolSchema
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonObjectBuilder
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject

// Tool results embed app data as a JSON string in a TextContent block. That's app data rather than the
// MCP protocol envelope, so it deliberately keeps the REST API's SNAKE_CASE conventions (see McpJacksonCompat.kt).
private val objectMapper =
    JsonMapper.builder()
        .addModule(JavaTimeModule())
        .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .build()

internal fun jsonResult(value: Any): CallToolResult = CallToolResult(content = listOf(TextContent(objectMapper.writeValueAsString(value))))

internal fun textResult(text: String): CallToolResult = CallToolResult(content = listOf(TextContent(text)))

internal fun errorResult(text: String): CallToolResult = CallToolResult(content = listOf(TextContent(text)), isError = true)

/** Thrown from a tool handler when the caller's arguments are invalid; reported back as an isError tool result. */
internal class McpToolArgumentException(message: String) : RuntimeException(message)

internal class ToolArguments(private val arguments: JsonObject?) {
    fun string(name: String): String? =
        (arguments?.get(name) as? JsonPrimitive)
            ?.contentOrNull
            ?.trim()
            ?.takeIf { it.isNotEmpty() }

    fun int(name: String): Int? {
        val value = arguments?.get(name) as? JsonPrimitive ?: return null
        if (value.contentOrNull == null) return null

        return value.intOrNull ?: value.content.trim().toIntOrNull() ?: throw McpToolArgumentException("$name must be an integer")
    }

    fun requiredString(name: String): String = string(name) ?: throw McpToolArgumentException("$name is required")

    fun requiredInt(name: String): Int = int(name) ?: throw McpToolArgumentException("$name is required")
}

/** Every Projektor tool only reads data, so they're all registered with readOnlyHint. */
internal fun Server.addReadOnlyTool(
    name: String,
    description: String,
    required: List<String> = emptyList(),
    properties: JsonObjectBuilder.() -> Unit,
    handler: suspend (ToolArguments) -> CallToolResult,
) {
    addTool(
        name = name,
        description = description,
        inputSchema =
            ToolSchema(
                properties = buildJsonObject(properties),
                required = required.takeIf { it.isNotEmpty() },
            ),
        toolAnnotations = ToolAnnotations(readOnlyHint = true, openWorldHint = false),
    ) { request ->
        try {
            handler(ToolArguments(request.arguments))
        } catch (e: McpToolArgumentException) {
            errorResult(e.message ?: "Invalid arguments")
        }
    }
}

internal fun JsonObjectBuilder.stringProperty(
    name: String,
    description: String,
) = putJsonObject(name) {
    put("type", "string")
    put("description", description)
}

internal fun JsonObjectBuilder.integerProperty(
    name: String,
    description: String,
) = putJsonObject(name) {
    put("type", "integer")
    put("description", description)
}

internal fun JsonObjectBuilder.enumProperty(
    name: String,
    description: String,
    values: List<String>,
) = putJsonObject(name) {
    put("type", "string")
    put("description", description)
    putJsonArray("enum") { values.forEach { add(it) } }
}

internal fun JsonObjectBuilder.repositoryProperties() {
    stringProperty("orgName", "GitHub organization or user name that owns the repository")
    stringProperty("repoName", "GitHub repository name")
}

internal fun JsonObjectBuilder.pullRequestProperties() {
    stringProperty(
        "pullRequestUrl",
        "The GitHub pull request's URL, e.g. https://github.com/{org}/{repo}/pull/{number}. " +
            "An alternative to passing orgName, repoName, and pullRequestNumber separately.",
    )
    repositoryProperties()
    integerProperty("pullRequestNumber", "The pull request number")
}

internal data class RepositoryLocation(val orgName: String, val repoName: String) {
    // GIT_METADATA.REPO_NAME stores the org-qualified slug, matching ApiRoutes.kt's `fullRepoName`
    val fullRepoName: String
        get() = "$orgName/$repoName"
}

internal data class PullRequestLocation(val orgName: String, val repoName: String, val pullRequestNumber: Int) {
    override fun toString() = "pull request $pullRequestNumber in $orgName/$repoName"
}

// Callers frequently have just a GitHub PR link (e.g. pasted from a user message) rather than
// its three parts pre-split, so pullRequestUrl is accepted as an alternative to orgName/repoName/
// pullRequestNumber. Trailing path segments/query/fragment are allowed so e.g. a "/files" or
// "?diff=split" suffixed URL still parses.
private val githubPullRequestUrlPattern = Regex("""^https?://github\.com/([^/]+)/([^/]+)/pull/(\d+)(?:[/?#].*)?$""")

private fun parseGitHubPullRequestUrl(url: String): PullRequestLocation? =
    githubPullRequestUrlPattern.matchEntire(url.trim())?.let { match ->
        val (orgName, repoName, pullRequestNumber) = match.destructured
        PullRequestLocation(orgName, repoName, pullRequestNumber.toInt())
    }

internal fun ToolArguments.repositoryLocationOrNull(): RepositoryLocation? {
    val orgName = string("orgName")
    val repoName = string("repoName")

    return if (orgName != null && repoName != null) RepositoryLocation(orgName, repoName) else null
}

internal fun ToolArguments.requiredRepositoryLocation(): RepositoryLocation =
    repositoryLocationOrNull() ?: throw McpToolArgumentException("orgName and repoName are required")

/** The pull request the caller identified, or null if they didn't pass any pull request arguments at all. */
internal fun ToolArguments.pullRequestLocationOrNull(): PullRequestLocation? {
    val pullRequestUrl = string("pullRequestUrl")
    if (pullRequestUrl != null) {
        return parseGitHubPullRequestUrl(pullRequestUrl)
            ?: throw McpToolArgumentException("pullRequestUrl must look like https://github.com/{org}/{repo}/pull/{number}")
    }

    val pullRequestNumber = int("pullRequestNumber") ?: return null
    val repository =
        repositoryLocationOrNull() ?: throw McpToolArgumentException(
            "orgName and repoName are required with pullRequestNumber",
        )

    return PullRequestLocation(repository.orgName, repository.repoName, pullRequestNumber)
}

internal fun ToolArguments.requiredPullRequestLocation(): PullRequestLocation =
    pullRequestLocationOrNull()
        ?: throw McpToolArgumentException("Either pullRequestUrl, or orgName, repoName, and pullRequestNumber together, are required")
