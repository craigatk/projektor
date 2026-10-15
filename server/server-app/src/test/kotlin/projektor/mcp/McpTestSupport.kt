package projektor.mcp

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.client.HttpClient
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import strikt.api.expectThat
import strikt.assertions.isEqualTo

private val mcpObjectMapper = ObjectMapper()

class McpToolCallResponse(val bodyText: String) {
    private val result: JsonNode = mcpObjectMapper.readTree(bodyText).path("result")

    val isError: Boolean
        get() = result.path("isError").asBoolean(false)

    /** The tool result's (single) text content block. */
    val text: String
        get() = result.path("content").path(0).path("text").asText()

    /** The text content block parsed as JSON, for tools that return app data. */
    val json: JsonNode
        get() = mcpObjectMapper.readTree(text)
}

suspend fun HttpClient.postMcp(requestBody: String): String {
    val response =
        post("/mcp") {
            headers {
                append(HttpHeaders.ContentType, "application/json")
                append(HttpHeaders.Accept, "application/json, text/event-stream")
            }
            setBody(requestBody)
        }

    expectThat(response.status).isEqualTo(HttpStatusCode.OK)

    return response.bodyAsText()
}

suspend fun HttpClient.callMcpTool(
    name: String,
    arguments: Map<String, Any?>,
): McpToolCallResponse {
    val requestBody =
        mcpObjectMapper.writeValueAsString(
            mapOf(
                "jsonrpc" to "2.0",
                "id" to 1,
                "method" to "tools/call",
                "params" to mapOf("name" to name, "arguments" to arguments),
            ),
        )

    return McpToolCallResponse(postMcp(requestBody))
}

/** A minimal Jacoco report with one package; each file maps to (missed lines, covered lines). */
fun jacocoReport(
    groupName: String,
    packageName: String,
    files: Map<String, Pair<List<Int>, List<Int>>>,
): String {
    fun counters(
        missed: Int,
        covered: Int,
    ) = listOf(
        """<counter type="INSTRUCTION" missed="$missed" covered="$covered"/>""",
        """<counter type="LINE" missed="$missed" covered="$covered"/>""",
    )

    val sourceFiles =
        files.flatMap { (fileName, lines) ->
            val (missedLines, coveredLines) = lines

            listOf("""<sourcefile name="$fileName">""") +
                missedLines.map { """<line nr="$it" mi="1" ci="0" mb="0" cb="0"/>""" } +
                coveredLines.map { """<line nr="$it" mi="0" ci="1" mb="0" cb="0"/>""" } +
                counters(missedLines.size, coveredLines.size) +
                "</sourcefile>"
        }

    val totalCounters = counters(files.values.sumOf { it.first.size }, files.values.sumOf { it.second.size })

    return (
        listOf(
            """<?xml version="1.0" encoding="UTF-8" standalone="yes"?><!DOCTYPE report PUBLIC "-//JACOCO//DTD Report 1.1//EN" "report.dtd">""",
            """<report name="$groupName">""",
            """<sessioninfo id="test" start="1597920506557" dump="1597920520403"/>""",
            """<package name="$packageName">""",
        ) + sourceFiles + totalCounters + "</package>" + totalCounters + "</report>"
    ).joinToString("\n")
}
