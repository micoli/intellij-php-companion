package org.micoli.php.configuration.documentation;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import io.swagger.v3.oas.annotations.media.Schema;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public class InstanceGenerator {

    public <T> T get(Class<T> clazz, boolean useExampleAsDefaultValue) {
        return get(clazz, new HashMap<>(), useExampleAsDefaultValue);
    }

    private <T> T get(Class<T> clazz, Map<Class<?>, Object> recursionGuard, boolean useExampleAsDefaultValue) {
        if (recursionGuard.containsKey(clazz)) {
            return null;
        }

        try {
            T instance = getClassToInstantiate(clazz).getDeclaredConstructor().newInstance();
            recursionGuard.put(clazz, instance);

            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);

                Object currentValue = field.get(instance);
                if (currentValue != null && !shouldOverrideDefaultValue(currentValue)) {
                    continue;
                }

                Object value = generateFieldValue(field, recursionGuard, useExampleAsDefaultValue);
                if (value != null) {
                    field.set(instance, value);
                }
            }

            return instance;
        } catch (IllegalAccessException
                | InvocationTargetException
                | InstantiationException
                | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private Object generateFieldValue(
            Field field, Map<Class<?>, Object> recursionGuard, boolean useExampleAsDefaultValue) {
        Class<?> fieldType = field.getType();
        JsonSubTypes subtypes = getSubtypes(field);

        String exampleValue = getExampleFromAnnotation(field);
        if (exampleValue != null && useExampleAsDefaultValue) {
            return convertExampleValue(exampleValue, fieldType);
        }

        if (fieldType.isArray()) {
            String[] examplesValues = getExamplesFromAnnotation(field);
            if (examplesValues != null && useExampleAsDefaultValue) {
                return convertExampleValues(examplesValues, fieldType);
            }
            if (subtypes != null) {
                return generateArrayValues(
                        fieldType.getComponentType(), subtypes, recursionGuard, useExampleAsDefaultValue);
            }
            return generateArrayValue(fieldType.componentType(), recursionGuard, useExampleAsDefaultValue);
        }

        if (isPrimitiveOrWrapper(fieldType)) {
            return getDefaultPrimitiveValue(fieldType);
        }

        if (fieldType == String.class) {
            return "";
        }

        if (!fieldType.getName().startsWith("java.")) {
            return get(fieldType, recursionGuard, useExampleAsDefaultValue);
        }

        return null;
    }

    @SuppressWarnings("unchecked")
    private <T> Class<T> getClassToInstantiate(Class<T> clazz) {
        JsonSubTypes subtypes = clazz.getAnnotation(JsonSubTypes.class);
        if (subtypes != null && subtypes.value().length > 0) {
            return (Class<T>) subtypes.value()[0].value();
        }
        return clazz;
    }

    private static JsonSubTypes getSubtypes(Field field) {
        if (field.getType().getComponentType() == null) {
            return field.getType().getAnnotation(JsonSubTypes.class);
        }
        return field.getType().getComponentType().getAnnotation(JsonSubTypes.class);
    }

    private String getExampleFromAnnotation(Field field) {
        Schema schema = field.getAnnotation(Schema.class);
        if (schema != null) {
            if (!schema.example().isEmpty()) {
                return schema.example();
            }
        }
        return null;
    }

    private String[] getExamplesFromAnnotation(Field field) {
        Schema schema = field.getAnnotation(Schema.class);
        if (schema != null) {
            if (schema.examples().length > 0) {
                return schema.examples();
            }
        }
        return null;
    }

    private Object convertExampleValues(String[] examples, Class<?> targetType) {
        Class<?> componentType = targetType.getComponentType();
        Object result = Array.newInstance(componentType, examples.length);
        for (int i = 0; i < examples.length; i++) {
            Object value = convertExampleValue(examples[i], componentType);
            Array.set(result, i, value);
        }
        return result;
    }

    private Object generateArrayValue(
            Class<?> componentType, Map<Class<?>, Object> recursionGuard, boolean useExampleAsDefaultValue) {

        Object array = Array.newInstance(componentType, 1);
        Array.set(array, 0, getExampleValue(componentType, recursionGuard, useExampleAsDefaultValue));

        return array;
    }

    private Object generateArrayValues(
            Class<?> componentType,
            JsonSubTypes subTypes,
            Map<Class<?>, Object> recursionGuard,
            boolean useExampleAsDefaultValue) {

        Object array = Array.newInstance(componentType, subTypes.value().length);

        for (int index = 0; index < subTypes.value().length; index++) {
            Array.set(
                    array,
                    index,
                    getExampleValue(subTypes.value()[index].value(), recursionGuard, useExampleAsDefaultValue));
        }

        return array;
    }

    private Object getExampleValue(
            Class<?> subType, Map<Class<?>, Object> recursionGuard, boolean useExampleAsDefaultValue) {
        if (isPrimitiveOrWrapper(subType)) {
            return getDefaultPrimitiveValue(subType);
        } else if (subType == String.class) {
            return "";
        } else if (!subType.getName().startsWith("java.")) {
            return get(subType, recursionGuard, useExampleAsDefaultValue);
        }
        return null;
    }

    private boolean shouldOverrideDefaultValue(Object value) {
        if (value.getClass().isArray()) {
            return Array.getLength(value) == 0;
        }
        return false;
    }

    private boolean isPrimitiveOrWrapper(Class<?> type) {
        return type.isPrimitive()
                || type == Boolean.class
                || type == Integer.class
                || type == Long.class
                || type == Double.class
                || type == Float.class
                || type == Character.class
                || type == Byte.class
                || type == Short.class;
    }

    private Object getDefaultPrimitiveValue(Class<?> type) {
        if (type == boolean.class || type == Boolean.class) return false;
        if (type == int.class || type == Integer.class) return 0;
        if (type == long.class || type == Long.class) return 0L;
        if (type == double.class || type == Double.class) return 0.0;
        if (type == float.class || type == Float.class) return 0.0f;
        if (type == char.class || type == Character.class) return '\0';
        if (type == byte.class || type == Byte.class) return (byte) 0;
        if (type == short.class || type == Short.class) return (short) 0;
        return null;
    }

    private Object convertExampleValue(String example, Class<?> targetType) {
        try {
            if (targetType == String.class) {
                return example;
            } else if (targetType == boolean.class || targetType == Boolean.class) {
                return Boolean.parseBoolean(example);
            } else if (targetType == int.class || targetType == Integer.class) {
                return Integer.parseInt(example);
            } else if (targetType == long.class || targetType == Long.class) {
                return Long.parseLong(example);
            } else if (targetType == double.class || targetType == Double.class) {
                return Double.parseDouble(example);
            } else if (targetType == float.class || targetType == Float.class) {
                return Float.parseFloat(example);
            }
        } catch (NumberFormatException ignored) {
        }
        return example;
    }
}
