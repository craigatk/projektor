package projektor.mcp

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import org.apache.commons.lang3.RandomStringUtils
import org.junit.jupiter.api.Test
import projektor.ApplicationTestCase
import projektor.TestSuiteData
import projektor.incomingresults.randomPublicId
import strikt.api.expectThat
import strikt.assertions.contains
import strikt.assertions.isEqualTo

class McpPullRequestFailureContextApplicationTest : ApplicationTestCase() {
    private val initializeRequestBody =
        """
        {
          "jsonrpc": "2.0",
          "id": 1,
          "method": "initialize",
          "params": {
            "protocolVersion": "2025-06-18",
            "capabilities": {},
            "clientInfo": {
              "name": "test-client",
              "version": "1.0"
            }
          }
        }
        """.trimIndent()

    @Test
    fun `initialize response uses spec-compliant camelCase field names with no explicit nulls`() =
        projektorTestApplication {
            val response =
                client.post("/mcp") {
                    headers {
                        append(HttpHeaders.ContentType, "application/json")
                        append(HttpHeaders.Accept, "application/json, text/event-stream")
                    }
                    setBody(initializeRequestBody)
                }

            expectThat(response.status).isEqualTo(HttpStatusCode.OK)

            val bodyText = response.bodyAsText()
            expectThat(bodyText).contains("\"protocolVersion\"")
            expectThat(bodyText).contains("\"serverInfo\"")
            expectThat(bodyText).not { contains("\"protocol_version\"") }
            expectThat(bodyText).not { contains("\"server_info\"") }
            expectThat(bodyText).not { contains(":null") }
        }

    @Test
    fun `tools list response has spec-compliant JSON Schema property types`() =
        projektorTestApplication {
            val listToolsRequestBody =
                """
                {
                  "jsonrpc": "2.0",
                  "id": 1,
                  "method": "tools/list",
                  "params": {}
                }
                """.trimIndent()

            val response =
                client.post("/mcp") {
                    headers {
                        append(HttpHeaders.ContentType, "application/json")
                        append(HttpHeaders.Accept, "application/json, text/event-stream")
                    }
                    setBody(listToolsRequestBody)
                }

            expectThat(response.status).isEqualTo(HttpStatusCode.OK)

            val bodyText = response.bodyAsText()
            expectThat(bodyText).contains("\"type\":\"string\"")
            expectThat(bodyText).contains("\"type\":\"integer\"")
            expectThat(bodyText).contains("\"type\":\"object\"")
            expectThat(bodyText).not { contains("\"is_string\"") }
            expectThat(bodyText).not { contains("coerceToInlineType") }
        }

    private fun callToolRequestBody(
        orgName: String,
        repoName: String,
        pullRequestNumber: Int,
    ) = """
        {
          "jsonrpc": "2.0",
          "id": 1,
          "method": "tools/call",
          "params": {
            "name": "get_pull_request_failing_test_context",
            "arguments": {
              "orgName": "$orgName",
              "repoName": "$repoName",
              "pullRequestNumber": $pullRequestNumber
            }
          }
        }
        """.trimIndent()

    private fun callToolRequestBodyWithUrl(pullRequestUrl: String) =
        """
        {
          "jsonrpc": "2.0",
          "id": 1,
          "method": "tools/call",
          "params": {
            "name": "get_pull_request_failing_test_context",
            "arguments": {
              "pullRequestUrl": "$pullRequestUrl"
            }
          }
        }
        """.trimIndent()

    @Test
    fun `should return failing test context for pull request`() =
        projektorTestApplication {
            val orgName = RandomStringUtils.randomAlphabetic(12)
            val repoName = "repo"
            val pullRequestNumber = 7

            val publicId = randomPublicId()

            val testRunDB =
                testRunDBGenerator.createTestRun(
                    publicId,
                    listOf(
                        TestSuiteData(
                            "testSuite1",
                            listOf(),
                            listOf("failingTestSuite1TestCase1"),
                            listOf(),
                        ),
                    ),
                )
            testRunDBGenerator.addResultsMetadata(testRunDB, true)
            testRunDBGenerator.addGitMetadata(
                testRunDB,
                "$orgName/$repoName",
                false,
                "feature",
                null,
                pullRequestNumber,
                null,
            )

            val response =
                client.post("/mcp") {
                    headers {
                        append(HttpHeaders.ContentType, "application/json")
                        append(HttpHeaders.Accept, "application/json, text/event-stream")
                    }
                    setBody(callToolRequestBody(orgName, repoName, pullRequestNumber))
                }

            expectThat(response.status).isEqualTo(HttpStatusCode.OK)

            val bodyText = response.bodyAsText()
            expectThat(bodyText).contains(publicId.id)
            expectThat(bodyText).contains("failingTestSuite1TestCase1 failure message")
            expectThat(bodyText).contains("\"type\":\"text\"")
            expectThat(bodyText).not { contains("\"type\":\"TEXT\"") }
        }

