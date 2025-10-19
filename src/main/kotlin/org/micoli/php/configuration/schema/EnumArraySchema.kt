package org.micoli.php.configuration.schema

import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class EnumArraySchema(val enumClass: KClass<out Enum<*>>)
