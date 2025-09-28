package org.micoli.php.configuration.schema.valueGenerator

import com.intellij.psi.codeStyle.CommonCodeStyleSettings
import com.intellij.util.containers.stream
import java.lang.reflect.Modifier
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

class CodeStyleGenerator : PropertyValueGenerator {
    override fun getFieldNames(): ImmutableList<String> = persistentListOf("styleAttribute")

    override fun getValues(): ImmutableList<String> {
        return CommonCodeStyleSettings::class
            .java
            .getFields()
            .stream()
            .filter { it != null }
            .filter { Modifier.isPublic(it.modifiers) }
            .filter {
                it.type == Int::class.javaPrimitiveType ||
                    it.type == Int::class.java ||
                    it.type == Boolean::class.javaPrimitiveType ||
                    it.type == Boolean::class.java
            }
            .map { it.name }
            .distinct()
            .sorted()
            .toList()
            .toImmutableList()
    }
}
