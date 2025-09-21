package org.micoli.php.builders

import org.micoli.php.tasks.configuration.Watcher

class WatcherBuilder private constructor() {
    private val watcher: Watcher = Watcher()

    fun withTaskId(taskId: String): WatcherBuilder {
        watcher.taskId = taskId
        return this
    }

    fun withDebounce(debounce: Int): WatcherBuilder {
        watcher.debounce = debounce
        return this
    }

    fun withNotify(notify: Boolean): WatcherBuilder {
        watcher.notify = notify
        return this
    }

    fun withWatches(watches: Array<String>): WatcherBuilder {
        watcher.watches = watches
        return this
    }

    fun build(): Watcher {
        return watcher
    }

    companion object {
        fun create(): WatcherBuilder {
            return WatcherBuilder()
        }
    }
}
