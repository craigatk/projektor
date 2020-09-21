package projektor.server.example

import com.google.gson.Gson
import java.io.File
import java.math.BigDecimal
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import projektor.parser.GroupedResultsXmlLoader
import projektor.parser.ResultsXmlLoader
import projektor.parser.grouped.model.GitMetadata
import projektor.parser.grouped.model.ResultsMetadata
import projektor.server.api.results.SaveResultsResponse
import projektor.server.example.coverage.JacocoXmlLoader
import projektor.server.example.coverage.JestXmlLoader

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
            resultsXmlLoader.reallyLongOutput(),
            resultsXmlLoader.reallyLongOutput5000(),
            resultsXmlLoader.reallyLongOutput10000(),
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
    val saveResultsResponse = sendGroupedResultsToServer(groupedResultsXmlLoader.passingGroupedResults(null))
    println("View run with passing grouped tests at at $uiBaseUrl${saveResultsResponse.uri}")
}

fun loadPassingGroupedExampleWithAttachments() {
    val groupedResultsXmlLoader = GroupedResultsXmlLoader()
    val saveResultsResponse = sendGroupedResultsToServer(groupedResultsXmlLoader.passingGroupedResults(null))
    sendAttachmentToServer(saveResultsResponse.id, "src/main/resources/attachment-1.txt")
    sendAttachmentToServer(saveResultsResponse.id, "src/main/resources/test-run-summary.png")
    println("View run with passing grouped tests and attachments at $uiBaseUrl${saveResultsResponse.uri}")
}

fun loadGroupedExampleWithWallClock() {
    val groupedResultsXmlLoader = GroupedResultsXmlLoader()
    val saveResultsResponse = sendGroupedResultsToServer(groupedResultsXmlLoader.passingGroupedResults(null, BigDecimal("12.251")))

    println("View run with passing grouped tests and wall clock duration at $uiBaseUrl${saveResultsResponse.uri}")
}

fun loadInvalidExample() {
    val resultsResponse = sendResultsToServer(ResultsXmlLoader().invalid())
    println("View run with invalid results at at $uiBaseUrl${resultsResponse.uri}")
}

fun loadSingleCoverageExample() {
    val resultsResponse = sendResultsToServer(ResultsXmlLoader().passing())
    sendCoverageToServer(resultsResponse.id, JacocoXmlLoader().serverApp())
    println("View run with single coverage results at at $uiBaseUrl${resultsResponse.uri}")
}

fun loadMultipleCoverageExample() {
    val resultsResponse = sendResultsToServer(ResultsXmlLoader().passing())
    sendCoverageToServer(resultsResponse.id, JacocoXmlLoader().serverApp())
    sendCoverageToServer(resultsResponse.id, JacocoXmlLoader().junitResultsParser())
    sendCoverageToServer(resultsResponse.id, JacocoXmlLoader().jacocoXmlParser())
    println("View run with multiple coverage results at at $uiBaseUrl${resultsResponse.uri}")
}

fun loadMultipleCoverageWithPreviousRunExample() {
    val repoName = "projektor/projektor"
    val branchName = "main"
    val gitMetadata = GitMetadata()
    gitMetadata.repoName = repoName
    gitMetadata.branchName = branchName
    gitMetadata.isMainBranch = true
    val resultsMetadata = ResultsMetadata()
    resultsMetadata.git = gitMetadata

    val previousResultsResponse = sendGroupedResultsToServer(GroupedResultsXmlLoader().passingGroupedResults(metadata = resultsMetadata))
    sendCoverageToServer(previousResultsResponse.id, JacocoXmlLoader().serverAppReduced())
    sendCoverageToServer(previousResultsResponse.id, JacocoXmlLoader().junitResultsParser())
    sendCoverageToServer(previousResultsResponse.id, JacocoXmlLoader().jacocoXmlParser())

    val currentResultsResponse = sendGroupedResultsToServer(GroupedResultsXmlLoader().passingGroupedResults(metadata = resultsMetadata))
    sendCoverageToServer(currentResultsResponse.id, JacocoXmlLoader().serverApp())
    sendCoverageToServer(currentResultsResponse.id, JacocoXmlLoader().junitResultsParserReduced())
    sendCoverageToServer(currentResultsResponse.id, JacocoXmlLoader().jacocoXmlParserReduced())

    println("View run with multiple coverage results and previous results at $uiBaseUrl${currentResultsResponse.uri}")
}

