import java.util.zip.GZIPInputStream

fun ungzip(content: ByteArray): String =
        GZIPInputStream(content.inputStream()).bufferedReader(Charsets.UTF_8).use { it.readText() }
