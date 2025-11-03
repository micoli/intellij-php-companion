package org.micoli.php.symfony.profiler.models

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.micoli.php.symfony.list.SearchableRecord
import org.micoli.php.symfony.profiler.htmlParsers.FileLocation

class MessengerStats(
    val messageCount: Int,
)

class MessengerData(val dispatches: List<MessengerDispatch>, val stats: MessengerStats)

class MessengerDispatch(
    val messageName: String,
    val busName: String,
    val dispatch: FileLocation?,
    val message: String
) : SearchableRecord {
    override fun getSearchString(): ImmutableList<String> {
        return persistentListOf(messageName, busName, message)
    }
}
