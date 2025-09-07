package org.micoli.php.service.intellij.search;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConcurrentSearchManager {
    private final Duration searchTimeout;

    public ConcurrentSearchManager(Duration searchTimeout) {
        this.searchTimeout = searchTimeout;
    }

    private record SearchEntry(String query, Instant creationTime) {
        private boolean isExpired(Duration timeout) {
            return creationTime.plus(timeout).isBefore(Instant.now());
        }
    }

    private final List<SearchEntry> searchInProgressList = Collections.synchronizedList(new ArrayList<>());

    public void addSearch(String query) {
        cleanExpiredSearches();
        searchInProgressList.add(new SearchEntry(query, Instant.now()));
    }

    public void removeSearch(String query) {
        searchInProgressList.removeIf(entry -> entry.query().equals(query));
    }

    public boolean isSearchInProgress(String query) {
        cleanExpiredSearches();
        return searchInProgressList.stream().anyMatch(entry -> entry.query().equals(query));
    }

    public boolean isEmpty() {
        cleanExpiredSearches();
        return searchInProgressList.isEmpty();
    }

    private void cleanExpiredSearches() {
        searchInProgressList.removeIf(entry -> entry.isExpired(searchTimeout));
    }
}
