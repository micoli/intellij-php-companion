package org.micoli.php.service.filesystem

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.util.messages.MessageBus
import java.nio.file.Paths

class FileListener<Id>(handler: VfsHandler<Id>, messageBus: MessageBus?) {
    var isEnabled: Boolean = false

    interface VfsHandler<T> {
        fun vfsHandle(id: T, file: VirtualFile)
    }

    val vfsListener: BulkFileListener
    private var patterns: MutableMap<Id, Watchee> = HashMap()

    init {
        this.vfsListener =
            object : BulkFileListener {
                override fun after(events: MutableList<out VFileEvent>) {
                    if (!isEnabled) {
                        return
                    }
                    events.forEach { event: VFileEvent ->
                        val file = event.file ?: return@forEach
                        val path = Paths.get(file.path)
                        patterns.forEach { (id: Id, watchee: Watchee) ->
                            if (watchee.match(path, event)) {
                                handler.vfsHandle(id, file)
                            }
                        }
                    }
                }
            }
        subscribe(messageBus)
    }

    private fun subscribe(messageBus: MessageBus?) {
        if (messageBus == null) return
        messageBus
            .connect()
            .subscribe<BulkFileListener>(VirtualFileManager.VFS_CHANGES, vfsListener)
    }

    fun setPatterns(patterns: MutableMap<Id, Watchee>) {
        this.isEnabled = true
        this.patterns = patterns
    }

    fun getPatterns(): MutableMap<Id, Watchee> {
        return this.patterns
    }

    fun reset() {
        this.isEnabled = false
        this.patterns = HashMap()
    }

    companion object {
        val LOGGER: Logger = Logger.getInstance(FileListener::class.java.getSimpleName())
    }
}
