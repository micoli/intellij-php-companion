package org.micoli.php.configuration.documentation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import io.swagger.v3.oas.annotations.media.Schema;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;
import net.steppschuh.markdowngenerator.table.Table;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

public class MarkdownSchemaGenerator {

    private final Set<Class<?>> processedClasses = new HashSet<>();

    public String generateMarkdownDocumentation(
            DocumentationType type, Class<?> clazz, int maxDepth, String exampleRoot) {
        processedClasses.clear();
        return generateMarkdownDocumentationInternal(type, clazz, new LinkedHashSet<>(), maxDepth - 1, exampleRoot);
    }

    private String generateMarkdownDocumentationInternal(
            DocumentationType type,
            Class<?> clazz,
            Set<Class<?>> subClassesToDocument,
            int maxDepth,
            String exampleRoot) {
        if (processedClasses.contains(clazz)) {
            return "";
        }
        processedClasses.add(clazz);

        StringBuilder markdown = new StringBuilder();

        if (type == DocumentationType.DESCRIPTION || type == DocumentationType.FULL) {
            Schema classSchema = clazz.getAnnotation(Schema.class);
            if (classSchema != null && !classSchema.description().isEmpty()) {
                markdown.append(classSchema.description());
            }
        }
        if (type == DocumentationType.PROPERTIES || type == DocumentationType.FULL) {
            markdown.append(getYamlProperties(clazz));
        }
        if (type == DocumentationType.EXAMPLE || type == DocumentationType.FULL) {
            markdown.append("```yaml\n")
                    .append(generateYamlExample(exampleRoot, clazz, subClassesToDocument)
                            .replaceAll("```", "````"))
                    .append("\n```");
        }

        //        if (maxDepth > 0) {
        //            for (Class<?> subClass : subClassesToDocument) {
        //                if (!processedClasses.contains(subClass)) {
        //                    markdown.append("---\n\n")
        //                            .append(generateMarkdownDocumentationInternal(
        //                                    subClass, new LinkedHashSet<>(), maxDepth - 1));
        //                }
        //            }
        //        }

        return markdown.toString();
    }

    private String getYamlProperties(Class<?> clazz) {
        ConfigurationGenerator configurationGenerator = new ConfigurationGenerator();
        Object example = configurationGenerator.generateConfiguration(clazz, false);
        Table.Builder tableBuilder = new Table.Builder().addRow("Property", "Description", "Example", "Default value");

        ClassPropertiesDocumentationGenerator classPropertyTraverser = new ClassPropertiesDocumentationGenerator();
        List<ClassPropertiesDocumentationGenerator.PropertyInfo> fields =
                classPropertyTraverser.getProperties(example, 5);
        for (ClassPropertiesDocumentationGenerator.PropertyInfo property : fields) {
            tableBuilder.addRow(
                    property.dotNotationPath(),
                    (property.description() == null || property.description().isEmpty()) ? "" : property.description(),
                    (property.example() == null || property.example().isEmpty()) ? "" : "`" + property.example() + "`",
                    (property.defaultValue() == null || property.defaultValue().isEmpty())
                            ? ""
                            : "`" + property.defaultValue().replaceAll("\n", "\\\\n"));
        }
        return tableBuilder.build().serialize();
    }
    //    private String getYamlProperties(Class<?> clazz, Set<Class<?>> subClassesToDocument) {
    //        StringBuilder markdown = new StringBuilder();
    //        markdown.append("## Properties\n\n")
    //                .append("| Property  | Description | Example | Default value |\n")
    //                .append("|-----------|-------------|---------|------------------|\n");
    //
    //        Field[] fields = clazz.getDeclaredFields();
    //        for (Field field : fields) {
    //            if (Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers())) {
    //                continue;
    //            }
    //
    //            field.setAccessible(true);
    //
    //            String description = "";
    //            String example = "";
    //            String defaultValue = "";
    //
    //            Schema fieldSchema = field.getAnnotation(Schema.class);
    //            if (fieldSchema != null) {
    //                description = fieldSchema.description();
    //                example = fieldSchema.example();
    //            }
    //
    //            try {
    //                defaultValue = YamlFormatter.formatDefaultValue(
    //                        field.get(clazz.getDeclaredConstructor().newInstance()));
    //            } catch (Exception e) {
    //                defaultValue = "N/A";
    //            }
    //
    //            markdown.append("| ")
    //                    .append(field.getName())
    //                    .append(" | ")
    //                    .append(description.isEmpty() ? "-" : description)
    //                    .append(" | ")
    //                    .append(example.isEmpty() ? "-" : "`" + example + "`")
    //                    .append(" | ")
    //                    .append(defaultValue.isEmpty() ? "-" : "`" + defaultValue + "`")
    //                    .append(" |\n");
    //
    //            Class<?> fieldClass = field.getType();
    //            if (fieldClass.isArray()) {
    //                fieldClass = fieldClass.getComponentType();
    //            }
    //
    //            if (Helper.isCustomClass(fieldClass) && !processedClasses.contains(fieldClass)) {
    //                subClassesToDocument.add(fieldClass);
    //            }
    //        }
    //        return markdown.toString();
    //    }
    private static String convertJsonObjectToYaml(JsonObject jsonObject) {
        try {
            String jsonString = jsonObject.toString();

            ObjectMapper jsonMapper = new ObjectMapper();
            JsonNode jsonNode = jsonMapper.readTree(jsonString);

            YAMLMapper yamlMapper = new YAMLMapper();
            return yamlMapper.writeValueAsString(jsonNode);

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la conversion JSON vers YAML", e);
        }
    }

