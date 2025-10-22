package org.micoli.php.symfony.cliDumpParser

import com.google.gson.*
import kotlin.math.max

object JsonToPhpArrayConverter {
    private val gson = Gson()

    @JvmStatic
    fun convertJsonToPhp(jsonString: String?): String {
        val jsonElement = gson.fromJson(jsonString, JsonElement::class.java)
        return convertJsonElementToPhp(jsonElement, 0)
    }

    private fun convertJsonElementToPhp(element: JsonElement?, indentLevel: Int): String {
        if (element == null || element.isJsonNull) {
            return "null"
        }

        if (element.isJsonPrimitive) {
            return convertPrimitiveToPhp(element.asJsonPrimitive)
        }

        if (element.isJsonArray) {
            return convertArrayToPhp(element.asJsonArray, indentLevel)
        }

        if (element.isJsonObject) {
            return convertObjectToPhp(element.asJsonObject, indentLevel)
        }

        return "null"
    }

    private fun convertPrimitiveToPhp(primitive: JsonPrimitive): String {
        if (primitive.isBoolean) {
            return if (primitive.asBoolean) "true" else "false"
        }

        if (primitive.isNumber) {
            // Preserve integer vs float distinction
            val numberStr = primitive.asString
            return if (numberStr.contains(".")) primitive.asDouble.toString()
            else primitive.asLong.toString()
        }

        if (primitive.isString) {
            return escapePhpString(primitive.asString)
        }

        return "null"
    }

    private fun convertArrayToPhp(array: JsonArray, indentLevel: Int): String {
        if (array.isEmpty) {
            return "[]"
        }

        val sb = StringBuilder()
        val indent = getCustomIndent(indentLevel, " ")
        val nextIndent = getCustomIndent(indentLevel + 1, " ")

        sb.append("[\n")

        for (i in 0..<array.size()) {
            val element = array.get(i)
            sb.append(nextIndent)
            sb.append(convertJsonElementToPhp(element, indentLevel + 1))

            if (i < array.size() - 1) {
                sb.append(",")
            }
            sb.append("\n")
        }

        sb.append(indent).append("]")
        return sb.toString()
    }

    private fun convertObjectToPhp(`object`: JsonObject, indentLevel: Int): String {
        if (`object`.isEmpty) {
            return "[]"
        }

        val sb = StringBuilder()
        val indent = getCustomIndent(indentLevel, " ")
        val nextIndent = getCustomIndent(indentLevel + 1, " ")

        sb.append("[\n")

        var count = 0
        for (entry in `object`.entrySet()) {
            val key = entry.key
            val value = entry.value

            sb.append(nextIndent)

            // Check if key is numeric (for array-like objects)
            if (isNumericKey(key)) {
                sb.append(key)
            } else {
                sb.append(escapePhpString(key))
            }

            sb.append(" => ")
            sb.append(convertJsonElementToPhp(value, indentLevel + 1))

            if (count < `object`.size() - 1) {
                sb.append(",")
            }
            sb.append("\n")
            count++
        }

        sb.append(indent).append("]")
        return sb.toString()
    }

    private fun escapePhpString(str: String?): String {
        if (str == null) {
            return "null"
        }

        // Use single quotes for PHP strings and escape single quotes
        return ("'" +
            str.replace("\\", "\\\\")
                .replace("'", "\\'")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t") +
            "'")
    }

    private fun isNumericKey(key: String): Boolean {
        try {
            key.toInt()
            return true
        } catch (_: NumberFormatException) {
            return false
        }
    }

    fun convertJsonToPhp(jsonString: String, indentString: String): String {
        val jsonElement = gson.fromJson(jsonString, JsonElement::class.java)
        return convertJsonElementToPhpWithCustomIndent(jsonElement, 0, indentString)
    }

