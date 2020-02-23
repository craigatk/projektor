package projektor.server.example

import com.google.gson.Gson
import java.io.File
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import projektor.parser.GroupedResultsXmlLoader
import projektor.parser.ResultsXmlLoader
import projektor.server.api.SaveResultsResponse

val serverBaseUrl = System.getenv("SERVER_URL") ?: "http://localhost:8080"
val uiBaseUrl = System.getenv("SERVER_URL") ?: "http://localhost:1234"

fun loadPassingExample() {
    val resultsXmlLoader = ResultsXmlLoader()

    val testResults = listOf(resultsXmlLoader.passing())

    val saveResultsResponse = sendResultsToServer(testResults)
    println("View run with only passing tests at at $uiBaseUrl${saveResultsResponse.uri}")
}

fun loadAllExamples() {
    val resultsXmlLoader = ResultsXmlLoader()

    val testResults = listOf(
            resultsXmlLoader.passing(),
            resultsXmlLoader.failing(),
            resultsXmlLoader.failingLongFailureMessage(),
            resultsXmlLoader.longOutput(),
            resultsXmlLoader.output(),
            resultsXmlLoader.someIgnored(),
            resultsXmlLoader.someIgnoredSomeFailing()
    )

    val saveResultsResponse = sendResultsToServer(testResults)
    println("View run with all tests at at $uiBaseUrl${saveResultsResponse.uri}")
}

fun loadCypressExamples() {
    val resultsXmlLoader = ResultsXmlLoader()
    val saveResultsResponse = sendResultsToServer(resultsXmlLoader.cypressResults())
    println("View run with Cypress tests at at $uiBaseUrl${saveResultsResponse.uri}")
}

fun loadPassingGroupedExample() {
    val groupedResultsXmlLoader = GroupedResultsXmlLoader()
    val saveResultsResponse = sendGroupedResultsToServer(groupedResultsXmlLoader.passingGroupedResults())
    println("View run with passing grouped tests at at $uiBaseUrl${saveResultsResponse.uri}")
}

fun loadPassingGroupedExampleWithAttachments() {
    val groupedResultsXmlLoader = GroupedResultsXmlLoader()
    val saveResultsResponse = sendGroupedResultsToServer(groupedResultsXmlLoader.passingGroupedResults())
    sendAttachmentToServer(saveResultsResponse.id, "src/main/resources/attachment-1.txt")
    sendAttachmentToServer(saveResultsResponse.id, "src/main/resources/test-run-summary.png")
    println("View run with passing grouped tests and attachments at at $uiBaseUrl${saveResultsResponse.uri}")
}

fun sendResultsToServer(resultXmlList: List<String>): SaveResultsResponse =
        resultXmlList.joinToString("\n").let(::sendResultsToServer)

fun sendResultsToServer(resultsBlob: String): SaveResultsResponse {
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

fun sendAttachmentToServer(publicId: String, attachmentFilePath: String) {
    val attachmentFile = File(attachmentFilePath)
    val client = OkHttpClient()
    val mediaType = "octet/stream".toMediaType()
    val url = "$serverBaseUrl/run/$publicId/attachment/${attachmentFile.name}"
    val requestBody = attachmentFile.readBytes().toRequestBody(mediaType)

    val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

    val response = client.newCall(request).execute()
    println("Response code ${response.code} from uploading attachment $attachmentFilePath")
}

fun sendGroupedResultsToServer(groupedResultsJson: String): SaveResultsResponse {
    val client = OkHttpClient()
    val mediaType = "application/json".toMediaType()
    val url = "$serverBaseUrl/groupedResults"
    val requestBody = groupedResultsJson.toRequestBody(mediaType)

    val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

    val response = client.newCall(request).execute()
    val responseString = response.body?.string()
    return Gson().fromJson(responseString, SaveResultsResponse::class.java)
}
