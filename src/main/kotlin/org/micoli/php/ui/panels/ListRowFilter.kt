package org.micoli.php.ui.panels

import java.util.*
import java.util.regex.Pattern
import javax.swing.RowFilter
import kotlin.Boolean
import kotlin.collections.MutableList
import kotlin.collections.dropLastWhile
import kotlin.collections.toTypedArray
import kotlin.text.contains
import kotlin.text.isEmpty
import kotlin.text.lowercase
import kotlin.text.split
import kotlin.text.toRegex
import kotlin.text.trim
import org.micoli.php.symfony.list.SearchableRecord

class ListRowFilter<M, I> : RowFilter<M?, I?>() {
    private var searchText = ""
    private var searchPatterns: MutableList<Pattern?>? = null
    private var searchParts: MutableList<String?>? = null
    private var isRegexMode = true

    fun updateFilter(searchText: String, isRegexMode: Boolean) {
        this.searchText = searchText
        this.isRegexMode = isRegexMode
        searchPatterns =
          if (isRegexMode)
            Arrays.stream(searchText.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
              .map { obj: String? -> obj!!.trim { it <= ' ' } }
              .map { expr: String? -> Pattern.compile("(?i)$expr") }
              .toList()
          else null
        searchParts =
          if (!isRegexMode) Arrays.stream(searchText.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()).map { obj: String? -> obj!!.trim { it <= ' ' } }.toList()
          else null
    }

    override fun include(entry: Entry<out M?, out I?>): Boolean {
        if (searchText.isEmpty()) {
            return true
        }
        if (entry.getValue(entry.valueCount - 1) is SearchableRecord) {
            val searchableRecord = entry.getValue(entry.valueCount - 1) as SearchableRecord
            if (isRegexMode) {
                return searchPatterns!!.stream().allMatch { pattern: Pattern? ->
                    searchableRecord.getSearchString().stream().anyMatch { c: String? -> pattern!!.matcher(c).find() }
                }
            }
            return searchParts!!.stream().allMatch { part: String? ->
                searchableRecord.getSearchString().joinToString(" ").lowercase(Locale.getDefault()).contains(part!!.lowercase(Locale.getDefault()))
            }
        }
        return false
    }
}
