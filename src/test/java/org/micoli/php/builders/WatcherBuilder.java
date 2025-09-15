package org.micoli.php.builders;

import org.micoli.php.tasks.configuration.Watcher;

public class WatcherBuilder {
    private final Watcher watcher;

    private WatcherBuilder() {
        this.watcher = new Watcher();
    }

    public static WatcherBuilder create() {
        return new WatcherBuilder();
    }

    public WatcherBuilder withTaskId(String taskId) {
        watcher.taskId = taskId;
        return this;
    }

    public WatcherBuilder withDebounce(int debounce) {
        watcher.debounce = debounce;
        return this;
    }

    public WatcherBuilder withNotify(boolean notify) {
        watcher.notify = notify;
        return this;
    }

    public WatcherBuilder withWatches(String[] watches) {
        watcher.watches = watches;
        return this;
    }

    public Watcher build() {
        return watcher;
    }
}
