package org.micoli.php;

import com.google.gson.*;
import java.util.*;

public class JsonAssertUtils {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static void assertJsonEquals(String expected, String actual) {
        JsonElement expectedElement = JsonParser.parseString(expected);
        JsonElement actualElement = JsonParser.parseString(actual);

        if (!jsonEquals(expectedElement, actualElement)) {

            String message = String.format(
                    "JSON strings are not equal:\nExpected:\n%s\n\nActual:\n%s",
                    gson.toJson(expectedElement), gson.toJson(actualElement));

            throw new AssertionError(message);
        }
    }

    public static void assertJsonEquals(String message, String expected, String actual) {
        try {
            assertJsonEquals(expected, actual);
        } catch (AssertionError e) {
            throw new AssertionError(message + "\n" + e.getMessage());
        }
    }

    private static boolean jsonEquals(JsonElement expected, JsonElement actual) {
        if (expected == null && actual == null) return true;
        if (expected == null || actual == null) return false;

        if (expected.isJsonNull() && actual.isJsonNull()) return true;
        if (expected.isJsonNull() || actual.isJsonNull()) return false;

        if (expected.isJsonPrimitive() && actual.isJsonPrimitive()) {
            return expected.getAsJsonPrimitive().equals(actual.getAsJsonPrimitive());
        }

        if (expected.isJsonObject() && actual.isJsonObject()) {
            return jsonObjectEquals(expected.getAsJsonObject(), actual.getAsJsonObject());
        }

        if (expected.isJsonArray() && actual.isJsonArray()) {
            return jsonArrayEquals(expected.getAsJsonArray(), actual.getAsJsonArray());
        }

        return false;
    }

    private static boolean jsonObjectEquals(JsonObject expected, JsonObject actual) {
        if (expected.size() != actual.size()) return false;

        for (Map.Entry<String, JsonElement> entry : expected.entrySet()) {
            String key = entry.getKey();
            JsonElement expectedValue = entry.getValue();
            JsonElement actualValue = actual.get(key);

            if (!jsonEquals(expectedValue, actualValue)) {
                return false;
            }
        }

        return true;
    }

    private static boolean jsonArrayEquals(JsonArray expected, JsonArray actual) {
        if (expected.size() != actual.size()) return false;

        List<JsonElement> expectedList = new ArrayList<>();
        List<JsonElement> actualList = new ArrayList<>();

        for (JsonElement element : expected) {
            expectedList.add(element);
        }

        for (JsonElement element : actual) {
            actualList.add(element);
        }

        for (JsonElement expectedElement : expectedList) {
            boolean found = false;
            for (int i = 0; i < actualList.size(); i++) {
                if (jsonEquals(expectedElement, actualList.get(i))) {
                    actualList.remove(i);
                    found = true;
                    break;
                }
            }
            if (!found) return false;
        }

        return actualList.isEmpty();
    }

    public static void assertJsonEqualsOrdered(String expected, String actual) {
        JsonElement expectedElement = JsonParser.parseString(expected);
        JsonElement actualElement = JsonParser.parseString(actual);

        if (!jsonEqualsOrdered(expectedElement, actualElement)) {
            String message = String.format(
                    "JSON strings are not equal (ordered):\nExpected:\n%s\n\nActual:\n%s",
                    gson.toJson(expectedElement), gson.toJson(actualElement));
            throw new AssertionError(message);
        }
    }

    private static boolean jsonEqualsOrdered(JsonElement expected, JsonElement actual) {
        if (expected == null && actual == null) return true;
        if (expected == null || actual == null) return false;

        if (expected.isJsonNull() && actual.isJsonNull()) return true;
        if (expected.isJsonNull() || actual.isJsonNull()) return false;

        if (expected.isJsonPrimitive() && actual.isJsonPrimitive()) {
            return expected.getAsJsonPrimitive().equals(actual.getAsJsonPrimitive());
        }

        if (expected.isJsonObject() && actual.isJsonObject()) {
            return jsonObjectEquals(expected.getAsJsonObject(), actual.getAsJsonObject());
        }

        if (expected.isJsonArray() && actual.isJsonArray()) {
            return jsonArrayEqualsOrdered(expected.getAsJsonArray(), actual.getAsJsonArray());
        }

        return false;
    }

    private static boolean jsonArrayEqualsOrdered(JsonArray expected, JsonArray actual) {
        if (expected.size() != actual.size()) return false;

        for (int i = 0; i < expected.size(); i++) {
            if (!jsonEqualsOrdered(expected.get(i), actual.get(i))) {
                return false;
            }
        }

        return true;
    }

    public static String normalizeJson(String json) {
        JsonElement element = JsonParser.parseString(json);
        return gson.toJson(element);
    }

    public static boolean isJsonEqual(String json1, String json2) {
        try {
            JsonElement element1 = JsonParser.parseString(json1);
            JsonElement element2 = JsonParser.parseString(json2);
            return jsonEquals(element1, element2);
        } catch (Exception e) {
            return false;
        }
    }
}
