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
import org.micoli.php.symfony.cliDumpParser.PhpDumpHelper
import org.micoli.php.utils.JsonAssertUtils.assertJsonEquals

@RunWith(Parameterized::class)
class PhpDumperHelperTest(private val filename: String?) {
    @Test
    @Throws(IOException::class)
    fun testItParseDump() {
        assertJsonEquals(readFile(".json"), PhpDumpHelper.parseCliDumperToJson(readFile(".txt")))
    }

    @Throws(IOException::class)
    private fun readFile(path: String?): String {
        return Files.readAllLines(Paths.get(DIRECTORY + filename + path), StandardCharsets.UTF_8).joinToString("\n")
    }

    companion object {
        private const val DIRECTORY = "src/test/resources/phpDumpHelperTestCases/"

        @JvmStatic
        @Parameterized.Parameters(name = "testItParseDump({0})")
        @Throws(IOException::class)
        fun parameters(): Collection<Array<String>> {
            Files.list(Paths.get(DIRECTORY)).use { stream ->
                return stream
                  .filter { path: Path? -> path.toString().endsWith(".txt") }
                  .map { path: Path? -> arrayOf(path!!.fileName.toString().replace(".txt", "")) }
                  .collect(Collectors.toList())
            }
        }
    }
}
