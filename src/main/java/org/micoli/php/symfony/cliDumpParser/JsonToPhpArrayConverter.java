package org.micoli.php.symfony.cliDumpParser;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.util.Map;

public class JsonToPhpArrayConverter {

    private static final Gson gson = new Gson();

    public static String convertJsonToPhp(String jsonString) {
        JsonElement jsonElement = gson.fromJson(jsonString, JsonElement.class);
        return convertJsonElementToPhp(jsonElement, 0);
    }

    private static String convertJsonElementToPhp(JsonElement element, int indentLevel) {
        if (element == null || element.isJsonNull()) {
            return "null";
        }

        if (element.isJsonPrimitive()) {
            return convertPrimitiveToPhp(element.getAsJsonPrimitive());
        }

        if (element.isJsonArray()) {
            return convertArrayToPhp(element.getAsJsonArray(), indentLevel);
        }

        if (element.isJsonObject()) {
            return convertObjectToPhp(element.getAsJsonObject(), indentLevel);
        }

        return "null";
    }

    private static String convertPrimitiveToPhp(JsonPrimitive primitive) {
        if (primitive.isBoolean()) {
            return primitive.getAsBoolean() ? "true" : "false";
        }

        if (primitive.isNumber()) {
            // Preserve integer vs float distinction
            String numberStr = primitive.getAsString();
            if (numberStr.contains(".")) {
                return String.valueOf(primitive.getAsDouble());
            } else {
                return String.valueOf(primitive.getAsLong());
            }
        }

        if (primitive.isString()) {
            return escapePhpString(primitive.getAsString());
        }

        return "null";
    }

    private static String convertArrayToPhp(JsonArray array, int indentLevel) {
        if (array.isEmpty()) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder();
        String indent = getCustomIndent(indentLevel, " ");
        String nextIndent = getCustomIndent(indentLevel + 1, " ");

        sb.append("[\n");

        for (int i = 0; i < array.size(); i++) {
            JsonElement element = array.get(i);
            sb.append(nextIndent);
            sb.append(convertJsonElementToPhp(element, indentLevel + 1));

            if (i < array.size() - 1) {
                sb.append(",");
            }
            sb.append("\n");
        }

        sb.append(indent).append("]");
        return sb.toString();
    }

    private static String convertObjectToPhp(JsonObject object, int indentLevel) {
        if (object.isEmpty()) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder();
        String indent = getCustomIndent(indentLevel, " ");
        String nextIndent = getCustomIndent(indentLevel + 1, " ");

        sb.append("[\n");

        int count = 0;
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            String key = entry.getKey();
            JsonElement value = entry.getValue();

            sb.append(nextIndent);

            // Check if key is numeric (for array-like objects)
            if (isNumericKey(key)) {
                sb.append(key);
            } else {
                sb.append(escapePhpString(key));
            }

            sb.append(" => ");
            sb.append(convertJsonElementToPhp(value, indentLevel + 1));

            if (count < object.size() - 1) {
                sb.append(",");
            }
            sb.append("\n");
            count++;
        }

