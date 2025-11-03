package org.micoli.php.symfony.profiler.models

import java.time.OffsetDateTime
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.micoli.php.symfony.list.SearchableRecord

class Log(
    val time: OffsetDateTime,
    val channel: String,
    val severity: String,
    val message: String,
    val context: String,
) : SearchableRecord {
    override fun getSearchString(): ImmutableList<String> {
        return persistentListOf(severity, channel, message, context)
    }
}

class LoggerData(val logs: List<Log>)
