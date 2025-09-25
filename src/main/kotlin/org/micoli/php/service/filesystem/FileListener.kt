package org.micoli.php.service.filesystem

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import java.nio.file.PathMatcher
import java.nio.file.Paths
import java.util.function.Consumer

class FileListener<Id>(handler: VfsHandler<Id>) {
    var isEnabled: Boolean = false

    interface VfsHandler<T> {
        fun vfsHandle(id: T, file: VirtualFile)
    }

    val vfsListener: BulkFileListener
    private var patterns: MutableMap<Id, MutableList<PathMatcher>> = HashMap()

    init {
        this.vfsListener =
            object : BulkFileListener {
                override fun after(events: MutableList<out VFileEvent>) {
                    events.forEach { event: VFileEvent? ->
                        if (!isEnabled) {
                            return@forEach
                        }
                        val file = event!!.file
                        if (file == null || file.isDirectory) {
                            return@forEach
                        }
                        val path = Paths.get(file.path)
                        patterns.forEach { (id: Id, pathMatchers: MutableList<PathMatcher>) ->
                            pathMatchers.forEach(
                                Consumer { pathMatcher: PathMatcher ->
                                    if (pathMatcher.matches(path)) {
                                        handler.vfsHandle(id, file)
                                    }
                                })
                        }
                    }
                }
            }
    }

    fun setPatterns(patterns: MutableMap<Id, MutableList<PathMatcher>>) {
        this.isEnabled = true
        this.patterns = patterns
    }

    fun getPatterns(): MutableMap<Id, MutableList<PathMatcher>> {
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
