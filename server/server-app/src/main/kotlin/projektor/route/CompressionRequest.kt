package projektor.route

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
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
