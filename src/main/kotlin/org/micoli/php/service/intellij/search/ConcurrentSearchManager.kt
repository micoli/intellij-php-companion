package org.micoli.php.service.intellij.search

import java.time.Duration
import java.time.Instant
import java.util.*

class ConcurrentSearchManager(private val searchTimeout: Duration) {
    @JvmRecord
    private data class SearchEntry(val query: String, val creationTime: Instant?) {
        fun isExpired(timeout: Duration): Boolean {
            return creationTime!!.plus(timeout).isBefore(Instant.now())
        }
    }

    private val searchInProgressList: MutableList<SearchEntry> =
        Collections.synchronizedList<SearchEntry>(ArrayList())

    fun addSearch(query: String) {
        cleanExpiredSearches()
        searchInProgressList.add(SearchEntry(query, Instant.now()))
    }

    fun removeSearch(query: String) {
        searchInProgressList.removeIf { entry: SearchEntry? -> entry!!.query == query }
    }

    fun isSearchInProgress(query: String): Boolean {
        cleanExpiredSearches()
        return searchInProgressList.stream().anyMatch { entry: SearchEntry? ->
            entry!!.query == query
        }
    }

    val isEmpty: Boolean
        get() {
            cleanExpiredSearches()
            return searchInProgressList.isEmpty()
        }

    private fun cleanExpiredSearches() {
        searchInProgressList.removeIf { entry: SearchEntry? -> entry!!.isExpired(searchTimeout) }
    }
}
