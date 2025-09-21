package org.micoli.php.configuration.schema.valueGenerator

import kotlinx.collections.immutable.ImmutableList

interface PropertyValueGenerator {
    fun getFieldNames(): ImmutableList<String>

    fun getValues(): ImmutableList<String>
}
