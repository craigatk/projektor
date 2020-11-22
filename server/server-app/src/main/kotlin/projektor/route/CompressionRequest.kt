package projektor.route

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
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
