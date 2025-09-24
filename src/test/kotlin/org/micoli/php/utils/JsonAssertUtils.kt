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

    fun assertJsonEquals(message: String?, expected: String, actual: String) {
        try {
            assertJsonEquals(expected, actual)
        } catch (e: AssertionError) {
            throw AssertionError(message + "\n" + e.message)
        }
    }

    private fun jsonEquals(expected: JsonElement?, actual: JsonElement?): Boolean {
        if (expected == null && actual == null) return true
        if (expected == null || actual == null) return false

        if (expected.isJsonNull() && actual.isJsonNull()) return true
        if (expected.isJsonNull() || actual.isJsonNull()) return false

        if (expected.isJsonPrimitive() && actual.isJsonPrimitive()) {
            return expected.getAsJsonPrimitive() == actual.getAsJsonPrimitive()
        }

        if (expected.isJsonObject() && actual.isJsonObject()) {
            return jsonObjectEquals(expected.getAsJsonObject(), actual.getAsJsonObject())
        }

        if (expected.isJsonArray() && actual.isJsonArray()) {
            return jsonArrayEquals(expected.getAsJsonArray(), actual.getAsJsonArray())
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

        val expectedList: MutableList<JsonElement?> = ArrayList<JsonElement?>()
        val actualList: MutableList<JsonElement?> = ArrayList<JsonElement?>()

        for (element in expected) {
            expectedList.add(element)
        }

        for (element in actual) {
            actualList.add(element)
        }

        for (expectedElement in expectedList) {
            var found = false
            for (i in actualList.indices) {
                if (jsonEquals(expectedElement, actualList.get(i))) {
                    actualList.removeAt(i)
                    found = true
                    break
                }
            }
            if (!found) return false
        }

        return actualList.isEmpty()
    }

    fun assertJsonEqualsOrdered(expected: String, actual: String) {
        val expectedElement = JsonParser.parseString(expected)
        val actualElement = JsonParser.parseString(actual)

        if (!jsonEqualsOrdered(expectedElement, actualElement)) {
            val message =
                String.format(
                    "JSON strings are not equal (ordered):\nExpected:\n%s\n\nActual:\n%s",
                    gson.toJson(expectedElement),
                    gson.toJson(actualElement),
                )
            throw AssertionError(message)
        }
    }

    private fun jsonEqualsOrdered(expected: JsonElement?, actual: JsonElement?): Boolean {
        if (expected == null && actual == null) return true
        if (expected == null || actual == null) return false

        if (expected.isJsonNull() && actual.isJsonNull()) return true
        if (expected.isJsonNull() || actual.isJsonNull()) return false

        if (expected.isJsonPrimitive() && actual.isJsonPrimitive()) {
            return expected.getAsJsonPrimitive() == actual.getAsJsonPrimitive()
        }

        if (expected.isJsonObject() && actual.isJsonObject()) {
            return jsonObjectEquals(expected.getAsJsonObject(), actual.getAsJsonObject())
        }

        if (expected.isJsonArray() && actual.isJsonArray()) {
            return jsonArrayEqualsOrdered(expected.getAsJsonArray(), actual.getAsJsonArray())
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

    fun normalizeJson(json: String): String? {
        val element = JsonParser.parseString(json)
        return gson.toJson(element)
    }

    fun isJsonEqual(json1: String, json2: String): Boolean {
        try {
            val element1 = JsonParser.parseString(json1)
            val element2 = JsonParser.parseString(json2)
            return jsonEquals(element1, element2)
        } catch (_: Exception) {
            return false
        }
    }
}
