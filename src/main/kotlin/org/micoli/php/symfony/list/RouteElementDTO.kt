package org.micoli.php.symfony.list

import com.intellij.psi.PsiElement
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

@JvmRecord
data class RouteElementDTO(
    val uri: String,
    val name: String,
    val methods: String,
    val fqcn: String,
    val element: PsiElement
) : SearchableRecord {
    override fun getSearchString(): ImmutableList<String> {
        return persistentListOf(uri, name, methods, fqcn)
    }
}
