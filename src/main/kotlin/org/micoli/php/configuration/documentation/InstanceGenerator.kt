package org.micoli.php.configuration.documentation

import com.fasterxml.jackson.annotation.JsonSubTypes
import io.swagger.v3.oas.annotations.media.Schema
import java.lang.reflect.Field
import java.lang.reflect.InvocationTargetException

class InstanceGenerator {
    fun <T> get(clazz: Class<*>, useExampleAsDefaultValue: Boolean): T? =
        get(clazz, HashMap(), useExampleAsDefaultValue)

    private fun <T> get(
        clazz: Class<*>,
        recursionGuard: MutableMap<Class<*>?, Any?>,
        useExampleAsDefaultValue: Boolean,
    ): T? {
        if (recursionGuard.containsKey(clazz)) {
            return null
        }

        try {
            val instance = getClassToInstantiate(clazz).getDeclaredConstructor().newInstance()
            recursionGuard[clazz] = instance

            val fields = clazz.getDeclaredFields()
            for (field in fields) {
                field.setAccessible(true)
                val currentValue = field.get(instance)
                if (currentValue != null && !shouldOverrideDefaultValue(currentValue)) {
                    continue
                }

                val value = generateFieldValue(field, recursionGuard, useExampleAsDefaultValue)
                if (value != null) {
                    field.set(instance, value)
                }
            }

            return instance as T?
        } catch (e: IllegalAccessException) {
            throw RuntimeException(e)
        } catch (e: InvocationTargetException) {
            throw RuntimeException(e)
        } catch (e: InstantiationException) {
            throw RuntimeException(e)
        } catch (e: NoSuchMethodException) {
            throw RuntimeException(e)
        }
    }

    private fun generateFieldValue(
        field: Field,
        recursionGuard: MutableMap<Class<*>?, Any?>,
        useExampleAsDefaultValue: Boolean,
    ): Any? {
        val fieldType = field.type
        val subtypes: JsonSubTypes? = getSubtypes(field)

        val exampleValue = getExampleFromAnnotation(field)
        if (exampleValue != null && useExampleAsDefaultValue) {
            return convertExampleValue(exampleValue, fieldType)
        }

        if (fieldType.isArray) {
            val examplesValues = getExamplesFromAnnotation(field)
            if (examplesValues != null && useExampleAsDefaultValue) {
                return convertExampleValues(examplesValues, fieldType)
            }
            if (subtypes != null) {
                return generateArrayValues(
                    fieldType.componentType, subtypes, recursionGuard, useExampleAsDefaultValue)
            }
            return generateArrayValue(
                fieldType.componentType(), recursionGuard, useExampleAsDefaultValue)
        }

        if (isPrimitiveOrWrapper(fieldType)) {
            return getDefaultPrimitiveValue(fieldType)
        }

        if (fieldType == String::class.java) {
            return ""
        }

        if (!fieldType.getName().startsWith("java.")) {
            return get(fieldType, recursionGuard, useExampleAsDefaultValue)
        }

        return null
    }

    private fun getClassToInstantiate(clazz: Class<*>): Class<*> {
        val subtypes = clazz.getAnnotation(JsonSubTypes::class.java)
        if (subtypes != null && subtypes.value.isNotEmpty()) {
            return subtypes.value[0].value.java
        }
        return clazz
    }

    private fun getExampleFromAnnotation(field: Field): String? {
        val schema = field.getAnnotation(Schema::class.java)
        if (schema != null) {
            if (!schema.example.isEmpty()) {
                return schema.example
            }
        }
        return null
    }

    private fun getExamplesFromAnnotation(field: Field): Array<String>? {
        val schema = field.getAnnotation(Schema::class.java)
        if (schema != null) {
            if (schema.examples.isNotEmpty()) {
                return schema.examples
            }
        }
        return null
    }

    private fun convertExampleValues(examples: Array<String>, targetType: Class<*>): Any {
        val componentType = targetType.componentType
        val result = java.lang.reflect.Array.newInstance(componentType, examples.size)
        for (i in examples.indices) {
            val value = convertExampleValue(examples[i], componentType)
            java.lang.reflect.Array.set(result, i, value)
        }
        return result
    }

