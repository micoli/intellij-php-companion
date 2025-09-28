package org.micoli.php.service.filesystem

import com.intellij.openapi.vfs.newvfs.events.VFileContentChangeEvent
import com.intellij.openapi.vfs.newvfs.events.VFileCopyEvent
import com.intellij.openapi.vfs.newvfs.events.VFileCreateEvent
import com.intellij.openapi.vfs.newvfs.events.VFileDeleteEvent
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.openapi.vfs.newvfs.events.VFileMoveEvent
import com.intellij.openapi.vfs.newvfs.events.VFilePropertyChangeEvent

enum class WatchEvent {
    CREATE,
    CONTENT_CHANGED,
    COPY,
    DELETE,
    MOVE,
    PROPERTY_CHANGED;

    companion object {
        fun all(): Set<WatchEvent> {
            return setOf(CREATE, CONTENT_CHANGED, COPY, DELETE, MOVE, PROPERTY_CHANGED)
        }

        fun fromVFileEvent(vFileEvent: VFileEvent): WatchEvent {
            return when (vFileEvent) {
                is VFileCreateEvent -> CREATE
                is VFileContentChangeEvent -> CONTENT_CHANGED
                is VFileCopyEvent -> COPY
                is VFileDeleteEvent -> DELETE
                is VFileMoveEvent -> MOVE
                is VFilePropertyChangeEvent -> PROPERTY_CHANGED
                else ->
                    throw IllegalArgumentException(
                        "Unknown VFileEvent type: ${vFileEvent.javaClass.name}")
            }
        }
    }
}
