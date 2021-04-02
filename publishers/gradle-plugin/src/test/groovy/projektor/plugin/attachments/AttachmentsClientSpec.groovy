package projektor.plugin.attachments

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.http.HttpHeader
import com.github.tomakehurst.wiremock.verification.LoggedRequest
import org.gradle.api.logging.Logger
import projektor.plugin.AttachmentsWireMockStubber
import projektor.plugin.client.ClientConfig
import projektor.plugin.client.ClientToken
import spock.lang.Specification
import spock.lang.Unroll

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig

class AttachmentsClientSpec extends Specification {

    WireMockServer wireMockServer = new WireMockServer(wireMockConfig().dynamicPort())

    AttachmentsWireMockStubber attachmentsStubber = new AttachmentsWireMockStubber(wireMockServer)

    Logger logger = Mock()

    def setup() {
        wireMockServer.start()
    }

    def cleanup() {
        wireMockServer.stop()
    }

    @Unroll
    void "should send attachment to server token in header #expectedTokenPresent"() {
        given:
        String serverUrl = attachmentsStubber.serverUrl

        String publicId = "ATT123"

        AttachmentsClient attachmentsClient = new AttachmentsClient(
                new ClientConfig(serverUrl, false, maybePublishToken, 1, 0, 10_000),
                logger
        )

        File attachmentFile = new File("src/test/resources/attachment1.txt")

        attachmentsStubber.stubAttachmentPostSuccess(publicId, "attachment1.txt")

        when:
        attachmentsClient.sendAttachmentToServer(publicId, attachmentFile)

        then:
        List<LoggedRequest> attachmentRequests = attachmentsStubber.findAttachmentsRequests(publicId)
        attachmentRequests.size() == 1

        and:
        attachmentRequests[0].url.endsWith("attachments/attachment1.txt")

        and:
        attachmentRequests[0].bodyAsString == "Here is attachment 1"

        and:
        HttpHeader publishTokenInHeader = attachmentRequests[0].header(ClientToken.PUBLISH_TOKEN_NAME)
        publishTokenInHeader.present == expectedTokenPresent

        if (expectedTokenPresent) {
            assert publishTokenInHeader.firstValue() == expectedTokenValue
        }

        where:
        maybePublishToken     || expectedTokenPresent | expectedTokenValue
        Optional.empty()      || false                | null
        Optional.of("TOKEN1") || true                 | "TOKEN1"
    }
}
