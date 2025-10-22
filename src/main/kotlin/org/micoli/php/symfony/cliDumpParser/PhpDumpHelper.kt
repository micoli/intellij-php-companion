package org.micoli.php.symfony.cliDumpParser

import com.google.gson.*
import java.util.*
import java.util.regex.Pattern

object PhpDumpHelper {
    fun parseCliDumperToJson(cliDumperOutput: String): String? {
        return GsonBuilder()
            .setPrettyPrinting()
            .create()
            .toJson(parseValue(cleanAnsiEscapeSequences(cliDumperOutput)))
    }

    private fun cleanAnsiEscapeSequences(input: String): String {
        return input
            .trim { it <= ' ' }
            .replace("\\n", "")
            .replace("\u001B".toRegex(), " ")
            .replace("\\s{0,2}]8;;file:.*? ]8;; \\\\".toRegex(), "")
    }

    private fun parseValue(input: String): JsonElement {
        var input = input
        input = input.trim { it <= ' ' }

        // Match an array
        val arrayPattern = Pattern.compile("^array:\\d+\\s*\\[\\s*(.*)?\\s*]$", Pattern.DOTALL)
        val arrayMatcher = arrayPattern.matcher(input)
        if (arrayMatcher.matches()) {
            val content = arrayMatcher.group(1)
            return parseArray(content ?: "")
        }

        // Match an object (App\Tests\TestDTO {#383)
        val namedObjectPattern =
            Pattern.compile(
                "^([A-Za-z_\\\\][A-Za-z0-9_\\\\]*?)(\\s@[A-Za-z0-9]*)?\\s*\\{#\\d+\\s*(.*?)\\s*}$",
                Pattern.DOTALL)
        val namedObjectMatcher = namedObjectPattern.matcher(input)
        if (namedObjectMatcher.matches()) {
            return parseObject(namedObjectMatcher.group(3))
        }

        // Match an anonym class (class@anonymous {#382)
        val anonymousObjectPattern =
            Pattern.compile("^class@anonymous\\s*\\{#\\d+\\s*(.*?)\\s*}$", Pattern.DOTALL)
        val anonymousObjectMatcher = anonymousObjectPattern.matcher(input)
        if (anonymousObjectMatcher.matches()) {
            return parseObject(anonymousObjectMatcher.group(1))
        }

        // Match a string with double quotes
        val stringPattern = Pattern.compile("^\"(.*)\"$")
        val stringMatcher = stringPattern.matcher(input)
        if (stringMatcher.matches()) {
            return JsonPrimitive(stringMatcher.group(1))
        }

        // Match a number
        if (isNumeric(input)) {
            return try {
                if (input.contains(".")) {
                    JsonPrimitive(input.toDouble())
                } else {
                    JsonPrimitive(input.toInt())
                }
            } catch (_: NumberFormatException) {
                JsonPrimitive(input)
            }
        }

        // Match a boolean
        return when (input) {
            "true" -> JsonPrimitive(true)
            "false" -> JsonPrimitive(false)
            "null" -> JsonNull.INSTANCE
            else -> JsonPrimitive(input)
        }
    }

    private fun parseObject(content: String?): JsonObject {
        val jsonObject = JsonObject()

        if (content == null || content.trim { it <= ' ' }.isEmpty()) {
            return jsonObject
        }

        val properties = splitObjectProperties(content)

        for (property in properties) {
            var property = property
            property = property.trim { it <= ' ' }

            // Pattern for property with visibility (+name:, -name:, #name:)
            val propertyPattern = Pattern.compile("^([+\\-#])([^:]+):\\s*(.+)$", Pattern.DOTALL)
            val propertyMatcher = propertyPattern.matcher(property)

            if (propertyMatcher.matches()) {
                // val visibility = propertyMatcher.group(1)
                val propertyName = propertyMatcher.group(2).trim { it <= ' ' }
                val propertyValue = propertyMatcher.group(3).trim { it <= ' ' }

                jsonObject.add(propertyName, parseValue(propertyValue))
                continue
            }

            // Ssearch pattern "key" => "value"
            val keyValuePattern = Pattern.compile("^\"([^\"]*)\"\\s*=>\\s*(.+)$", Pattern.DOTALL)
            val keyValueMatcher = keyValuePattern.matcher(property)
            if (keyValueMatcher.matches()) {
                val key = keyValueMatcher.group(1)
                val value = keyValueMatcher.group(2).trim { it <= ' ' }
                jsonObject.add(key, parseValue(value))

                continue
            }

            // Handle simple property names (like "time", "date", "uuid", etc.)
            val simplePropertyPattern =
                Pattern.compile("^([a-zA-Z_][a-zA-Z0-9_]*):\\s*(.+)$", Pattern.DOTALL)
            val simplePropertyMatcher = simplePropertyPattern.matcher(property)
            if (simplePropertyMatcher.matches()) {
                val key = simplePropertyMatcher.group(1)
                val value = simplePropertyMatcher.group(2).trim { it <= ' ' }
                jsonObject.add(key, parseValue(value))
                continue
            }

            // Property without explicit key
            jsonObject.add("property_" + jsonObject.size(), parseValue(property))
        }

        return jsonObject
    }

