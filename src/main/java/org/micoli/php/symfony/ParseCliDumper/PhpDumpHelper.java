package org.micoli.php.symfony.ParseCliDumper;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PhpDumpHelper {

    public static String parseCliDumperToJson(String cliDumperOutput) {
        // spotless:off
        return new GsonBuilder()
            .setPrettyPrinting()
            .create()
            .toJson(
                parseValue(
                    cleanAnsiEscapeSequences(cliDumperOutput)
                )
            );
        // spotless:on
    }

    private static String cleanAnsiEscapeSequences(String input) {
        // spotless:off
        return input
            .trim()
            .replace("\\n", "")
            .replaceAll("\u001B", " ")
            .replaceAll("\\s{0,2}]8;;file:.*? ]8;; \\\\", "");
        // spotless:on
    }

    private static JsonElement parseValue(String input) {
        input = input.trim();

        // Match an array
        Pattern arrayPattern = Pattern.compile("^array:\\d+\\s*\\[\\s*(.*)?\\s*]$", Pattern.DOTALL);
        Matcher arrayMatcher = arrayPattern.matcher(input);
        if (arrayMatcher.matches()) {
            String content = arrayMatcher.group(1);
            return parseArray(content != null ? content : "");
        }

        // Match an object (App\Tests\TestDTO {#383)
        Pattern namedObjectPattern = Pattern.compile("^([A-Za-z_\\\\][A-Za-z0-9_\\\\]*?)(\\s@[A-Za-z0-9]*)?\\s*\\{#\\d+\\s*(.*?)\\s*}$", Pattern.DOTALL);
        Matcher namedObjectMatcher = namedObjectPattern.matcher(input);
        if (namedObjectMatcher.matches()) {
            String className = namedObjectMatcher.group(1);
            String content = namedObjectMatcher.group(3);
            return parseObject(className, content);
        }

        // Match an anonym class (class@anonymous {#382)
        Pattern anonymousObjectPattern = Pattern.compile("^class@anonymous\\s*\\{#\\d+\\s*(.*?)\\s*}$", Pattern.DOTALL);
        Matcher anonymousObjectMatcher = anonymousObjectPattern.matcher(input);
        if (anonymousObjectMatcher.matches()) {
            String content = anonymousObjectMatcher.group(1);
            return parseObject("@anonymous", content);
        }

        // Match a string with double quotes
        Pattern stringPattern = Pattern.compile("^\"(.*)\"$");
        Matcher stringMatcher = stringPattern.matcher(input);
        if (stringMatcher.matches()) {
            return new JsonPrimitive(stringMatcher.group(1));
        }

        // Match a number
        if (isNumeric(input)) {
            try {
                if (input.contains(".")) {
                    return new JsonPrimitive(Double.parseDouble(input));
                }
                else {
                    return new JsonPrimitive(Integer.parseInt(input));
                }
            } catch (NumberFormatException e) {
                return new JsonPrimitive(input);
            }
        }

        // Match a boolean
        return switch (input) {
        case "true" -> new JsonPrimitive(true);
        case "false" -> new JsonPrimitive(false);
        case "null" -> JsonNull.INSTANCE;
        default ->

            // Default, traited as a string
            new JsonPrimitive(input);
        };

    }

    private static JsonObject parseObject(String className, String content) {
        JsonObject jsonObject = new JsonObject();

        if (content == null || content.trim().isEmpty()) {
            return jsonObject;
        }

        List<String> properties = splitObjectProperties(content);

        for (String property : properties) {
            property = property.trim();

            // Pattern for property with visibility (+name:, -name:, #name:)
            Pattern propertyPattern = Pattern.compile("^([+\\-#])([^:]+):\\s*(.+)$", Pattern.DOTALL);
            Matcher propertyMatcher = propertyPattern.matcher(property);

            if (propertyMatcher.matches()) {
                String visibility = propertyMatcher.group(1);
                String propertyName = propertyMatcher.group(2).trim();
                String propertyValue = propertyMatcher.group(3).trim();

                jsonObject.add(propertyName, parseValue(propertyValue));
                continue;
            }

            // Ssearch pattern "key" => "value"
            Pattern keyValuePattern = Pattern.compile("^\"([^\"]*)\"\\s*=>\\s*(.+)$", Pattern.DOTALL);
            Matcher keyValueMatcher = keyValuePattern.matcher(property);
            if (keyValueMatcher.matches()) {
                String key = keyValueMatcher.group(1);
                String value = keyValueMatcher.group(2).trim();
                jsonObject.add(key, parseValue(value));

                continue;
            }

            // Handle simple property names (like "time", "date", "uuid", etc.)
            Pattern simplePropertyPattern = Pattern.compile("^([a-zA-Z_][a-zA-Z0-9_]*):\\s*(.+)$", Pattern.DOTALL);
            Matcher simplePropertyMatcher = simplePropertyPattern.matcher(property);
            if (simplePropertyMatcher.matches()) {
                String key = simplePropertyMatcher.group(1);
                String value = simplePropertyMatcher.group(2).trim();
                jsonObject.add(key, parseValue(value));
                continue;
            }

            // Property without explicit key
            jsonObject.add("property_" + jsonObject.size(), parseValue(property));
        }

        return jsonObject;
    }

    private static JsonElement parseArray(String content) {
        if (content == null || content.trim().isEmpty()) {
            return new JsonArray();
        }

        List<String> items = splitObjectProperties(content);
        JsonObject jsonObject = new JsonObject();
        JsonArray jsonArray = new JsonArray();
        boolean isAssociative = false;
        boolean isIndexed = false;
        Map<Integer, JsonElement> indexedItems = new HashMap<>();

        for (String item : items) {
            item = item.trim();

            // Search pattern "key" => "value"
            Pattern quotedKeyPattern = Pattern.compile("^\"([^\"]*)\"\\s*=>\\s*(.+)$", Pattern.DOTALL);
            Matcher quotedKeyMatcher = quotedKeyPattern.matcher(item);
            if (quotedKeyMatcher.matches()) {
                String key = quotedKeyMatcher.group(1);
                String value = quotedKeyMatcher.group(2).trim();
                jsonObject.add(key, parseValue(value));
                isAssociative = true;
                continue;
            }

            // Search pattern key => value (no quote around key)
            Pattern keyPattern = Pattern.compile("^([^\\s=>]+)\\s*=>\\s*(.+)$", Pattern.DOTALL);
            Matcher keyMatcher = keyPattern.matcher(item);
            if (keyMatcher.matches()) {
                String key = keyMatcher.group(1);
                String value = keyMatcher.group(2).trim();

                // Check if key is a numerical index
                if (isNumeric(key)) {
                    int index = Integer.parseInt(key);
                    indexedItems.put(index, parseValue(value));
                    isIndexed = true;
                }
                else {
                    jsonObject.add(key, parseValue(value));
                    isAssociative = true;
                }
                continue;
            }

            // Search pattern index => value (for indexed arrays)
            Pattern indexPattern = Pattern.compile("^(\\d+)\\s*=>\\s*(.+)$", Pattern.DOTALL);
            Matcher indexMatcher = indexPattern.matcher(item);
            if (indexMatcher.matches()) {
                int index = Integer.parseInt(indexMatcher.group(1));
                String value = indexMatcher.group(2).trim();
                indexedItems.put(index, parseValue(value));
                isIndexed = true;
                continue;
            }

            // Simple Value (for arrays without explicit keys)
            jsonArray.add(parseValue(item));
            isIndexed = true;
        }

        if (isAssociative && !isIndexed) {
            return jsonObject;
        }
        if (isIndexed && !isAssociative) {
            // Check if indices are consecutive keys
            if (!indexedItems.isEmpty()) {
                return createArrayFromIndexedItems(indexedItems);
            }
            return jsonArray;
        }
        if (isAssociative) {
            // Mixte case, then it's an object
            return jsonObject;
        }

        return new JsonArray();
    }

    private static JsonElement createArrayFromIndexedItems(Map<Integer, JsonElement> indexedItems) {
        if (indexedItems.isEmpty()) {
            return new JsonArray();
        }

        List<Integer> indices = new ArrayList<>(indexedItems.keySet());
        Collections.sort(indices);

        boolean isConsecutive = true;
        for (int i = 0; i < indices.size(); i++) {
            if (indices.get(i) != i) {
                isConsecutive = false;
                break;
            }
        }

        if (isConsecutive) {
            JsonArray jsonArray = new JsonArray();
            for (int i = 0; i < indices.size(); i++) {
                jsonArray.add(indexedItems.get(i));
            }
            return jsonArray;
        }

        JsonObject jsonObject = new JsonObject();
        for (Map.Entry<Integer, JsonElement> entry : indexedItems.entrySet()) {
            jsonObject.add(entry.getKey().toString(), entry.getValue());
        }
        return jsonObject;
    }

    private static List<String> splitObjectProperties(String content) {
        List<String> properties = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int depth = 0;
        boolean inString = false;
        boolean escapeNext = false;

        for (int i = 0; i < content.length(); i++) {
            char ch = content.charAt(i);

            if (escapeNext) {
                current.append(ch);
                escapeNext = false;
                continue;
            }

            if (ch == '\\') {
                escapeNext = true;
                current.append(ch);
                continue;
            }

            if (ch == '"') {
                inString = !inString;
                current.append(ch);
                continue;
            }

            if (!inString) {
                if (ch == '[' || ch == '{') {
                    depth++;
                }
                else if (ch == ']' || ch == '}') {
                    depth--;
                }

                // if we found a new line and we are at 0 level, then it's a new property
                if (ch == '\n' && depth == 0) {
                    String trimmed = current.toString().trim();
                    if (!trimmed.isEmpty()) {
                        properties.add(trimmed);
                    }
                    current = new StringBuilder();
                    continue;
                }
            }

            current.append(ch);
        }

        // Add last item
        String trimmed = current.toString().trim();
        if (!trimmed.isEmpty()) {
            properties.add(trimmed);
        }

        return properties;
    }

    private static boolean isNumeric(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