    private String generateYamlExample(String exampleRoot, Class<?> clazz, Set<Class<?>> subClassesToDocument) {
        ConfigurationGenerator configurationGenerator = new ConfigurationGenerator();
        Object example = configurationGenerator.generateConfiguration(clazz, true);
        DumperOptions options = new DumperOptions();
        options.setExplicitStart(false);
        options.setExplicitEnd(false);
        options.setCanonical(false);
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(options);
        String yamlContent = yaml.dump(example).replaceAll("!!\\S+\\s*", "");
        if (exampleRoot == null || exampleRoot.isEmpty()) {
            return yamlContent;
        }

        return String.format(
                "%s:\n%s",
                exampleRoot,
                Arrays.stream(yamlContent.split("\n")).map(str -> "  " + str).collect(Collectors.joining("\n")));
    }

    private String generateYamlExampleOld(Class<?> clazz, Set<Class<?>> subClassesToDocument) {
        JsonObject json = new JsonObject();
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers())) {
                continue;
            }

            field.setAccessible(true);
            String fieldName = field.getName();

            Schema fieldSchema = field.getAnnotation(Schema.class);
            JsonElement valueToShow = getFieldValueToShow(clazz, subClassesToDocument, field, fieldSchema);
            if (valueToShow == null) {
                continue;
            }

            json.add(fieldName, valueToShow);
        }

        return convertJsonObjectToYaml(json);
    }

    private JsonElement getFieldValueToShow(
            Class<?> clazz, Set<Class<?>> subClassesToDocument, Field field, Schema fieldSchema) {
        if (fieldSchema != null && !fieldSchema.example().isEmpty()) {
            return parseExampleAnnotation(fieldSchema.example(), field.getType());
        }
        if (Helper.isCustomClass(field.getType())) {
            try {
                if (field.getType().isArray()) {
                    Class<?> componentType = field.getType().getComponentType();
                    if (Helper.isCustomClass(componentType)) {
                        JsonArray arrResult = new JsonArray();
                        arrResult.add(createExampleInstance(componentType));
                        subClassesToDocument.add(componentType);
                        return arrResult;
                    }
                }
                subClassesToDocument.add(field.getType());
                return createExampleInstance(field.getType());
            } catch (Exception ex) {
                return null;
            }
        }
        try {
            return createExampleInstance(clazz);
        } catch (Exception e) {
            return null;
        }
    }

    private JsonElement createExampleInstance(Class<?> clazz) throws Exception {
        Object instance = clazz.getDeclaredConstructor().newInstance();
        JsonObject jsonObject = new JsonObject();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers()) || Modifier.isTransient(field.getModifiers())) {
                continue;
            }

            field.setAccessible(true);
            Schema fieldSchema = field.getAnnotation(Schema.class);

            if (fieldSchema != null && !fieldSchema.example().isEmpty()) {
                jsonObject.add(field.getName(), parseExampleAnnotation(fieldSchema.example(), field.getType()));
            } else if (Helper.isCustomClass(field.getType())) {
                if (field.getType().isArray()) {
                    Class<?> componentType = field.getType().getComponentType();
                    if (Helper.isCustomClass(componentType)) {
                        JsonArray array = new JsonArray();
                        array.add(createExampleInstance(componentType));
                        jsonObject.add(field.getName(), createExampleInstance(field.getType()));
                    }
                } else {
                    jsonObject.add(field.getName(), createExampleInstance(field.getType()));
                }
            }
        }

        return jsonObject;
    }

    private JsonElement parseExampleAnnotation(String example, Class<?> fieldType) {
        try {
            if (fieldType == boolean.class || fieldType == Boolean.class) {
                return new JsonPrimitive(Boolean.parseBoolean(example));
            } else if (fieldType == int.class || fieldType == Integer.class) {
                return new JsonPrimitive(Integer.parseInt(example));
            } else if (fieldType == String.class) {
                return new JsonPrimitive(example);
            } else if (fieldType == long.class || fieldType == Long.class) {
                return new JsonPrimitive(Long.parseLong(example));
            } else if (fieldType == double.class || fieldType == Double.class) {
                return new JsonPrimitive(Double.parseDouble(example));
            } else if (fieldType == float.class || fieldType == Float.class) {
                return new JsonPrimitive(Float.parseFloat(example));
            } else if (fieldType.isArray() && fieldType.getComponentType() == String.class) {
                JsonArray resultArray = new JsonArray();
                for (String str : example.split(",\\s*")) {
                    resultArray.add(str);
                }
                return resultArray;
            } else if (fieldType.isArray()) {
                JsonArray resultArray = new JsonArray();
                Class<?> componentType = fieldType.getComponentType();
                for (String part : example.split(",\\s*")) {
                    resultArray.add(parseExampleAnnotation(part, componentType));
                }
                return resultArray;
            }
        } catch (NumberFormatException ignored) {
        }
        return null;
    }

    private String getSimpleTypeName(Class<?> type) {
        if (type.isArray()) {
            return getSimpleTypeName(type.getComponentType()) + "[]";
        }
        return type.getSimpleName();
    }
}
