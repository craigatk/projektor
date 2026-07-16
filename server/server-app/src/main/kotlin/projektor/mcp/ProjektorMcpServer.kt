package projektor.mcp

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.types.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.types.Implementation
import io.modelcontextprotocol.kotlin.sdk.types.ServerCapabilities
import io.modelcontextprotocol.kotlin.sdk.types.TextContent
import io.modelcontextprotocol.kotlin.sdk.types.ToolSchema
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.int
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import projektor.pullrequest.PullRequestFailureContextService

private const val GET_PULL_REQUEST_FAILING_TEST_CONTEXT = "get_pull_request_failing_test_context"

private val objectMapper =
    JsonMapper.builder()
        .addModule(JavaTimeModule())
        .propertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .build()

fun buildProjektorMcpServer(pullRequestFailureContextService: PullRequestFailureContextService): Server {
    val server =
        Server(
            Implementation(name = "projektor", version = "1.0.0"),
            ServerOptions(
                capabilities =
                    ServerCapabilities(
                        tools = ServerCapabilities.Tools(listChanged = false),
                    ),
            ),
        )

    server.addTool(
        name = GET_PULL_REQUEST_FAILING_TEST_CONTEXT,
        description =
            "Fetches the diagnostic context (failure message, stack trace, stdout/stderr) for every " +
                "currently-failing test in the most recent Projektor test run recorded for a GitHub pull " +
                "request, so it can be used to diagnose why those tests are failing.",
        inputSchema =
            ToolSchema(
                properties =
                    buildJsonObject {
                        putJsonObject("orgName") {
                            put("type", "string")
                            put("description", "GitHub organization or user name that owns the repository")
                        }
                        putJsonObject("repoName") {
                            put("type", "string")
                            put("description", "GitHub repository name")
                        }
                        putJsonObject("pullRequestNumber") {
                            put("type", "integer")
                            put("description", "The pull request number")
                        }
                    },
                required = listOf("orgName", "repoName", "pullRequestNumber"),
            ),
    ) { request ->
        val arguments = request.arguments
        val orgName = arguments?.get("orgName")?.jsonPrimitive?.content
        val repoName = arguments?.get("repoName")?.jsonPrimitive?.content
        val pullRequestNumber = arguments?.get("pullRequestNumber")?.jsonPrimitive?.int

        if (orgName == null || repoName == null || pullRequestNumber == null) {
            CallToolResult(
                content = listOf(TextContent("orgName, repoName, and pullRequestNumber are all required")),
                isError = true,
            )
        } else {
            val failureContext =
                pullRequestFailureContextService.fetchFailureContext(orgName, repoName, pullRequestNumber)

            if (failureContext == null) {
                CallToolResult(
                    content =
                        listOf(
                            TextContent(
                                "No test run found for pull request $pullRequestNumber in $orgName/$repoName",
                            ),
                        ),
                )
            } else {
                CallToolResult(content = listOf(TextContent(objectMapper.writeValueAsString(failureContext))))
            }
        }
    }

    return server
}
