package org.micoli.php.configuration.schema

import kotlin.reflect.KClass

object EnumSchemaHelper {
    fun getEnumValues(enumClass: KClass<out Enum<*>>): List<String> {
        return enumClass.java.enumConstants.map { it.name }
    }

    fun getExamples(enumClass: KClass<out Enum<*>>, count: Int = 2): List<String> {
        return getEnumValues(enumClass).take(count)
    }
}
