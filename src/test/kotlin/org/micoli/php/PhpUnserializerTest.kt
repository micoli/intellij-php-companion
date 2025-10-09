package org.micoli.php

import com.fasterxml.jackson.databind.JsonNode
import java.io.*
import junit.framework.TestCase
import org.micoli.php.service.PhpGzDecoder
import org.micoli.php.service.serialize.PhpUnserializer
import org.micoli.php.symfony.profiler.SymfonyProfileService
import org.micoli.php.symfony.profiler.models.PHPProfilerDump

class PhpUnserializerTest : TestCase() {
    fun testItUnserializePhpProfileDump() {
        val kotlinResult =
            PhpUnserializer.unserialize(PhpGzDecoder.gzdecode(getGzcontent())) as Map<*, *>
        assertEquals("8af368", (kotlinResult["token"] as JsonNode).textValue())
        assertEquals(
            "Symfony\\Component\\HttpKernel\\DataCollector\\RequestDataCollector",
            (((kotlinResult["data"] as Map<*, *>)["request"] as Map<*, *>).get("__class")
                    as JsonNode)
                .textValue())
    }

    fun testItUnserializePhpProfileDumpInProperObject() {
        val jsonNode =
            PhpUnserializer.unserializeToJsonNode(PhpGzDecoder.gzdecode(getGzcontent())) as JsonNode

        assertEquals("8af368", jsonNode.get("token").textValue())
    }

    fun testItUnserializePhpProfileDumpInClass() {
        val profilerDump: PHPProfilerDump =
            SymfonyProfileService.unserializeProfileDump(PhpGzDecoder.gzdecode(getGzcontent()))

        assertEquals("8af368", profilerDump.token)
    }

    private fun getGzcontent(): ByteArray =
        FileInputStream("src/test/resources/profiler/var/cache/dev/profiler/68/f3/8af368")
            .readAllBytes()

    fun testItUnserializeNullByteArray() {
        val value: ByteArray? = null
        assertNull(PhpUnserializer.unserialize(value))
    }

    fun testItUnserializeNullString() {
        val value: String? = null
        assertNull(PhpUnserializer.unserialize(value?.toByteArray()))
    }

    fun testItUnserializeCommonDumps() {
        for (pair in
            listOf(
                Pair("a:2:{s:1:\"a\";i:1;s:1:\"b\";i:2;}", "{\"a\":1,\"b\":2}"),
                Pair("a:3:{i:0;i:1;s:1:\"a\";i:2;s:1:\"b\";i:3;}", "{\"0\":1,\"a\":2,\"b\":3}"),
                Pair("a:3:{i:0;s:1:\"a\";i:1;s:1:\"b\";i:2;s:1:\"c\";}", "[\"a\",\"b\",\"c\"]"),
                Pair("i:1;", "1"),
                Pair("d:1.1;", "1.1"),
                Pair("b:1;", "true"),
                Pair("N;", "null"),
                Pair("s:0:\"\";", "\"\""),
                Pair("s:5:\"abcde\";", "\"abcde\""),
                Pair(
                    "E:23:\"App\\Tests\\Misc\\AnEnum:B\";",
                    "{\"value\":\"B\",\"__class\":\"App\\\\Tests\\\\Misc\\\\AnEnum\"}"),
                Pair("a:0:{}", "[]"),
                Pair("O:8:\"stdClass\":0:{}", "{\"__class\":\"stdClass\"}"),
                Pair(
                    "O:30:\"App\\UseCase\\ListArticles\\Query\":2:{s:4:\"page\";i:1;s:3:\"tag\";N;}",
                    "{\"__class\":\"App\\\\UseCase\\\\ListArticles\\\\Query\",\"page\":1,\"tag\":null}",
                ),
                Pair(
                    "O:21:\"App\\Tests\\Misc\\AClass\":6:{s:26:\"\\x00App\\Tests\\Misc\\AClass\\x00int\";i:1;s:29:\"\\x00App\\Tests\\Misc\\AClass\\x00double\";d:1.1;s:29:\"\\x00App\\Tests\\Misc\\AClass\\x00string\";s:6:\"string\";s:27:\"\\x00App\\Tests\\Misc\\AClass\\x00bool\";b:1;s:29:\"\\x00App\\Tests\\Misc\\AClass\\x00anEnum\";E:23:\"App\\Tests\\Misc\\AnEnum:A\";s:33:\"\\x00App\\Tests\\Misc\\AClass\\x00asubObject\";O:24:\"App\\Tests\\Misc\\ASubClass\":2:{s:29:\"\\x00App\\Tests\\Misc\\ASubClass\\x00int\";i:1;s:32:\"\\x00App\\Tests\\Misc\\ASubClass\\x00double\";d:1.1;}}",
                    "{\"__class\":\"App\\\\Tests\\\\Misc\\\\AClass\"}",
                ),
                Pair(
                    "O:41:\"App\\Tests\\Misc\\AClassWithCustomSerializer\":2:{i:0;i:1;i:1;d:1.1;}",
                    "{\"__class\":\"App\\\\Tests\\\\Misc\\\\AClassWithCustomSerializer\"}",
                ),
            )) {
            assertEquals(
                pair.second,
                PhpUnserializer.unserializeToJsonNode(pair.first.toByteArray()).toString())
        }
    }
}
