package projektor.plugin.client

import java.util.zip.GZIPInputStream
import java.util.zip.GZIPOutputStream

class CompressionUtil {
    static byte[] gzip(String s) {
        def targetStream = new ByteArrayOutputStream()
        def zipStream = new GZIPOutputStream(targetStream)
        zipStream.write(s.getBytes('UTF-8'))
        zipStream.close()
        def zippedBytes = targetStream.toByteArray()
        targetStream.close()
        return zippedBytes
    }

   static String gunzip(byte[] compressed) {
       GZIPInputStream inflaterStream = new GZIPInputStream(new ByteArrayInputStream(compressed))
        String uncompressedStr = inflaterStream.getText('UTF-8')
        return uncompressedStr
    }
}
