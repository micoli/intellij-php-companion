package org.micoli.php.ui.panels.rowMatchers

import kotlin.Boolean
import org.micoli.php.symfony.list.SearchableRecord

class EmptyMatcher : RowMatcher {
    override fun match(searchableRecord: SearchableRecord): Boolean {
        return true
    }
}