    private fun generateArrayValue(
        componentType: Class<*>,
        recursionGuard: MutableMap<Class<*>?, Any?>,
        useExampleAsDefaultValue: Boolean,
    ): Any {
        val array = java.lang.reflect.Array.newInstance(componentType, 1)
        java.lang.reflect.Array.set(
            array, 0, getExampleValue(componentType, recursionGuard, useExampleAsDefaultValue))

        return array
    }

    private fun generateArrayValues(
        componentType: Class<*>?,
        subTypes: JsonSubTypes,
        recursionGuard: MutableMap<Class<*>?, Any?>,
        useExampleAsDefaultValue: Boolean,
    ): Any {
        val array = java.lang.reflect.Array.newInstance(componentType, subTypes.value.size)

        for (index in subTypes.value.indices) {
            java.lang.reflect.Array.set(
                array,
                index,
                getExampleValue(
                    subTypes.value[index].value.java, recursionGuard, useExampleAsDefaultValue),
            )
        }

        return array
    }

    private fun getExampleValue(
        subType: Class<*>,
        recursionGuard: MutableMap<Class<*>?, Any?>,
        useExampleAsDefaultValue: Boolean,
    ): Any? {
        if (isPrimitiveOrWrapper(subType)) {
            return getDefaultPrimitiveValue(subType)
        } else if (subType == String::class.java) {
            return ""
        } else if (!subType.getName().startsWith("java.")) {
            return get(subType, recursionGuard, useExampleAsDefaultValue)
        }
        return null
    }

    private fun shouldOverrideDefaultValue(value: Any): Boolean {
        if (value.javaClass.isArray) {
            return java.lang.reflect.Array.getLength(value) == 0
        }
        return false
    }

    private fun isPrimitiveOrWrapper(type: Class<*>): Boolean =
        type.isPrimitive ||
            type == Boolean::class.java ||
            type == Int::class.java ||
            type == Long::class.java ||
            type == Double::class.java ||
            type == Float::class.java ||
            type == Char::class.java ||
            type == Byte::class.java ||
            type == Short::class.java

    private fun getDefaultPrimitiveValue(type: Class<*>?): Any? {
        if (type == Boolean::class.javaPrimitiveType || type == Boolean::class.java) return false
        if (type == Int::class.javaPrimitiveType || type == Int::class.java) return 0
        if (type == Long::class.javaPrimitiveType || type == Long::class.java) return 0L
        if (type == Double::class.javaPrimitiveType || type == Double::class.java) return 0.0
        if (type == Float::class.javaPrimitiveType || type == Float::class.java) return 0.0f
        if (type == Char::class.javaPrimitiveType || type == Char::class.java) return '\u0000'
        if (type == Byte::class.javaPrimitiveType || type == Byte::class.java) return 0.toByte()
        if (type == Short::class.javaPrimitiveType || type == Short::class.java) return 0.toShort()
        return null
    }

    private fun convertExampleValue(example: String, targetType: Class<*>?): Any {
        try {
            return when (targetType) {
                Boolean::class.javaPrimitiveType,
                Boolean::class.java -> example.toBoolean()
                Int::class.javaPrimitiveType,
                Int::class.java -> example.toInt()
                Long::class.javaPrimitiveType,
                Long::class.java -> example.toLong()
                Double::class.javaPrimitiveType,
                Double::class.java -> example.toDouble()
                Float::class.javaPrimitiveType,
                Float::class.java -> example.toFloat()
                String::class.java -> example
                else -> throw RuntimeException("Unsupported type: $targetType")
            }
        } catch (ignored: NumberFormatException) {}
        return example
    }

    companion object {
        private fun getSubtypes(field: Field): JsonSubTypes? {
            if (field.type.componentType == null) {
                return field.type.getAnnotation(JsonSubTypes::class.java)
            }
            return field.type.componentType.getAnnotation(JsonSubTypes::class.java)
        }
    }
}
