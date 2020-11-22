package projektor.plugin.client

import okhttp3.MediaType
import okhttp3.Request
import okio.Buffer
import org.gradle.api.logging.Logger
import spock.lang.Specification

import java.nio.charset.Charset

import static projektor.plugin.client.CompressionUtil.gunzip

class AbstractClientSpec extends Specification {
    Logger logger = Mock()

    def "when compression enabled should compress payload"() {
        given:
        final boolean compressionEnabled = true
        AbstractClient client = new AbstractClient(
                new ClientConfig("http://localhost:8080", compressionEnabled, Optional.empty(), 1, 0, 10_000),
                logger
        ) {}

        String partialUrl = "/groupedResults"
        MediaType mediaType = MediaType.get("application/json")
        String payload = '{"key": "value"}'

        when:
        Request request = client.buildRequest(partialUrl, mediaType, payload)

        then:
        Buffer buffer = new Buffer()
        request.body.writeTo(buffer)

        String uncompressedPayload = gunzip(buffer.readByteArray())
        uncompressedPayload == payload
    }

    def "when compression not enabled should not compress payload"() {
        given:
        final boolean compressionEnabled = false
        AbstractClient client = new AbstractClient(
                new ClientConfig("http://localhost:8080", compressionEnabled, Optional.empty(), 1, 0, 10_000),
                logger
        ) {}

        String partialUrl = "/groupedResults"
        MediaType mediaType = MediaType.get("application/json")
        String payload = '{"key": "value"}'

        when:
        Request request = client.buildRequest(partialUrl, mediaType, payload)

        then:
        Buffer buffer = new Buffer()
        request.body.writeTo(buffer)

        String requestBody = buffer.readString(Charset.forName("UTF-8"))
        requestBody == payload
    }
}
