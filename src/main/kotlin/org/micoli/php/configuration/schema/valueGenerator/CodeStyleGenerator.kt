package org.micoli.php.configuration.schema.valueGenerator

import com.intellij.psi.codeStyle.CommonCodeStyleSettings
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import java.util.*
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

class CodeStyleGenerator : PropertyValueGenerator {
    override fun getFieldNames(): ImmutableList<String> = persistentListOf("styleAttribute")

    override fun getValues(): ImmutableList<String> {
        val fields = CommonCodeStyleSettings::class.java.getFields()
        return Arrays.stream(fields)
            .filter { field: Field? -> field != null }
            .filter { field: Field -> Modifier.isPublic(field.modifiers) }
            .filter { field: Field ->
                field.type == Int::class.javaPrimitiveType ||
                    field.type == Int::class.java ||
                    field.type == Boolean::class.javaPrimitiveType ||
                    field.type == Boolean::class.java
            }
            .map { obj: Field -> obj.name }
            .distinct()
            .sorted()
            .toList()
            .toImmutableList()
    }
}
