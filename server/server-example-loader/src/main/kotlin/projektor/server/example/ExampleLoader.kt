package projektor.server.example

import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import projektor.parser.ResultsXmlLoader
import projektor.server.api.SaveResultsResponse

val serverBaseUrl = System.getenv("SERVER_URL") ?: "http://localhost:8080"
val uiBaseUrl = System.getenv("SERVER_URL") ?: "http://localhost:1234"

fun loadPassingExample() {
    val resultsXmlLoader = ResultsXmlLoader()

    val testResults = listOf(resultsXmlLoader.passing())

    val saveResultsResponse = sendSaveResultsRequestToServer(testResults)
    println("View run with only passing tests at at $uiBaseUrl${saveResultsResponse.uri}")
}

fun loadAllExamples() {
    val resultsXmlLoader = ResultsXmlLoader()

    val testResults = listOf(
            resultsXmlLoader.passing(),
            resultsXmlLoader.failing(),
            resultsXmlLoader.longOutput(),
            resultsXmlLoader.output(),
            resultsXmlLoader.someIgnored(),
            resultsXmlLoader.someIgnoredSomeFailing()
    )

    val saveResultsResponse = sendSaveResultsRequestToServer(testResults)
    println("View run with all tests at at $uiBaseUrl${saveResultsResponse.uri}")
}

fun loadCypressExamples() {
    val resultsXmlLoader = ResultsXmlLoader()
    val saveResultsResponse = sendSaveResultsRequestToServer(resultsXmlLoader.cypressResults())
    println("View run with Cypress tests at at $uiBaseUrl${saveResultsResponse.uri}")
}

fun sendSaveResultsRequestToServer(resultXmlList: List<String>): SaveResultsResponse =
        resultXmlList.joinToString("\n").let(::sendSaveResultsRequestToServer)

fun sendSaveResultsRequestToServer(resultsBlob: String): SaveResultsResponse {
    val client = OkHttpClient()
    val plainTextMediaType = "text/plain".toMediaType()
    val url = "$serverBaseUrl/results"
    val requestBody = resultsBlob.toRequestBody(plainTextMediaType)

    val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

    val response = client.newCall(request).execute()
    val responseString = response.body?.string()
    return Gson().fromJson(responseString, SaveResultsResponse::class.java)
}
