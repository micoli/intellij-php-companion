package org.micoli.php.ui.table.rowMatchers

import org.micoli.php.symfony.list.SearchableRecord

interface RowMatcher {
    fun match(searchableRecord: SearchableRecord): Boolean
}
