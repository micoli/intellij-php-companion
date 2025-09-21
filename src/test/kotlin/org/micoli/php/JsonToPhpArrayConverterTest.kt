package org.micoli.php

import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.stream.Collectors
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.micoli.php.symfony.cliDumpParser.JsonToPhpArrayConverter

@RunWith(Parameterized::class)
class JsonToPhpArrayConverterTest(private val filename: String?) {
    @Test
    @Throws(IOException::class)
    fun testItConvertJsonToPhp() {
        assert(readFile(".php") == JsonToPhpArrayConverter.convertJsonToPhp(readFile(".json")))
    }

    @Throws(IOException::class) private fun readFile(path: String?): String = Files.readAllLines(Paths.get(DIRECTORY + filename + path), StandardCharsets.UTF_8).joinToString("\n")

    companion object {
        private const val DIRECTORY = "src/test/resources/jsonToPhpArrayConverterTestCases/"

        @JvmStatic
        @Parameterized.Parameters(name = "Filter with \"{0}\" and isRegex is {1}")
        @Throws(IOException::class)
        fun parameters(): Collection<Array<String>> {
            Files.list(Paths.get(DIRECTORY)).use { stream ->
                return stream
                  .filter { path: Path? -> path != null }
                  .filter { path: Path -> path.toString().endsWith(".json") }
                  .map { path: Path -> arrayOf(path.fileName.toString().replace(".json", "")) }
                  .collect(Collectors.toList())
            }
        }
    }
}
