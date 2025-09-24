package org.micoli.php.openAPI

import com.intellij.psi.PsiElement
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
    override fun getSearchString(): List<String> {
        return listOf(uri, method, description, operationId, rootPath)
    }
}
