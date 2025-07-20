package org.micoli.php.events;

import com.intellij.util.messages.Topic;

public interface IndexingEvents {
    Topic<IndexingEvents> INDEXING_EVENTS = Topic.create("PHP Companion Configuration Events", IndexingEvents.class);

    void indexingStatusChanged(boolean isIndexing);
}
