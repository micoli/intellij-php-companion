package org.micoli.php.configuration.documentation;

import io.swagger.v3.oas.annotations.media.Schema;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class InstanceGenerator {

    private final Map<Class<?>, Object> instanceCache = new HashMap<>();

    public <T> T get(Class<T> clazz, boolean useExampleAsDefaultValue) {
        return get(clazz, new HashMap<>(), useExampleAsDefaultValue);
    }

    @SuppressWarnings("unchecked")
    private <T> T get(Class<T> clazz, Map<Class<?>, Object> recursionGuard, boolean useExampleAsDefaultValue) {
        if (recursionGuard.containsKey(clazz)) {
            return (T) recursionGuard.get(clazz);
        }

        try {
            T instance = clazz.getDeclaredConstructor().newInstance();
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

        } catch (Exception e) {
            throw new RuntimeException("Impossible de générer une instance pour " + clazz.getName(), e);
        }
    }

    private Object generateFieldValue(
            Field field, Map<Class<?>, Object> recursionGuard, boolean useExampleAsDefaultValue) {
        Class<?> fieldType = field.getType();

        String exampleValue = getExampleFromAnnotation(field);
        if (exampleValue != null && useExampleAsDefaultValue) {
            return convertExampleValue(exampleValue, fieldType);
        }

        if (fieldType.isArray()) {
            String[] examplesValues = getExamplesFromAnnotation(field);
            if (examplesValues != null && useExampleAsDefaultValue) {
                return convertExampleValues(examplesValues, fieldType);
            }
            return generateArrayValue(fieldType, recursionGuard, useExampleAsDefaultValue);
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
            Class<?> arrayType, Map<Class<?>, Object> recursionGuard, boolean useExampleAsDefaultValue) {
        Class<?> componentType = arrayType.getComponentType();

        Object array = Array.newInstance(componentType, 1);

        if (isPrimitiveOrWrapper(componentType)) {
            Array.set(array, 0, getDefaultPrimitiveValue(componentType));
        } else if (componentType == String.class) {
            Array.set(array, 0, "");
        } else if (!componentType.getName().startsWith("java.")) {
            Object elementInstance = get(componentType, recursionGuard, useExampleAsDefaultValue);
            Array.set(array, 0, elementInstance);
        }

        return array;
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
}
