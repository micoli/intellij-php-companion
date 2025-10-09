package org.micoli.php.ui.table

import javax.swing.RowFilter
import org.micoli.php.symfony.list.SearchableRecord
import org.micoli.php.ui.table.rowMatchers.EmptyMatcher
import org.micoli.php.ui.table.rowMatchers.PlainMatcher
import org.micoli.php.ui.table.rowMatchers.RegexMatcher
import org.micoli.php.ui.table.rowMatchers.RowMatcher

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
        val value =
            (entry.model as ObjectTableModel<*>).getObjectAt(entry.identifier as Int)
                ?: return false
        if (value !is SearchableRecord) {
            return false
        }
        return searcher.match(value)
    }
}