    private fun parseArray(content: String?): JsonElement {
        if (content == null || content.trim { it <= ' ' }.isEmpty()) {
            return JsonArray()
        }

        val items = splitObjectProperties(content)
        val jsonObject = JsonObject()
        val jsonArray = JsonArray()
        var isAssociative = false
        var isIndexed = false
        val indexedItems: MutableMap<Int?, JsonElement?> = HashMap<Int?, JsonElement?>()

        for (item in items) {
            var item = item
            item = item.trim { it <= ' ' }

            // Search pattern "key" => "value"
            val quotedKeyPattern = Pattern.compile("^\"([^\"]*)\"\\s*=>\\s*(.+)$", Pattern.DOTALL)
            val quotedKeyMatcher = quotedKeyPattern.matcher(item)
            if (quotedKeyMatcher.matches()) {
                val key = quotedKeyMatcher.group(1)
                val value = quotedKeyMatcher.group(2).trim { it <= ' ' }
                jsonObject.add(key, parseValue(value))
                isAssociative = true
                continue
            }

            // Search pattern key => value (no quote around key)
            val keyPattern = Pattern.compile("^([^\\s=>]+)\\s*=>\\s*(.+)$", Pattern.DOTALL)
            val keyMatcher = keyPattern.matcher(item)
            if (keyMatcher.matches()) {
                val key = keyMatcher.group(1)
                val value = keyMatcher.group(2).trim { it <= ' ' }

                // Check if key is a numerical index
                if (isNumeric(key)) {
                    val index = key.toInt()
                    indexedItems[index] = parseValue(value)
                    isIndexed = true
                } else {
                    jsonObject.add(key, parseValue(value))
                    isAssociative = true
                }
                continue
            }

            // Search pattern index => value (for indexed arrays)
            val indexPattern = Pattern.compile("^(\\d+)\\s*=>\\s*(.+)$", Pattern.DOTALL)
            val indexMatcher = indexPattern.matcher(item)
            if (indexMatcher.matches()) {
                val index = indexMatcher.group(1).toInt()
                val value = indexMatcher.group(2).trim { it <= ' ' }
                indexedItems[index] = parseValue(value)
                isIndexed = true
                continue
            }

            // Simple Value (for arrays without explicit keys)
            jsonArray.add(parseValue(item))
            isIndexed = true
        }

        if (isAssociative && !isIndexed) {
            return jsonObject
        }
        if (isIndexed && !isAssociative) {
            // Check if indices are consecutive keys
            if (!indexedItems.isEmpty()) {
                return createArrayFromIndexedItems(indexedItems)
            }
            return jsonArray
        }
        if (isAssociative) {
            // Mixte case, then it's an object
            return jsonObject
        }

        return JsonArray()
    }

    private fun createArrayFromIndexedItems(
        indexedItems: MutableMap<Int?, JsonElement?>
    ): JsonElement {
        if (indexedItems.isEmpty()) {
            return JsonArray()
        }

        val indices: MutableList<Int> = ArrayList<Int>(indexedItems.keys)
        indices.sort()

        var isConsecutive = true
        for (i in indices.indices) {
            if (indices[i] != i) {
                isConsecutive = false
                break
            }
        }

        if (isConsecutive) {
            val jsonArray = JsonArray()
            for (i in indices.indices) {
                jsonArray.add(indexedItems[i])
            }
            return jsonArray
        }

        val jsonObject = JsonObject()
        for (entry in indexedItems.entries) {
            jsonObject.add(entry.key.toString(), entry.value)
        }
        return jsonObject
    }

    private fun splitObjectProperties(content: String): MutableList<String> {
        val properties: MutableList<String> = ArrayList<String>()
        var current = StringBuilder()
        var depth = 0
        var inString = false
        var escapeNext = false

        for (i in 0..<content.length) {
            val ch = content[i]

            if (escapeNext) {
                current.append(ch)
                escapeNext = false
                continue
            }

            if (ch == '\\') {
                escapeNext = true
                current.append(ch)
                continue
            }

            if (ch == '"') {
                inString = !inString
                current.append(ch)
                continue
            }

            if (!inString) {
                if (ch == '[' || ch == '{') {
                    depth++
                } else if (ch == ']' || ch == '}') {
                    depth--
                }

                // if we found a new line and we are at 0 level, then it's a new property
                if (ch == '\n' && depth == 0) {
                    val trimmed = current.toString().trim { it <= ' ' }
                    if (!trimmed.isEmpty()) {
                        properties.add(trimmed)
                    }
                    current = StringBuilder()
                    continue
                }
            }

            current.append(ch)
        }

        // Add last item
        val trimmed = current.toString().trim { it <= ' ' }
        if (!trimmed.isEmpty()) {
            properties.add(trimmed)
        }

        return properties
    }

    private fun isNumeric(str: String): Boolean {
        try {
            str.toDouble()
            return true
        } catch (_: NumberFormatException) {
            return false
        }
    }
}
