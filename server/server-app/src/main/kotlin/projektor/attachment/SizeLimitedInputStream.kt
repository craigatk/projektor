package projektor.attachment

import java.io.FilterInputStream
import java.io.IOException
import java.io.InputStream

/**
 * Fails a read once more than maxBytes have been read, for streams whose size isn't known up front.
 */
class SizeLimitedInputStream(stream: InputStream, private val maxBytes: Long) : FilterInputStream(stream) {
    private var bytesRead = 0L

    var limitExceeded = false
        private set

    override fun read(): Int {
        val byte = super.read()

        if (byte != -1) {
            countBytes(1)
        }

        return byte
    }

    override fun read(
        buffer: ByteArray,
        offset: Int,
        length: Int,
    ): Int {
        val count = super.read(buffer, offset, length)

        if (count > 0) {
            countBytes(count)
        }

        return count
    }

    private fun countBytes(count: Int) {
        bytesRead += count

        if (bytesRead > maxBytes) {
            limitExceeded = true
            throw IOException("Stream is larger than the limit of $maxBytes bytes")
        }
    }
}
