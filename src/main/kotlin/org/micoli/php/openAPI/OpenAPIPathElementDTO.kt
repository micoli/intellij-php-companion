package org.micoli.php.openAPI

import com.intellij.psi.PsiElement
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.micoli.php.symfony.list.SearchableRecord

@JvmRecord
data class OpenAPIPathElementDTO(
    val rootPath: String,
    val uri: String,
    val method: String,
    val description: String,
    val operationId: String,
    val element: PsiElement?
) : SearchableRecord {
    override fun getSearchString(): ImmutableList<String> {
        return persistentListOf(uri, method, description, operationId, rootPath)
    }
}
