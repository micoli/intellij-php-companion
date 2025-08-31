package org.micoli.php.configuration.documentation;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.StringJoiner;
import java.util.stream.Collectors;

public class YamlFormatter {
    public static String formatYamlField(String fieldName, Object value, Class<?> fieldType, int indentLevel) {
        StringBuilder yaml = new StringBuilder();
        String indent = "  ".repeat(indentLevel);
        String subIndent = "  ".repeat(indentLevel + 1);

        yaml.append(indent).append(fieldName).append(": ");

        if (value == null) {
            return yaml.append("null\n").toString();
        }
        if (value instanceof String stringValue) {
            if (stringValue.contains("\n"))
                return yaml.append(" |\n")
                        .append(Arrays.stream(stringValue.split("\n"))
                                .map(line -> subIndent + line)
                                .collect(Collectors.joining("\n")))
                        .append("\n")
                        .toString();
            if (stringValue.contains("\\")
                    || stringValue.contains("*")
                    || stringValue.contains(":")
                    || stringValue.contains("#")
                    || stringValue.contains("\"")
                    || stringValue.contains("'")
                    || stringValue.startsWith(" ")
                    || stringValue.endsWith(" ")) {
                return yaml.append("'")
                        .append(stringValue.replace("'", "\\'"))
                        .append("'\n")
                        .toString();
            }
            return yaml.append(stringValue).append("\n").toString();
        }
        if (value instanceof Boolean || value instanceof Number) {
            yaml.append(value).append("\n");
            return yaml.toString();
        }
        if (value instanceof String[] strings) {
            if (strings.length == 0) {
                return yaml.append("[]\n").toString();
            }
            yaml.append("\n");
            for (String item : strings) {
                yaml.append(indent).append("  - ");
                if (item.contains("\\")
                        || item.contains(":")
                        || item.contains("#")
                        || item.contains("\"")
                        || item.contains("'")
                        || item.startsWith(" ")
                        || item.endsWith(" ")) {
                    return yaml.append("\"")
                            .append(item.replace("\"", "\\\""))
                            .append("\"\n")
                            .toString();
                }
                return yaml.append(item).append("\n").toString();
            }
        }
        if (fieldType.isArray()) {
            Object[] array = (Object[]) value;
            if (array.length == 0) {
                return yaml.append("[]\n").toString();
            }
            yaml.append("\n");
            for (Object item : array) {
                if (Helper.isCustomClass(item.getClass())) {
                    return yaml.append(indent)
                            .append("  -\n")
                            .append(YamlFormatter.generateYamlForObject(item, indentLevel + 2))
                            .toString();
                }
                return yaml.append(indent)
                        .append("  - ")
                        .append(item)
                        .append("\n")
                        .toString();
            }
        }
        if (Helper.isCustomClass(value.getClass())) {
            return yaml.append("\n")
                    .append(generateYamlForObject(value, indentLevel + 1))
                    .toString();
        }
        return yaml.append(value).append("\n").toString();
    }

    public static String generateYamlForObject(Object obj, int indentLevel) {
        StringBuilder yaml = new StringBuilder();
        Class<?> clazz = obj.getClass();
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers())) {
                continue;
            }

            field.setAccessible(true);

            try {
                Object fieldValue = field.get(obj);
                if (fieldValue != null) {
                    yaml.append(formatYamlField(field.getName(), fieldValue, field.getType(), indentLevel));
                }
            } catch (Exception ignored) {
            }
        }

        return yaml.toString();
    }

    public static String formatDefaultValue(Object value) {
        switch (value) {
            case null -> {
                return "null";
            }
            case String s -> {
                return String.format("\"%s\"", value);
            }
            case String[] array -> {
                StringJoiner joiner = new StringJoiner(", ", "[", "]");
                for (String item : array) {
                    joiner.add(String.format("\"%s\"", item));
                }
                return joiner.toString();
            }
            default -> {}
        }

        if (value.getClass().isArray()) {
            return Arrays.toString((Object[]) value);
        }

        return value.toString();
    }

    private String capitalize(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }
}
