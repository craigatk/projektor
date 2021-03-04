package projektor.server.example

import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import projektor.parser.GroupedResultsXmlLoader
import projektor.parser.ResultsXmlLoader
import projektor.parser.grouped.model.CoverageFile
import projektor.parser.grouped.model.GitMetadata
import projektor.parser.grouped.model.ResultsMetadata
import projektor.server.api.results.SaveResultsResponse
import projektor.server.example.coverage.CoverageFileWrapper.createCoverageFilePayload
import projektor.server.example.coverage.JacocoXmlLoader
import projektor.server.example.coverage.JestXmlLoader
import projektor.server.example.performance.PerformanceResultsLoader
import java.io.File
import java.math.BigDecimal

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

fun loadCypressExamplesWithFilePaths() {
    val resultsXmlLoader = ResultsXmlLoader()
    val saveResultsResponse = sendResultsToServer(resultsXmlLoader.cypressResultsWithFilePaths())
    println("View run with Cypress tests with file paths at at $uiBaseUrl${saveResultsResponse.uri}")
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

fun loadK6Examples() {
    val k6ExampleResponse = sendGroupedResultsToServer(GroupedResultsXmlLoader().wrapResultsXmlInGroup(ResultsXmlLoader().k6Example()))
    println("View run with k6 example results at at $uiBaseUrl${k6ExampleResponse.uri}")

    val k6GetFailedTestCasesLargeResponse = sendGroupedResultsToServer(GroupedResultsXmlLoader().wrapResultsXmlInGroup(ResultsXmlLoader().k6GetFailedTestCasesLarge()))
    println("View run with k6 getFailedTestCasesLarge results at at $uiBaseUrl${k6GetFailedTestCasesLargeResponse.uri}")
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

fun loadMultipleTestRunsFromSameRepoForTimeline() {
    val repoName = "timeline-org/timeline-repo"
    val branchName = "main"
    val gitMetadata = GitMetadata()
    gitMetadata.repoName = repoName
    gitMetadata.branchName = branchName
    gitMetadata.isMainBranch = true
    val resultsMetadata = ResultsMetadata()
    resultsMetadata.git = gitMetadata
    resultsMetadata.ci = true

    sendGroupedResultsToServer(GroupedResultsXmlLoader().passingGroupedResults(metadata = resultsMetadata))
    sendGroupedResultsToServer(GroupedResultsXmlLoader().wrapResultsXmlInGroup(resultsXml = ResultsXmlLoader().jestUi(), metadata = resultsMetadata))
    sendGroupedResultsToServer(GroupedResultsXmlLoader().wrapResultsXmlInGroup(resultsXml = ResultsXmlLoader().longOutput(), metadata = resultsMetadata))
    sendGroupedResultsToServer(GroupedResultsXmlLoader().wrapResultsXmlInGroup(resultsXml = ResultsXmlLoader().someIgnored(), metadata = resultsMetadata))

    println("View repository test run timeline at $uiBaseUrl/repository/$repoName")
}

fun loadMultipleShortTestRunsFromSameRepoForTimeline() {
    val repoName = "timeline-org/short-repo"
    val branchName = "main"
    val gitMetadata = GitMetadata()
    gitMetadata.repoName = repoName
    gitMetadata.branchName = branchName
    gitMetadata.isMainBranch = true
    val resultsMetadata = ResultsMetadata()
    resultsMetadata.git = gitMetadata
    resultsMetadata.ci = true

    sendGroupedResultsToServer(GroupedResultsXmlLoader().passingGroupedResults(metadata = resultsMetadata))
    sendGroupedResultsToServer(GroupedResultsXmlLoader().wrapResultsXmlInGroup(resultsXml = ResultsXmlLoader().longOutput(), metadata = resultsMetadata))
    sendGroupedResultsToServer(GroupedResultsXmlLoader().wrapResultsXmlInGroup(resultsXml = ResultsXmlLoader().someIgnored(), metadata = resultsMetadata))

    println("View repository short test run timeline at $uiBaseUrl/repository/$repoName")
}

fun slowTimeline() {
    val repoName = "slow-org/timeline-repo"
    val branchName = "main"
    val gitMetadata = GitMetadata()
    gitMetadata.repoName = repoName
    gitMetadata.branchName = branchName
    gitMetadata.isMainBranch = true
    val resultsMetadata = ResultsMetadata()
    resultsMetadata.git = gitMetadata
    resultsMetadata.ci = true

    sendGroupedResultsToServer(GroupedResultsXmlLoader().wrapResultsXmlInGroup(resultsXml = ResultsXmlLoader().slow(), metadata = resultsMetadata))
    sendGroupedResultsToServer(GroupedResultsXmlLoader().wrapResultsXmlInGroup(resultsXml = ResultsXmlLoader().slower(), metadata = resultsMetadata))
    sendGroupedResultsToServer(GroupedResultsXmlLoader().wrapResultsXmlInGroup(resultsXml = ResultsXmlLoader().slow(), metadata = resultsMetadata))

    println("View repository slow tests run timeline at $uiBaseUrl/repository/$repoName")
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

fun loadCoverage75WithGit() {
    val repoName = "projektor/coverage75"
    val branchName = "main"
    val gitMetadata = GitMetadata()
    gitMetadata.repoName = repoName
    gitMetadata.branchName = branchName
    gitMetadata.isMainBranch = true
    val resultsMetadata = ResultsMetadata()
    resultsMetadata.git = gitMetadata

    val coverageFile = CoverageFile()
    coverageFile.reportContents = JacocoXmlLoader().jacocoXmlParser75()

    val resultsResponse = sendGroupedResultsToServer(
        GroupedResultsXmlLoader().passingResultsWithCoverage(
            coverageFiles = listOf(coverageFile),
            metadata = resultsMetadata
        )
    )

    println("View run 75% coverage and Git metadata at $uiBaseUrl${resultsResponse.uri}")
}

fun loadCoverage85WithGit() {
    val repoName = "projektor/coverage75"
    val branchName = "main"
    val gitMetadata = GitMetadata()
    gitMetadata.repoName = repoName
    gitMetadata.branchName = branchName
    gitMetadata.isMainBranch = true
    val resultsMetadata = ResultsMetadata()
    resultsMetadata.git = gitMetadata

    val coverageFile = CoverageFile()
    coverageFile.reportContents = JacocoXmlLoader().jacocoXmlParser85()

    val resultsResponse = sendGroupedResultsToServer(
        GroupedResultsXmlLoader().passingResultsWithCoverage(
            coverageFiles = listOf(coverageFile),
            metadata = resultsMetadata
        )
    )

    println("View run 85% coverage and Git metadata at $uiBaseUrl${resultsResponse.uri}")
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

fun repositoryFlakyTests() {
    val repoName = "flaky-org/flaky-repo"
    val branchName = "main"
    val gitMetadata = GitMetadata()
    gitMetadata.repoName = repoName
    gitMetadata.branchName = branchName
    gitMetadata.isMainBranch = true
    val resultsMetadata = ResultsMetadata()
    resultsMetadata.git = gitMetadata
    resultsMetadata.ci = true

    repeat(5) {
        sendGroupedResultsToServer(GroupedResultsXmlLoader().wrapResultsXmlInGroup(resultsXml = ResultsXmlLoader().failing(), metadata = resultsMetadata))
    }

    repeat(6) {
        sendGroupedResultsToServer(GroupedResultsXmlLoader().wrapResultsXmlInGroup(resultsXml = ResultsXmlLoader().failingLongFailureMessage(), metadata = resultsMetadata))
    }

    repeat(2) {
        sendGroupedResultsToServer(GroupedResultsXmlLoader().wrapResultsXmlInGroup(resultsXml = ResultsXmlLoader().passing(), metadata = resultsMetadata))
    }

    println("View repository with flaky tests at $uiBaseUrl/repository/$repoName/tests/flaky")
}

fun loadJestWithCoverage() {
    val resultsResponse = sendResultsToServer(ResultsXmlLoader().jestUi())
    sendCoverageToServer(resultsResponse.id, JestXmlLoader().uiClover())

    println("View run with Jest results and coverage at $uiBaseUrl${resultsResponse.uri}")
}

fun loadJestWithFilePaths() {
    val resultsResponse = sendGroupedResultsToServer(GroupedResultsXmlLoader().wrapResultsXmlInGroup(resultsXml = ResultsXmlLoader().jestUiFilePath()))

    println("View run with Jest results with file paths at $uiBaseUrl${resultsResponse.uri}")
}

fun loadPerformanceK6GetRun() {
    val k6Results = PerformanceResultsLoader().k6GetRun()
    val groupedResults = GroupedResultsXmlLoader().wrapPerformanceResultsInGroup("perf.json", k6Results, null)
    val resultsResponse = sendGroupedResultsToServer(groupedResults)

    println("View run with k6 get-run performance results at $uiBaseUrl${resultsResponse.uri}")
}

fun loadPerformanceK6GetFailedTestCasesLarge() {
    val k6Results = PerformanceResultsLoader().k6GetFailedTestCasesLarge()
    val groupedResults = GroupedResultsXmlLoader().wrapPerformanceResultsInGroup("perf.json", k6Results, null)
    val resultsResponse = sendGroupedResultsToServer(groupedResults)

    println("View run with k6 get-failed-test-cases-large performance results at $uiBaseUrl${resultsResponse.uri}")
}

fun performanceSingleTestTimeline() {
    val repoName = "performance-org/performance-repo"
    val branchName = "main"
    val gitMetadata = GitMetadata()
    gitMetadata.repoName = repoName
    gitMetadata.branchName = branchName
    gitMetadata.isMainBranch = true
    val resultsMetadata = ResultsMetadata()
    resultsMetadata.git = gitMetadata

    val testName = "perf-test"

    val k6GetFailedTestCasesLargeResults = PerformanceResultsLoader().k6GetFailedTestCasesLarge()
    val k6GetRunResults = PerformanceResultsLoader().k6GetRun()

    sendGroupedResultsToServer(GroupedResultsXmlLoader().wrapPerformanceResultsInGroup(testName, k6GetRunResults, resultsMetadata))
    sendGroupedResultsToServer(GroupedResultsXmlLoader().wrapPerformanceResultsInGroup(testName, k6GetFailedTestCasesLargeResults, resultsMetadata))
    sendGroupedResultsToServer(GroupedResultsXmlLoader().wrapPerformanceResultsInGroup(testName, k6GetRunResults, resultsMetadata))

    println("View performance test timeline with single test at $uiBaseUrl/repository/$repoName")
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

fun coveragePayloadWithBaseDirectory() {
    val repoName = "craigatk/projektor"
    val branchName = "master"
    val gitMetadata = GitMetadata()
    gitMetadata.repoName = repoName
    gitMetadata.branchName = branchName
    gitMetadata.isMainBranch = true
    val resultsMetadata = ResultsMetadata()
    resultsMetadata.git = gitMetadata

    val resultsResponse = sendGroupedResultsToServer(GroupedResultsXmlLoader().passingGroupedResults(metadata = resultsMetadata))
    val publicId = resultsResponse.id
    sendCoveragePayloadToServer(publicId, createCoverageFilePayload(JacocoXmlLoader().jacocoXmlParser(), "server/parsing/jacoco-xml-parser/src/main/kotlin"))
    sendCoveragePayloadToServer(publicId, createCoverageFilePayload(JacocoXmlLoader().serverApp(), "server/server-app/src/main/kotlin"))

    println("View run with coverage base directory and Git metadata at $uiBaseUrl${resultsResponse.uri}")
}

fun invalidResults() {
    sendGroupedResultsToServer(GroupedResultsXmlLoader().wrapResultsXmlInGroup(ResultsXmlLoader().invalid()))

    println("View admin page with results failures at $uiBaseUrl/admin")
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

fun sendCoveragePayloadToServer(publicId: String, reportPayload: String) {
    val client = OkHttpClient()

    val mediaType = "application/json".toMediaType()
    val url = "$serverBaseUrl/run/$publicId/coverageFile"
    val requestBody = reportPayload.toRequestBody(mediaType)

    val request = Request.Builder()
        .url(url)
        .post(requestBody)
        .build()

    client.newCall(request).execute()
}
