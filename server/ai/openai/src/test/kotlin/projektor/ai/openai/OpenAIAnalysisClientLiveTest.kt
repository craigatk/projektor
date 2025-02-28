package projektor.ai.openai

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import strikt.api.expectThat
import strikt.assertions.isNotNull

@Disabled
class OpenAIAnalysisClientLiveTest {
    private val apiKey = ""

    @Test
    fun `should get test failure analysis`() {
        val client = OpenAIAnalysisClient(apiKey)

        val testOutput = loadTextFromFile("postgres_test_failure_output.txt")

        val chatResult = runBlocking { client.analyzeTestFailure(testOutput) }

        expectThat(chatResult).isNotNull()

        println(chatResult)
    }

    private fun loadTextFromFile(filename: String) =
        javaClass
            .getResourceAsStream("/$filename")
            .bufferedReader()
            .readText()
}
