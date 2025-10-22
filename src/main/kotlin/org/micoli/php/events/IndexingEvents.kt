package org.micoli.php.events

import com.intellij.util.messages.Topic

interface IndexingEvents {
    fun indexingStatusChanged(isIndexing: Boolean)

    companion object {
        @JvmField
        val INDEXING_EVENTS: Topic<IndexingEvents> =
            Topic.create<IndexingEvents>(
                "PHP Companion Configuration Events", IndexingEvents::class.java)
    }
}
