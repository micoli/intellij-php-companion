package org.micoli.php.service

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.util.zip.GZIPInputStream

object PhpGzDecoder {

    @Throws(Exception::class)
    fun gzdecode(compressedData: ByteArray): ByteArray? {
        require(compressedData.isNotEmpty()) {
            "Les données compressées ne peuvent pas être nulles ou vides"
        }

        ByteArrayInputStream(compressedData).use { bis ->
            GZIPInputStream(bis).use { gzis ->
                ByteArrayOutputStream().use { bos ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    while ((gzis.read(buffer).also { bytesRead = it }) != -1) {
                        bos.write(buffer, 0, bytesRead)
                    }
                    return bos.toByteArray()
                }
            }
        }
    }
}
