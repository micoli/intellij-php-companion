package org.micoli.php.symfony.profiler.models

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.micoli.php.symfony.list.SearchableRecord
import org.micoli.php.symfony.profiler.htmlParsers.FileLocation

class DBStats(
    val databaseQueriesCount: Int,
    val differentStatmentsCount: Int,
    val queryTime: Double,
)

class DBData(val queries: List<DBQuery>, entities: List<String>, stats: DBStats)

class DBQuery(
    val index: Number,
    val sql: String,
    val runnableSql: String,
    val htmlSql: String,
    val executionMS: Double,
    val backtrace: List<FileLocation>,
    val connection: String = "default",
) : SearchableRecord {
    override fun getSearchString(): ImmutableList<String> {
        return persistentListOf(sql)
    }
}
