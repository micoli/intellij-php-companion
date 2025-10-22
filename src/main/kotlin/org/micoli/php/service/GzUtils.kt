package org.micoli.php.service

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.*
import java.util.zip.InflaterInputStream

object GzUtils {
    @Throws(IOException::class)
    fun gzUncompress(compressedData: ByteArray): ByteArray {
        require(compressedData.isNotEmpty()) { "Compressed data cannot be null or empty" }

        ByteArrayInputStream(compressedData).use { bis ->
            InflaterInputStream(bis).use { iis ->
                ByteArrayOutputStream().use { bos ->
                    val buffer = ByteArray(8192)
                    var count: Int
                    while ((iis.read(buffer).also { count = it }) > 0) {
                        bos.write(buffer, 0, count)
                    }
                    return bos.toByteArray()
                }
            }
        }
    }

    @Throws(IOException::class)
    fun gzUncompress(compressedData: ByteArray, encoding: String): String {
        val uncompressedData = gzUncompress(compressedData)
        return String(uncompressedData, charset(encoding))
    }

    @Throws(IOException::class)
    fun gzUncompressBase64(base64CompressedData: String?): ByteArray {
        val compressedData = Base64.getDecoder().decode(base64CompressedData)
        return gzUncompress(compressedData)
    }

    @Throws(IOException::class)
    fun gzUncompressBase64String(base64CompressedData: String?, encoding: String): String {
        val uncompressedData = gzUncompressBase64(base64CompressedData)
        return String(uncompressedData, charset(encoding))
    }
}
