package org.micoli.php.configuration.schema.valueGenerator

import com.intellij.openapi.actionSystem.ActionManager
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList

class ActionIdValueGenerator : PropertyValueGenerator {
    override fun getFieldNames(): ImmutableList<String> = persistentListOf("actionId")

    override fun getValues(): ImmutableList<String> =
      ActionManager.getInstance()
        .getActionIdList("")
        .stream()
        .filter { s: String? -> s != null }
        .filter { s: String -> s.contains("anonymous-group-") }
        .distinct()
        .sorted()
        .toList()
        .toImmutableList()
}
