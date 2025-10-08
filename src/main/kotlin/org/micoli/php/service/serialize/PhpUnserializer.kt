package org.micoli.php.service.serialize

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.BooleanNode
import com.fasterxml.jackson.databind.node.DoubleNode
import com.fasterxml.jackson.databind.node.IntNode
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.node.TextNode
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.nio.charset.StandardCharsets

class PhpUnserializer {
    private var pos = 0
    private var data = ByteArray(0)
    private val references = mutableListOf<JsonNode>()

    companion object {
        fun unserializeToJsonNode(content: ByteArray?): JsonNode? {
            return PhpUnserializer().innerUnserialize(content)
        }

        fun <T : Any?> unserializeTo(
            content: ByteArray?,
            to: Class<T>,
            transformer: JsonTransformer? = null
        ): T {
            val mapper = ObjectMapper()
            mapper.registerModule(
                KotlinModule.Builder()
                    .configure(KotlinFeature.KotlinPropertyNameAsImplicitName, true)
                    .build())
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            mapper.enable(com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT)

            var jsonContent = PhpUnserializer().innerUnserialize(content)

            // java.io.File("src/test/resources/test1.json").writeText(jsonContent)
            if (transformer != null) {
                jsonContent = transformer.run(jsonContent)
            }
            // java.io.File("src/test/resources/test2.json").writeText(jsonContent)

            return mapper.treeToValue(jsonContent, to)
        }

        fun unserialize(content: ByteArray?): Any? {
            return toKotlinObject(PhpUnserializer().innerUnserialize(content ?: ByteArray(0)))
        }

        private fun toKotlinObject(value: JsonNode?): Any? {
            return when (value) {
                null -> null
                is NullNode -> null
                is BooleanNode -> value
                is IntNode -> value
                is DoubleNode -> value
                is TextNode -> value
                is ObjectNode -> {
                    val map = LinkedHashMap<Any, Any?>()
                    value.properties().forEach { (k, v) -> map[k] = toKotlinObject(v) }
                    map
                }

                is ArrayNode -> {
                    val list = ArrayList<Any?>()
                    value.forEach { v -> list.add(toKotlinObject(v)) }
                    list
                }

                else -> value
            }
        }
    }

    private fun innerUnserialize(content: ByteArray?): JsonNode? {
        if (content == null) {
            return NullNode.instance
        }
        pos = 0
        data = content
        references.clear()

        if (content.isEmpty()) {
            return null
        }

        return parseValue()
    }

    private fun parseValue(): JsonNode {
        if (pos >= data.size) {
            throw PhpUnserializeException("Unexpected end of data at position $pos," + posContext())
        }

        val type = data[pos].toInt().toChar()
        pos++

        return when (type) {
            'N' -> parseNull()
            'b' -> parseBoolean()
            'i' -> parseInteger()
            'd' -> parseDouble()
            's' -> parseString()
            'a' -> parseArray()
            'O' -> parseObject()
            'C' -> parseCustomObject()
            'R' -> parseReference()
            'E' -> parseEnum()
            'r' -> parseReference()
            else ->
                throw PhpUnserializeException(
                    "Unknown type '$type' at position ${pos - 1}," + posContext())
        }
    }

    private fun parseNull(): JsonNode {
        expectChar(';')
        return NullNode.instance
    }

    private fun parseBoolean(): JsonNode {
        expectChar(':')
        val value =
            when (data[pos]) {
                '0'.code.toByte() -> false
                '1'.code.toByte() -> true
                else ->
                    throw PhpUnserializeException(
                        "Invalid boolean value at position $pos," + posContext())
            }
        pos++
        expectChar(';')
        return if (value) BooleanNode.TRUE else BooleanNode.FALSE
    }

    private fun parseInteger(): JsonNode {
        expectChar(':')
        val start = pos
        while (pos < data.size && data[pos] != ';'.code.toByte()) {
            pos++
        }
        val numStr = extractString(start, pos)
        expectChar(';')

        return try {
            IntNode(numStr.toInt())
        } catch (_: NumberFormatException) {
            throw PhpUnserializeException(
                "Invalid integer value '$numStr' at position $start," + posContext())
        }
    }

    private fun extractString(start: Int, end: Int): String {
        return String(data, start, end - start, StandardCharsets.UTF_8)
    }

    private fun parseDouble(): JsonNode {
        expectChar(':')
        val start = pos
        while (pos < data.size && data[pos] != ';'.code.toByte()) {
            pos++
        }
        val numStr = extractString(start, pos)
        expectChar(';')

        return try {
            when (numStr) {
                "INF" -> DoubleNode(Double.POSITIVE_INFINITY)
                "-INF" -> DoubleNode(Double.NEGATIVE_INFINITY)
                "NAN" -> DoubleNode(Double.NaN)
                else -> DoubleNode(numStr.toDouble())
            }
        } catch (_: NumberFormatException) {
            throw PhpUnserializeException(
                "Invalid double value '$numStr' at position $start," + posContext())
        }
    }

    private fun parseString(): JsonNode {
        expectChar(':')
        val length = readNumber()
        expectChar(':')
        expectChar('"')

        val bytes = data.copyOfRange(pos, pos + length)
        pos += length

        expectChar('"')
        expectChar(';')

        val value = String(bytes, StandardCharsets.UTF_8)
        val result = TextNode(value)
        references.add(result)
        return result
    }

    private fun parseEnum(): JsonNode {
        expectChar(':')
        val length = readNumber()
        expectChar(':')
        expectChar('"')

        val bytes = data.copyOfRange(pos, pos + length)
        pos += length

        expectChar('"')
        expectChar(';')

        val parts = String(bytes, StandardCharsets.UTF_8).split(":", limit = 2)
        val enum = ObjectMapper().createObjectNode().put("value", parts[1]).put("__class", parts[0])
        references.add(enum)
        return enum
    }

