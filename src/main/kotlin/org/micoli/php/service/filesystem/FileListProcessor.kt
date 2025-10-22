package org.micoli.php.service.filesystem

import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import java.io.IOException
import org.eclipse.jgit.ignore.IgnoreNode

object FileListProcessor {
    @JvmStatic
    fun findFilesFromSelectedFiles(
        selectedFiles: MutableList<VirtualFile>
    ): MutableList<VirtualFile> {
        val filesSet: MutableSet<VirtualFile> = LinkedHashSet()

        for (file in selectedFiles) {
            if (file.isDirectory) {
                listFilesRecursively(file, filesSet)
                continue
            }
            filesSet.add(file)
        }
        return ArrayList(filesSet)
    }

    @JvmStatic
    fun filterFiles(
        ignoreFile: VirtualFile?,
        filesSet: MutableList<VirtualFile>
    ): MutableList<VirtualFile> {
        val ignoreNode = getIgnoreNode(ignoreFile) ?: return ArrayList(filesSet)

        return filesSet
            .stream()
            .filter { it != null }
            .filter {
                (ignoreNode.isIgnored(VfsUtil.getRelativePath(it, ignoreFile!!.parent), false) !=
                    IgnoreNode.MatchResult.IGNORED)
            }
            .toList()
    }

    private fun getIgnoreNode(ignoreFile: VirtualFile?): IgnoreNode? {
        if (ignoreFile == null) {
            return null
        }
        try {
            val ignoreNode = IgnoreNode()
            ignoreFile.inputStream.use { reader -> ignoreNode.parse(reader) }
            return ignoreNode
        } catch (_: IOException) {
            return null
        }
    }

    private fun listFilesRecursively(directory: VirtualFile, filesList: MutableSet<VirtualFile>) {
        val children = directory.children ?: return

        for (child in children) {
            if (child.isDirectory) {
                listFilesRecursively(child, filesList)
                continue
            }
            filesList.add(child)
        }
    }
}