        sb.append(indent).append("]");
        return sb.toString();
    }

    private static String escapePhpString(String str) {
        if (str == null) {
            return "null";
        }

        // Use single quotes for PHP strings and escape single quotes
        return "'"
                + str.replace("\\", "\\\\")
                        .replace("'", "\\'")
                        .replace("\n", "\\n")
                        .replace("\r", "\\r")
                        .replace("\t", "\\t")
                + "'";
    }

    private static boolean isNumericKey(String key) {
        try {
            Integer.parseInt(key);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static String convertJsonToPhp(String jsonString, String indentString) {
        JsonElement jsonElement = gson.fromJson(jsonString, JsonElement.class);
        return convertJsonElementToPhpWithCustomIndent(jsonElement, 0, indentString);
    }

    private static String convertJsonElementToPhpWithCustomIndent(
            JsonElement element, int indentLevel, String indentString) {
        if (element == null || element.isJsonNull()) {
            return "null";
        }

        if (element.isJsonPrimitive()) {
            return convertPrimitiveToPhp(element.getAsJsonPrimitive());
        }

        if (element.isJsonArray()) {
            return convertArrayToPhpWithCustomIndent(element.getAsJsonArray(), indentLevel, indentString);
        }

        if (element.isJsonObject()) {
            return convertObjectToPhpWithCustomIndent(element.getAsJsonObject(), indentLevel, indentString);
        }

        return "null";
    }

    private static String convertArrayToPhpWithCustomIndent(JsonArray array, int indentLevel, String indentString) {
        if (array.isEmpty()) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder();
        String indent = getCustomIndent(indentLevel, indentString);
        String nextIndent = getCustomIndent(indentLevel + 1, indentString);

        sb.append("[\n");

        for (int i = 0; i < array.size(); i++) {
            JsonElement element = array.get(i);
            sb.append(nextIndent);
            sb.append(convertJsonElementToPhpWithCustomIndent(element, indentLevel + 1, indentString));

            if (i < array.size() - 1) {
                sb.append(",");
            }
            sb.append("\n");
        }

        sb.append(indent).append("]");
        return sb.toString();
    }

    private static String convertObjectToPhpWithCustomIndent(JsonObject object, int indentLevel, String indentString) {
        if (object.isEmpty()) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder();
        String indent = getCustomIndent(indentLevel, indentString);
        String nextIndent = getCustomIndent(indentLevel + 1, indentString);

        sb.append("[\n");

        int count = 0;
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            String key = entry.getKey();
            JsonElement value = entry.getValue();

            sb.append(nextIndent);

            if (isNumericKey(key)) {
                sb.append(key);
            } else {
                sb.append(escapePhpString(key));
            }

            sb.append(" => ");
            sb.append(convertJsonElementToPhpWithCustomIndent(value, indentLevel + 1, indentString));

            if (count < object.size() - 1) {
                sb.append(",");
            }
            sb.append("\n");
            count++;
        }

        sb.append(indent).append("]");
        return sb.toString();
    }

    private static String getCustomIndent(int level, String indentString) {
        return String.valueOf(indentString).repeat(Math.max(0, level));
    }

    // Utility method for one-line output (no formatting)
    public static String convertJsonToPhpOneLine(String jsonString) {
        JsonElement jsonElement = gson.fromJson(jsonString, JsonElement.class);
        return convertJsonElementToPhpOneLine(jsonElement);
    }

    private static String convertJsonElementToPhpOneLine(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return "null";
        }

        if (element.isJsonPrimitive()) {
            return convertPrimitiveToPhp(element.getAsJsonPrimitive());
        }

        if (element.isJsonArray()) {
            return convertArrayToPhpOneLine(element.getAsJsonArray());
        }

        if (element.isJsonObject()) {
            return convertObjectToPhpOneLine(element.getAsJsonObject());
        }

        return "null";
    }

    private static String convertArrayToPhpOneLine(JsonArray array) {
        if (array.isEmpty()) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("[");

        for (int i = 0; i < array.size(); i++) {
            JsonElement element = array.get(i);
            sb.append(convertJsonElementToPhpOneLine(element));

            if (i < array.size() - 1) {
                sb.append(", ");
            }
        }

        sb.append("]");
        return sb.toString();
    }

    private static String convertObjectToPhpOneLine(JsonObject object) {
        if (object.isEmpty()) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("[");

        int count = 0;
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            String key = entry.getKey();
            JsonElement value = entry.getValue();

            if (isNumericKey(key)) {
                sb.append(key);
            } else {
                sb.append(escapePhpString(key));
            }

            sb.append(" => ");
            sb.append(convertJsonElementToPhpOneLine(value));

            if (count < object.size() - 1) {
                sb.append(", ");
            }
            count++;
        }

        sb.append("]");
        return sb.toString();
    }
}
