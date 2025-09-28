package org.micoli.php.configuration.documentation

import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import io.swagger.v3.oas.annotations.media.Schema
import java.lang.reflect.Field
import java.lang.reflect.Modifier

class ClassPropertiesDocumentationGenerator {
    @JvmRecord
    data class PropertyInfo(
        val dotNotationPath: String,
        val name: String,
        val type: Class<*>,
        val description: String?,
        val example: String?,
        val defaultValue: String?,
    )

    private val visitedClasses: MutableSet<Class<*>> = HashSet()
    private val properties: MutableList<PropertyInfo> = ArrayList<PropertyInfo>()

    fun getProperties(clazz: Any?, maxDepth: Int): MutableList<PropertyInfo> {
        visitedClasses.clear()
        traverseClass(clazz, "", maxDepth)
        return properties
    }

    private fun traverseClass(clazz: Any?, currentPath: String, maxDepth: Int) {
        if (maxDepth == 0) {
            return
        }
        if (clazz == null ||
            visitedClasses.contains(clazz.javaClass) ||
            isPrimitiveOrWrapper(clazz.javaClass) ||
            clazz.javaClass == String::class.java ||
            clazz.javaClass.getName().startsWith("java.")) {
            return
        }

        visitedClasses.add(clazz.javaClass)

        val fields: MutableSet<Field> = HashSet()
        var currentClass: Class<*>? = clazz.javaClass
        // System.out.println(currentPath)
        while (currentClass != null) {
            fields.addAll(listOf(*currentClass.getDeclaredFields()))
            fields.addAll(listOf(*currentClass.getFields()))
            currentClass = currentClass.superclass
        }
        for (field in fields) {
            field.setAccessible(true)

            if (Modifier.isStatic(field.modifiers)) {
                continue
            }
            val fieldValue: Any?
            try {
                fieldValue = field.get(clazz)
            } catch (e: IllegalAccessException) {
                throw RuntimeException(e)
            }
            val fieldTypeCandidate = field.type
            val fieldTypes: MutableMap<String, Class<*>> = HashMap()
            val fieldSchema = field.getAnnotation(Schema::class.java)
            val subtypes = fieldTypeCandidate.getAnnotation(JsonSubTypes::class.java)
            val jsonTypeInfo = fieldTypeCandidate.getAnnotation(JsonTypeInfo::class.java)
            if (subtypes != null) {
                for (value in subtypes.value) {
                    fieldTypes[value.name] = value.value.java
                }
            } else {
                fieldTypes[fieldTypeCandidate.getSimpleName()] = fieldTypeCandidate.javaClass
            }
            for (subType in fieldTypes.entries) {
                val fieldType: Class<*> = subType.value
                val isMultiple =
                    (fieldValue != null &&
                        (fieldValue.javaClass.isArray ||
                            MutableCollection::class.java.isAssignableFrom(fieldType) ||
                            MutableMap::class.java.isAssignableFrom(fieldType)))

                val fieldName = field.name + (if (isMultiple) "[]" else "")
                var fieldPath = if (currentPath.isEmpty()) fieldName else "$currentPath.$fieldName"
                if (jsonTypeInfo != null) {
                    fieldPath = fieldPath + "(" + jsonTypeInfo.property + "=" + subType.key + ")"
                }

                properties.add(
                    PropertyInfo(
                        fieldPath,
                        fieldName,
                        fieldType,
                        getDescription(fieldSchema),
                        getExample(fieldSchema),
                        getDefaultValue(isMultiple, fieldValue),
                    ))

                if (jsonTypeInfo != null) {
                    properties.add(
                        PropertyInfo(
                            fieldPath + "." + jsonTypeInfo.property,
                            fieldName,
                            fieldType,
                            subType.key,
                            subType.key,
                            subType.key,
                        ))
                }

                if (fieldValue != null && fieldValue.javaClass.isArray) {
                    val values = fieldValue as Array<*>
                    if (values.isNotEmpty()) {
                        traverseClass(values[0], fieldPath, maxDepth - 1)
                    }
                }

                if (MutableCollection::class.java.isAssignableFrom(fieldType)) {
                    assert(fieldValue is MutableCollection<*>)
                    val valueCollection = fieldValue as MutableCollection<*>
                    if (!valueCollection.isEmpty()) {
                        traverseClass(valueCollection.iterator().next(), fieldPath, maxDepth)
                    }
                }

                if (MutableMap::class.java.isAssignableFrom(fieldType)) {
                    throw UnsupportedOperationException("Maps are not yet supported")
                }

                if (!isPrimitiveOrWrapper(fieldType) && fieldType != String::class.java) {
                    traverseClass(fieldValue, fieldPath, maxDepth - 1)
                }
            }
        }
    }

    private fun getDefaultValue(isMultiple: Boolean, fieldValue: Any?): String? {
        if (isMultiple) {
            return null
        }

        if (fieldValue is String) {
            return fieldValue
        }
        if (fieldValue != null && isPrimitiveOrWrapper(fieldValue.javaClass)) {
            return fieldValue.toString()
        }
        return null
    }

    private fun getExample(fieldSchema: Schema?): String? {
        if (fieldSchema == null || fieldSchema.example.isEmpty()) {
            return null
        }
        return fieldSchema.example
    }

    private fun getDescription(fieldSchema: Schema?): String? {
        if (fieldSchema == null || fieldSchema.description.isEmpty()) {
            return null
        }
        return fieldSchema.description
    }

    private fun isPrimitiveOrWrapper(clazz: Class<*>): Boolean =
        clazz.isPrimitive ||
            clazz == Boolean::class.javaObjectType ||
            clazz == Char::class.javaObjectType ||
            clazz == Byte::class.javaObjectType ||
            clazz == Short::class.javaObjectType ||
            clazz == Int::class.javaObjectType ||
            clazz == Long::class.javaObjectType ||
            clazz == Float::class.javaObjectType ||
            clazz == Double::class.javaObjectType
}
