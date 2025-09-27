package org.micoli.php.symfony.list

import kotlinx.collections.immutable.ImmutableList

interface SearchableRecord {
    fun getSearchString(): ImmutableList<String>
}
