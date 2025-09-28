package org.micoli.php.ui.panels

import javax.swing.RowFilter
import kotlin.Boolean
import kotlin.text.isEmpty
import org.micoli.php.symfony.list.SearchableRecord
import org.micoli.php.ui.panels.rowMatchers.EmptyMatcher
import org.micoli.php.ui.panels.rowMatchers.PlainMatcher
import org.micoli.php.ui.panels.rowMatchers.RegexMatcher
import org.micoli.php.ui.panels.rowMatchers.RowMatcher

class ListRowFilter<M, I> : RowFilter<M?, I?>() {
    var searcher: RowMatcher = EmptyMatcher()

    fun updateFilter(searchText: String, isRegexMode: Boolean) {
        searcher =
            when {
                searchText.isEmpty() -> EmptyMatcher()
                isRegexMode -> RegexMatcher(searchText)
                !isRegexMode -> PlainMatcher(searchText)
                else -> EmptyMatcher()
            }
    }

    override fun include(entry: Entry<out M?, out I?>): Boolean {
        val value = entry.getValue(entry.valueCount - 1)
        if (value !is SearchableRecord) {
            return false
        }
        return searcher.match(value)
    }
}
