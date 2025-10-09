package org.micoli.php.ui.table.rowMatchers

import kotlin.Boolean
import org.micoli.php.symfony.list.SearchableRecord

class EmptyMatcher : RowMatcher {
    override fun match(searchableRecord: SearchableRecord): Boolean {
        return true
    }
}
