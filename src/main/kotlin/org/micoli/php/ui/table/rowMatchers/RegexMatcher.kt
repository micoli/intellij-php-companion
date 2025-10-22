package org.micoli.php.ui.table.rowMatchers

import com.intellij.util.containers.stream
import java.util.regex.Pattern
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

class RegexMatcher : RowMatcher {
    private var searchPatterns: ImmutableList<Pattern> = listOf<Pattern>().toImmutableList()

    constructor(searchText: String) {
        searchPatterns =
            searchText
                .split(" ".toRegex())
                .dropLastWhile { it.isEmpty() }
                .toTypedArray()
                .stream()
                .map { it.trim { subIt -> subIt <= ' ' } }
                .map { Pattern.compile("(?i)$it") }
                .toList()
                .toImmutableList()
    }

    override fun match(searchableRecord: SearchableRecord): Boolean {
        return searchPatterns.stream().allMatch {
            searchableRecord.getSearchString().stream().anyMatch { c: String? ->
                it.matcher(c).find()
            }
        }
    }
}
