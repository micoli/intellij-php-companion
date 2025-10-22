package org.micoli.php.symfony.list

import com.intellij.psi.PsiElement
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@JvmRecord
data class DoctrineEntityElementDTO(
    val className: String,
    val name: String,
    val schema: String,
    val fqcn: String,
    val element: PsiElement
) : SearchableRecord {
    override fun getSearchString(): ImmutableList<String> {
        return persistentListOf(className, name, schema, fqcn)
    }
}