    private fun parseArray(): JsonNode {
        expectChar(':')
        val size = readNumber()
        expectChar(':')
        expectChar('{')

        val array = LinkedHashMap<Any, JsonNode>()

        repeat(size) {
            val key =
                when (val keyValue = parseValue()) {
                    is TextNode -> keyValue.textValue()
                    is IntNode -> keyValue.intValue()
                    else ->
                        throw PhpUnserializeException(
                            "Invalid array key type at position $pos," + posContext())
                }

            val value = parseValue()
            array[key] = value
        }
        expectChar('}')
        if (array.keys.all { it is Integer }) {
            if ((0..<array.keys.size).joinToString(",") == array.keys.joinToString(",")) {
                val result = ObjectMapper().createArrayNode()
                for (element in array.values.toList()) {
                    result.add(element)
                }
                references.add(result)
                return result
            }
        }
        val result = ObjectMapper().createObjectNode()
        for (element in array) {
            result.set<JsonNode>(element.key.toString(), element.value)
        }
        references.add(result)
        return result
    }

    private fun parseObject(): JsonNode {
        expectChar(':')
        val classNameLength = readNumber()
        expectChar(':')
        expectChar('"')

        val className = extractString(pos, pos + classNameLength)
        pos += classNameLength

        expectChar('"')
        expectChar(':')
        val propertyCount = readNumber()
        expectChar(':')
        expectChar('{')

        val result = ObjectMapper().createObjectNode()
        result.putIfAbsent("__class", TextNode(className))
        references.add(result)

        try {
            repeat(propertyCount) {
                val propName =
                    when (val propKey = parseValue()) {
                        is TextNode -> {
                            val name = propKey.textValue()
                            when {
                                name.startsWith("\u0000*\u0000") -> name.substring(3)
                                name.contains("\u0000") -> {
                                    val parts = name.split("\u0000")
                                    if (parts.size >= 3) parts[2] else name
                                }

                                else -> name
                            }
                        }

                        else ->
                            throw PhpUnserializeException(
                                "Invalid object property name at position $pos," + posContext())
                    }

                val propValue = parseValue()
                result.putIfAbsent(propName, propValue)
            }

            expectChar('}')
        } catch (_: Exception) {
            var braceCount = 1 // On est déjà dans un '{'
            var attempts = 0
            val maxAttempts = 40000

            while (pos < data.size && attempts < maxAttempts && braceCount > 0) {
                val currentChar = data[pos].toInt().toChar()
                when (currentChar) {
                    '{' -> braceCount++
                    '}' -> braceCount--
                }
                pos++
                attempts++
            }
        }

        return result
    }

    private fun parseCustomObject(): JsonNode {
        expectChar(':')
        val classNameLength = readNumber()
        expectChar(':')
        expectChar('"')

        val className = extractString(pos, pos + classNameLength)
        pos += classNameLength

        expectChar('"')
        expectChar(':')
        val dataLength = readNumber()
        expectChar(':')
        expectChar('{')

        val dataStart = pos

        try {
            val customData = extractString(pos, pos + dataLength)
            pos += dataLength

            expectChar('}')

            val result = ObjectMapper().createObjectNode()
            result.putIfAbsent("__class", TextNode(className))
            result.putIfAbsent("__serialized_data", TextNode(customData))
            references.add(result)
            return result
        } catch (_: Exception) {
            pos = dataStart + dataLength

            if (pos < data.size && data[pos].toInt().toChar() == '}') {
                pos++
            }

            val emptyObject = ObjectMapper().createObjectNode()
            emptyObject.putIfAbsent("__class", TextNode(className))
            references.add(emptyObject)
            return emptyObject
        }
    }

    private fun parseReference(): JsonNode {
        expectChar(':')
        val index = readNumber()
        expectChar(';')

        val refIndex = index - 1

        if (refIndex < 0 || refIndex >= references.size) {
            throw PhpUnserializeException(
                "Invalid reference index $index at position $pos," + posContext())
        }

        return references[refIndex]
    }

    private fun readNumber(): Int {
        val start = pos
        var negative = false

        if (pos < data.size && data[pos] == '-'.code.toByte()) {
            negative = true
            pos++
        }

        while (pos < data.size && data[pos].toInt().toChar().isDigit()) {
            pos++
        }

        if (start == pos || (negative && start + 1 == pos)) {
            throw PhpUnserializeException("Expected number at position $start," + posContext())
        }

        val numStr = extractString(start, pos)
        return try {
            numStr.toInt()
        } catch (_: NumberFormatException) {
            throw PhpUnserializeException(
                "Invalid number '$numStr' at position $start," + posContext())
        }
    }

    private fun expectChar(char: Char) {
        if (pos >= data.size) {
            throw PhpUnserializeException(
                "Expected '$char' but reached end of data," + posContext())
        }
        val actualChar = data[pos].toInt().toChar()
        if (actualChar != char) {
            throw PhpUnserializeException(
                "Expected '$char' but found '$actualChar' at position $pos," + posContext())
        }
        pos++
    }

    private fun posContext(contextSize: Int = 120): String {
        val prefix = data.copyOfRange(pos - contextSize, pos).toString(StandardCharsets.UTF_8)
        val currentChar = data[pos].toInt().toChar()
        val suffix = data.copyOfRange(pos + 1, pos + contextSize).toString(StandardCharsets.UTF_8)
        return "$prefix[$currentChar]$suffix"
    }
}
