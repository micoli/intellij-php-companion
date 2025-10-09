package org.micoli.php.symfony.profiler

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.micoli.php.symfony.list.SearchableRecord

@JvmRecord
data class SymfonyProfileDTO(
    val token: String,
    val ip: String,
    val method: String,
    val url: String,
    val timestamp: String,
    val parent: String,
    val statusCode: String,
    val type: String,
    val profileUrl: String
) : SearchableRecord {
    override fun getSearchString(): ImmutableList<String> {
        return persistentListOf(url, statusCode, method, type, ip)
    }

    companion object {
        @JvmStatic val EMPTY = SymfonyProfileDTO("", "", "", "", "", "", "", "", "")
    }
}