    @Test
    fun `should report no test run found when pull request is unknown`() =
        projektorTestApplication {
            val orgName = RandomStringUtils.randomAlphabetic(12)

            val response =
                client.post("/mcp") {
                    headers {
                        append(HttpHeaders.ContentType, "application/json")
                        append(HttpHeaders.Accept, "application/json, text/event-stream")
                    }
                    setBody(callToolRequestBody(orgName, "repo", 123))
                }

            expectThat(response.status).isEqualTo(HttpStatusCode.OK)

            val bodyText = response.bodyAsText()
            expectThat(bodyText).contains("No test run found")
        }

    @Test
    fun `should return failing test context when pull request identified by URL`() =
        projektorTestApplication {
            val orgName = RandomStringUtils.randomAlphabetic(12)
            val repoName = "repo"
            val pullRequestNumber = 7

            val publicId = randomPublicId()

            val testRunDB =
                testRunDBGenerator.createTestRun(
                    publicId,
                    listOf(
                        TestSuiteData(
                            "testSuite1",
                            listOf(),
                            listOf("failingTestSuite1TestCase1"),
                            listOf(),
                        ),
                    ),
                )
            testRunDBGenerator.addResultsMetadata(testRunDB, true)
            testRunDBGenerator.addGitMetadata(
                testRunDB,
                "$orgName/$repoName",
                false,
                "feature",
                null,
                pullRequestNumber,
                null,
            )

            val pullRequestUrl = "https://github.com/$orgName/$repoName/pull/$pullRequestNumber"

            val response =
                client.post("/mcp") {
                    headers {
                        append(HttpHeaders.ContentType, "application/json")
                        append(HttpHeaders.Accept, "application/json, text/event-stream")
                    }
                    setBody(callToolRequestBodyWithUrl(pullRequestUrl))
                }

            expectThat(response.status).isEqualTo(HttpStatusCode.OK)

            val bodyText = response.bodyAsText()
            expectThat(bodyText).contains(publicId.id)
            expectThat(bodyText).contains("failingTestSuite1TestCase1 failure message")
        }

    @Test
    fun `should return error for malformed pull request URL`() =
        projektorTestApplication {
            val response =
                client.post("/mcp") {
                    headers {
                        append(HttpHeaders.ContentType, "application/json")
                        append(HttpHeaders.Accept, "application/json, text/event-stream")
                    }
                    setBody(callToolRequestBodyWithUrl("https://example.com/not-a-pull-request"))
                }

            expectThat(response.status).isEqualTo(HttpStatusCode.OK)

            val bodyText = response.bodyAsText()
            expectThat(bodyText).contains("pullRequestUrl must look like")
            expectThat(bodyText).contains("\"isError\":true")
        }

    @Test
    fun `should return error when neither pullRequestUrl nor the individual fields are given`() =
        projektorTestApplication {
            val requestBody =
                """
                {
                  "jsonrpc": "2.0",
                  "id": 1,
                  "method": "tools/call",
                  "params": {
                    "name": "get_pull_request_failing_test_context",
                    "arguments": {}
                  }
                }
                """.trimIndent()

            val response =
                client.post("/mcp") {
                    headers {
                        append(HttpHeaders.ContentType, "application/json")
                        append(HttpHeaders.Accept, "application/json, text/event-stream")
                    }
                    setBody(requestBody)
                }

            expectThat(response.status).isEqualTo(HttpStatusCode.OK)

            val bodyText = response.bodyAsText()
            expectThat(bodyText).contains("Either pullRequestUrl, or orgName, repoName, and pullRequestNumber together, are required")
            expectThat(bodyText).contains("\"isError\":true")
        }
}
