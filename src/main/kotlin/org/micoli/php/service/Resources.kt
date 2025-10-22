package org.micoli.php.service

import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

object Resources {
    @Throws(java.lang.Exception::class)
    fun readResourceFile(resourceName: String?): String {
        val inputStream = javaClass.getClassLoader().getResourceAsStream(resourceName)
        requireNotNull(inputStream) { "Resource not found: $resourceName" }

        BufferedReader(InputStreamReader(inputStream, StandardCharsets.UTF_8)).use { reader ->
            val content = java.lang.StringBuilder()
            var line: String?
            while ((reader.readLine().also { line = it }) != null) {
                content.append(line).append("\n")
            }
            return content.toString()
        }
    }
}
