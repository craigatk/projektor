package projektor.plugin.client

import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.github.tomakehurst.wiremock.verification.LoggedRequest
import org.gradle.api.logging.Logger
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import projektor.parser.coverage.payload.CoverageFilePayload
import projektor.parser.coverage.payload.CoveragePayloadParser
import projektor.plugin.CoverageWireMockStubber
import projektor.plugin.coverage.CodeCoverageFile
import spock.lang.Specification
import spock.lang.Unroll

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig

class CoverageClientSpec extends Specification {
    @Rule
    WireMockRule wireMockRule = new WireMockRule(wireMockConfig().dynamicPort())

    @Rule
    TemporaryFolder coverageDir = new TemporaryFolder()

    CoverageWireMockStubber coverageStubber = new CoverageWireMockStubber(wireMockRule)

    Logger logger = Mock()

    String serverUrl

    def setup() {
        serverUrl = coverageStubber.serverUrl
    }

    def "when compression enabled should send compressed coverage report to server"() {
        given:
        boolean compressionEnabled = true
        CoverageClient coverageClient = new CoverageClient(
                new ClientConfig(serverUrl, compressionEnabled, Optional.empty(), 1, 0, 10_000),
                logger
        )

        String publicId = "cov12345"

        String coverageFileContents = "<coverage></coverage>"
        File coverageFile = coverageDir.newFile("coverage.xml")
        coverageFile.text = coverageFileContents

        coverageStubber.stubCoveragePostSuccess(publicId)

        CodeCoverageFile coverageGroup = new CodeCoverageFile(coverageFile, "")

        when:
        coverageClient.sendCoverageToServer(coverageGroup, publicId)

        then:
        List<LoggedRequest> coverageRequests = coverageStubber.findCoverageRequests(publicId)
        coverageRequests.size() == 1

        LoggedRequest coverageRequest = coverageRequests[0]
        coverageRequest.header("Content-Encoding").firstValue() == 'gzip'
    }

    @Unroll
    def "when base path #basePath should include it in payload"() {
        given:
        boolean compressionEnabled = true
        CoverageClient coverageClient = new CoverageClient(
                new ClientConfig(serverUrl, compressionEnabled, Optional.empty(), 1, 0, 10_000),
                logger
        )

        String publicId = "cov12345"

        String coverageFileContents = "<coverage></coverage>"
        File coverageFile = coverageDir.newFile("coverage.xml")
        coverageFile.text = coverageFileContents

        coverageStubber.stubCoveragePostSuccess(publicId)

        CodeCoverageFile coverageGroup = new CodeCoverageFile(coverageFile, basePath)

        when:
        coverageClient.sendCoverageToServer(coverageGroup, publicId)

        then:
        List<LoggedRequest> coverageRequests = coverageStubber.findCoverageRequests(publicId)
        coverageRequests.size() == 1

        LoggedRequest coverageRequest = coverageRequests[0]
        CoverageFilePayload coverageFilePayload = new CoveragePayloadParser().parseCoverageFilePayload(coverageRequest.bodyAsString)
        coverageFilePayload.reportContents == coverageFileContents
        coverageFilePayload.baseDirectoryPath == expectedBasePath

        where:
        basePath            || expectedBasePath
        "server/server-app" || "server/server-app"
        null                || null
    }
}