fun loadCoverageWithProjectName() {
    val repoName = "projektor/projektor"
    val branchName = "main"
    val projectName = "server"
    val gitMetadata = GitMetadata()
    gitMetadata.repoName = repoName
    gitMetadata.branchName = branchName
    gitMetadata.isMainBranch = true
    gitMetadata.projectName = projectName
    val resultsMetadata = ResultsMetadata()
    resultsMetadata.git = gitMetadata

    val currentResultsResponse = sendGroupedResultsToServer(GroupedResultsXmlLoader().passingGroupedResults(metadata = resultsMetadata))
    sendCoverageToServer(currentResultsResponse.id, JacocoXmlLoader().serverApp())

    println("View run coverage and project name at $uiBaseUrl${currentResultsResponse.uri}")
}

fun loadResultsWithGitButWithoutCoverage() {
    val repoName = "no-coverage/projektor"
    val branchName = "main"
    val gitMetadata = GitMetadata()
    gitMetadata.repoName = repoName
    gitMetadata.branchName = branchName
    gitMetadata.isMainBranch = true
    val resultsMetadata = ResultsMetadata()
    resultsMetadata.git = gitMetadata

    val resultsResponse = sendGroupedResultsToServer(GroupedResultsXmlLoader().passingGroupedResults(metadata = resultsMetadata))

    println("View run with Git metadata and no test coverage at $uiBaseUrl${resultsResponse.uri}")
}

fun loadJestWithCoverage() {
    val resultsResponse = sendResultsToServer(ResultsXmlLoader().jestUi())
    sendCoverageToServer(resultsResponse.id, JestXmlLoader().ui())

    println("View run with Jest results and coverage at $uiBaseUrl${resultsResponse.uri}")
}

fun repositoryCoverageTimeline() {
    val repoName = "cov-org/cov-repo"
    val branchName = "main"
    val gitMetadata = GitMetadata()
    gitMetadata.repoName = repoName
    gitMetadata.branchName = branchName
    gitMetadata.isMainBranch = true
    val resultsMetadata = ResultsMetadata()
    resultsMetadata.git = gitMetadata

    sendCoverageToServer(sendGroupedResultsToServer(GroupedResultsXmlLoader().passingGroupedResults(metadata = resultsMetadata)).id, JacocoXmlLoader().jacocoXmlParser())
    sendCoverageToServer(sendGroupedResultsToServer(GroupedResultsXmlLoader().passingGroupedResults(metadata = resultsMetadata)).id, JacocoXmlLoader().jacocoXmlParserReduced())
    sendCoverageToServer(sendGroupedResultsToServer(GroupedResultsXmlLoader().passingGroupedResults(metadata = resultsMetadata)).id, JacocoXmlLoader().serverAppReduced())
    sendCoverageToServer(sendGroupedResultsToServer(GroupedResultsXmlLoader().passingGroupedResults(metadata = resultsMetadata)).id, JacocoXmlLoader().serverApp())
    sendCoverageToServer(sendGroupedResultsToServer(GroupedResultsXmlLoader().passingGroupedResults(metadata = resultsMetadata)).id, JacocoXmlLoader().junitResultsParser())
    sendCoverageToServer(sendGroupedResultsToServer(GroupedResultsXmlLoader().passingGroupedResults(metadata = resultsMetadata)).id, JacocoXmlLoader().junitResultsParserReduced())

    println("View repository coverage timeline at $uiBaseUrl/repository/$repoName")
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
    val url = "$serverBaseUrl/run/$publicId/attachments/${attachmentFile.name}"
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

fun sendCoverageToServer(publicId: String, reportXml: String) {
    val client = OkHttpClient()

    val mediaType = "text/plain".toMediaType()
    val url = "$serverBaseUrl/run/$publicId/coverage"
    val requestBody = reportXml.toRequestBody(mediaType)

    val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

    client.newCall(request).execute()
}
