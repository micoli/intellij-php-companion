package org.micoli.php.configuration.documentation;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;
import org.jetbrains.annotations.Nullable;

public final class ClassPropertiesDocumentationGenerator {

    public record PropertyInfo(
            String dotNotationPath,
            String name,
            Class<?> type,
            String description,
            String example,
            String defaultValue) {}

    private final Set<Class<?>> visitedClasses = new HashSet<>();
    private final List<PropertyInfo> properties = new ArrayList<>();

    public List<PropertyInfo> getProperties(Object object, int maxDepth) {
        visitedClasses.clear();
        traverseClass(object, "", maxDepth);
        return properties;
    }

    private void traverseClass(Object object, String currentPath, int maxDepth) {
        if (maxDepth == 0) {
            return;
        }
        if (object == null
                || visitedClasses.contains(object.getClass())
                || isPrimitiveOrWrapper(object.getClass())
                || object.getClass() == String.class
                || object.getClass().getName().startsWith("java.")) {
            return;
        }

        visitedClasses.add(object.getClass());

        for (Field field : object.getClass().getFields()) {
            if (!Modifier.isPublic(field.getModifiers())) {
                continue;
            }
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            Object fieldValue;
            try {
                fieldValue = field.get(object);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            Class<?> fieldTypeCandidate = field.getType();
            Map<String, Class<?>> fieldTypes;
            Schema fieldSchema = field.getAnnotation(Schema.class);
            JsonSubTypes subtypes = fieldTypeCandidate.getAnnotation(JsonSubTypes.class);
            JsonTypeInfo jsonTypeInfo = fieldTypeCandidate.getAnnotation(JsonTypeInfo.class);
            if (subtypes != null) {
                fieldTypes = Arrays.stream(subtypes.value())
                        .collect(Collectors.toMap(JsonSubTypes.Type::name, JsonSubTypes.Type::value));
            } else {
                fieldTypes = Map.of(fieldTypeCandidate.getSimpleName(), fieldTypeCandidate);
            }
            for (Map.Entry<String, Class<?>> subType : fieldTypes.entrySet()) {

                Class<?> fieldType = subType.getValue();
                boolean isMultiple = (fieldValue != null
                        && (fieldValue.getClass().isArray()
                                || Collection.class.isAssignableFrom(fieldType)
                                || Map.class.isAssignableFrom(fieldType)));

                String fieldName = field.getName() + (isMultiple ? "[]" : "");
                String fieldPath = currentPath.isEmpty() ? fieldName : currentPath + "." + fieldName;
                if (jsonTypeInfo != null) {
                    fieldPath = fieldPath + "(" + jsonTypeInfo.property() + "=" + subType.getKey() + ")";
                }

                properties.add(new PropertyInfo(
                        fieldPath,
                        fieldName,
                        fieldType,
                        getDescription(fieldSchema),
                        getExample(fieldSchema),
                        getDefaultValue(isMultiple, fieldValue)));

                if (jsonTypeInfo != null) {
                    properties.add(new PropertyInfo(
                            fieldPath + "." + jsonTypeInfo.property(),
                            fieldName,
                            fieldType,
                            subType.getKey(),
                            subType.getKey(),
                            subType.getKey()));
                }

                if (fieldValue != null && fieldValue.getClass().isArray()) {
                    Object[] values = (Object[]) fieldValue;
                    if (values.length > 0) {
                        traverseClass(values[0], fieldPath, maxDepth - 1);
                    }
                }

                if (Collection.class.isAssignableFrom(fieldType)) {
                    assert fieldValue instanceof Collection<?>;
                    Collection<?> valueCollection = (Collection<?>) fieldValue;
                    if (!valueCollection.isEmpty()) {
                        traverseClass(valueCollection.iterator().next(), fieldPath, maxDepth);
                    }
                }

                if (Map.class.isAssignableFrom(fieldType)) {
                    throw new UnsupportedOperationException("Maps are not yet supported");
                }

                if (!isPrimitiveOrWrapper(fieldType) && fieldType != String.class) {
                    traverseClass(fieldValue, fieldPath, maxDepth - 1);
                }
            }
        }
    }

    private @Nullable String getDefaultValue(boolean isMultiple, Object fieldValue) {
        if (isMultiple) {
            return null;
        }

        if (fieldValue instanceof String stringValue) {
            return stringValue;
        }
        if (fieldValue != null && isPrimitiveOrWrapper(fieldValue.getClass())) {
            return fieldValue.toString();
        }
        return null;
    }

    private String getExample(Schema fieldSchema) {
        if (fieldSchema == null || fieldSchema.example().isEmpty()) {
            return null;
        }
        return fieldSchema.example();
    }

    private String getDescription(Schema fieldSchema) {
        if (fieldSchema == null || fieldSchema.description().isEmpty()) {
            return null;
        }
        return fieldSchema.description();
    }

    private boolean isPrimitiveOrWrapper(Class<?> clazz) {
        return clazz.isPrimitive()
                || clazz == Boolean.class
                || clazz == Character.class
                || clazz == Byte.class
                || clazz == Short.class
                || clazz == Integer.class
                || clazz == Long.class
                || clazz == Float.class
                || clazz == Double.class;
    }
}
