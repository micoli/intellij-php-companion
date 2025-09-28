package org.micoli.php.ui.panels.rowMatchers

import com.intellij.util.containers.stream
import java.util.Locale
import kotlin.Boolean
import kotlin.collections.dropLastWhile
import kotlin.collections.toTypedArray
import kotlin.text.isEmpty
import kotlin.text.split
import kotlin.text.toRegex
import kotlin.text.trim
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import org.micoli.php.symfony.list.SearchableRecord

class PlainMatcher : RowMatcher {
    private var searchParts: ImmutableList<String> = emptyList<String>().toImmutableList()

    constructor(searchText: String) {
        searchParts =
            searchText
                .split(" ".toRegex())
                .dropLastWhile { it.isEmpty() }
                .toTypedArray()
                .stream()
                .filter { it != null }
                .map { it.trim { subIt -> subIt <= ' ' } }
                .toList()
                .toImmutableList()
    }

    override fun match(searchableRecord: SearchableRecord): Boolean {
        return searchParts.stream().allMatch {
            searchableRecord
                .getSearchString()
                .joinToString(" ")
                .lowercase(Locale.getDefault())
                .contains(it.lowercase(Locale.getDefault()))
        }
    }
}