    private fun convertJsonElementToPhpWithCustomIndent(
        element: JsonElement?,
        indentLevel: Int,
        indentString: String
    ): String {
        if (element == null || element.isJsonNull) {
            return "null"
        }

        if (element.isJsonPrimitive) {
            return convertPrimitiveToPhp(element.asJsonPrimitive)
        }

        if (element.isJsonArray) {
            return convertArrayToPhpWithCustomIndent(element.asJsonArray, indentLevel, indentString)
        }

        if (element.isJsonObject) {
            return convertObjectToPhpWithCustomIndent(
                element.asJsonObject, indentLevel, indentString)
        }

        return "null"
    }

    private fun convertArrayToPhpWithCustomIndent(
        array: JsonArray,
        indentLevel: Int,
        indentString: String
    ): String {
        if (array.isEmpty) {
            return "[]"
        }

        val sb = StringBuilder()
        val indent = getCustomIndent(indentLevel, indentString)
        val nextIndent = getCustomIndent(indentLevel + 1, indentString)

        sb.append("[\n")

        for (i in 0..<array.size()) {
            val element = array.get(i)
            sb.append(nextIndent)
            sb.append(
                convertJsonElementToPhpWithCustomIndent(element, indentLevel + 1, indentString))

            if (i < array.size() - 1) {
                sb.append(",")
            }
            sb.append("\n")
        }

        sb.append(indent).append("]")
        return sb.toString()
    }

    private fun convertObjectToPhpWithCustomIndent(
        jsonObject: JsonObject,
        indentLevel: Int,
        indentString: String
    ): String {
        if (jsonObject.isEmpty) {
            return "[]"
        }

        val sb = StringBuilder()
        val indent = getCustomIndent(indentLevel, indentString)
        val nextIndent = getCustomIndent(indentLevel + 1, indentString)

        sb.append("[\n")

        var count = 0
        for (entry in jsonObject.entrySet()) {
            val key = entry.key
            val value = entry.value

            sb.append(nextIndent)

            if (isNumericKey(key)) {
                sb.append(key)
            } else {
                sb.append(escapePhpString(key))
            }

            sb.append(" => ")
            sb.append(convertJsonElementToPhpWithCustomIndent(value, indentLevel + 1, indentString))

            if (count < jsonObject.size() - 1) {
                sb.append(",")
            }
            sb.append("\n")
            count++
        }

        sb.append(indent).append("]")
        return sb.toString()
    }

    private fun getCustomIndent(level: Int, indentString: String): String {
        return indentString.repeat(max(0, level))
    }

    fun convertJsonToPhpOneLine(jsonString: String?): String {
        val jsonElement = gson.fromJson(jsonString, JsonElement::class.java)
        return convertJsonElementToPhpOneLine(jsonElement)
    }

    private fun convertJsonElementToPhpOneLine(element: JsonElement?): String {
        if (element == null || element.isJsonNull) {
            return "null"
        }

        if (element.isJsonPrimitive) {
            return convertPrimitiveToPhp(element.asJsonPrimitive)
        }

        if (element.isJsonArray) {
            return convertArrayToPhpOneLine(element.asJsonArray)
        }

        if (element.isJsonObject) {
            return convertObjectToPhpOneLine(element.asJsonObject)
        }

        return "null"
    }

    private fun convertArrayToPhpOneLine(array: JsonArray): String {
        if (array.isEmpty) {
            return "[]"
        }

        val sb = StringBuilder()
        sb.append("[")

        for (i in 0..<array.size()) {
            val element = array.get(i)
            sb.append(convertJsonElementToPhpOneLine(element))

            if (i < array.size() - 1) {
                sb.append(", ")
            }
        }

        sb.append("]")
        return sb.toString()
    }

    private fun convertObjectToPhpOneLine(jsonObject: JsonObject): String {
        if (jsonObject.isEmpty) {
            return "[]"
        }

        val sb = StringBuilder()
        sb.append("[")

        var count = 0
        for (entry in jsonObject.entrySet()) {
            val key = entry.key
            val value = entry.value

            if (isNumericKey(key)) {
                sb.append(key)
            } else {
                sb.append(escapePhpString(key))
            }

            sb.append(" => ")
            sb.append(convertJsonElementToPhpOneLine(value))

            if (count < jsonObject.size() - 1) {
                sb.append(", ")
            }
            count++
        }

        sb.append("]")
        return sb.toString()
    }
}
