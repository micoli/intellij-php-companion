package org.micoli.php.symfony.list

import com.intellij.psi.PsiElement
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@JvmRecord
data class CommandElementDTO(
    val command: String,
    val description: String,
    val className: String,
    val element: PsiElement
) : SearchableRecord {
    override fun getSearchString(): ImmutableList<String> {
        return persistentListOf(command, description, className)
    }
}
