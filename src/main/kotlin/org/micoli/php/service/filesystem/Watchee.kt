package org.micoli.php.service.filesystem

import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import java.nio.file.Path
import java.nio.file.PathMatcher


data class Watchee(val pathMatchers: List<PathMatcher>, val events: Set<WatchEvent>) {
    fun match(path: Path, event: VFileEvent): Boolean {
        if (!this.events.contains(WatchEvent.fromVFileEvent(event))) {
            return false
        }
        return this.pathMatchers.any { it.matches(path) }
    }
}
