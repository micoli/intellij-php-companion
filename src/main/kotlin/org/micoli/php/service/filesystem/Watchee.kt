package org.micoli.php.service.filesystem

import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import java.nio.file.Path
import java.nio.file.PathMatcher
import kotlin.collections.forEach

data class Watchee(val pathMatchers: MutableList<PathMatcher>, val events: Set<WatchEvent>) {
    fun match(path: Path, event: VFileEvent): Boolean {
        this.pathMatchers.forEach { pathMatcher ->
            return pathMatcher.matches(path) and
                this.events.contains(WatchEvent.fromVFileEvent(event))
        }
        return false
    }
}
