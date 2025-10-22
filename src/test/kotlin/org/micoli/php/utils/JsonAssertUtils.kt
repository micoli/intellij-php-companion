package org.micoli.php.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser

object JsonAssertUtils {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    @JvmStatic
    fun assertJsonEquals(expected: String, actual: String) {
        val expectedElement = JsonParser.parseString(expected)
        val actualElement = JsonParser.parseString(actual)

        if (!jsonEquals(expectedElement, actualElement)) {
            val message =
                String.format(
                    "JSON strings are not equal:\nExpected:\n%s\n\nActual:\n%s",
                    gson.toJson(expectedElement),
                    gson.toJson(actualElement),
                )

            throw AssertionError(message)
        }
    }

    private fun jsonEquals(expected: JsonElement?, actual: JsonElement?): Boolean {
        if (expected == null && actual == null) return true
        if (expected == null || actual == null) return false

        if (expected.isJsonNull && actual.isJsonNull) return true
        if (expected.isJsonNull || actual.isJsonNull) return false

        if (expected.isJsonPrimitive && actual.isJsonPrimitive) {
            return expected.asJsonPrimitive == actual.asJsonPrimitive
        }

        if (expected.isJsonObject && actual.isJsonObject) {
            return jsonObjectEquals(expected.asJsonObject, actual.asJsonObject)
        }

        if (expected.isJsonArray && actual.isJsonArray) {
            return jsonArrayEquals(expected.asJsonArray, actual.asJsonArray)
        }

        return false
    }

    private fun jsonObjectEquals(expected: JsonObject, actual: JsonObject): Boolean {
        if (expected.size() != actual.size()) return false

        for (entry in expected.entrySet()) {
            val key = entry.key
            val expectedValue = entry.value
            val actualValue = actual.get(key)

            if (!jsonEquals(expectedValue, actualValue)) {
                return false
            }
        }

        return true
    }

    private fun jsonArrayEquals(expected: JsonArray, actual: JsonArray): Boolean {
        if (expected.size() != actual.size()) return false

        val expectedList: MutableList<JsonElement?> = ArrayList()
        val actualList: MutableList<JsonElement?> = ArrayList()

        for (element in expected) {
            expectedList.add(element)
        }

        for (element in actual) {
            actualList.add(element)
        }

        for (expectedElement in expectedList) {
            var found = false
            for (i in actualList.indices) {
                if (jsonEquals(expectedElement, actualList[i])) {
                    actualList.removeAt(i)
                    found = true
                    break
                }
            }
            if (!found) return false
        }

        return actualList.isEmpty()
    }

    private fun jsonEqualsOrdered(expected: JsonElement?, actual: JsonElement?): Boolean {
        if (expected == null && actual == null) return true
        if (expected == null || actual == null) return false

        if (expected.isJsonNull && actual.isJsonNull) return true
        if (expected.isJsonNull || actual.isJsonNull) return false

        if (expected.isJsonPrimitive && actual.isJsonPrimitive) {
            return expected.asJsonPrimitive == actual.asJsonPrimitive
        }

        if (expected.isJsonObject && actual.isJsonObject) {
            return jsonObjectEquals(expected.asJsonObject, actual.asJsonObject)
        }

        if (expected.isJsonArray && actual.isJsonArray) {
            return jsonArrayEqualsOrdered(expected.asJsonArray, actual.asJsonArray)
        }

        return false
    }

    private fun jsonArrayEqualsOrdered(expected: JsonArray, actual: JsonArray): Boolean {
        if (expected.size() != actual.size()) return false

        for (i in 0..<expected.size()) {
            if (!jsonEqualsOrdered(expected.get(i), actual.get(i))) {
                return false
            }
        }

        return true
    }
}
