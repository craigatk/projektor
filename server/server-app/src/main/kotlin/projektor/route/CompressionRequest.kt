package projektor.route

import io.ktor.http.HttpHeaders
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.header
import io.ktor.server.request.receive
import projektor.util.ungzip

object CompressionRequest {
    suspend fun receiveCompressedOrPlainTextPayload(call: ApplicationCall): String {
        val payload = if (call.request.header(HttpHeaders.ContentEncoding) == "gzip") {
            ungzip(call.receive<ByteArray>())
        } else {
            call.receive<String>()
        }

        return payload
    }
}
