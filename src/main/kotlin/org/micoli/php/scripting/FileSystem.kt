package org.micoli.php.scripting

import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import java.io.File
import java.io.IOException
import org.eclipse.jgit.ignore.IgnoreNode
import org.micoli.php.service.filesystem.PathUtil.getBaseDir
import org.micoli.php.ui.Notification.Companion.getInstance

interface ScriptFileSystem {
    fun clearPath(path: String)

    fun clearPath(path: String, mustBeGitIgnored: Boolean)
}

/** known as `fs` in scripting engine */
class FileSystem(private val project: Project) : ScriptFileSystem {
    private val localFileSystem: LocalFileSystem = LocalFileSystem.getInstance()

    /**
     * Removes a path and it's sub content. Path must be ignored by GIT.
     *
     * @param path the relative filepath
     */
    override fun clearPath(path: String) {
        internalClearPath(path, true)
    }

    /**
     * Removes a path and it's sub content.
     *
     * @param path the relative filepath
     * @param mustBeGitIgnored if false, the path will be removed even if it's not ignored by GIT.
     */
    override fun clearPath(path: String, mustBeGitIgnored: Boolean) {
        internalClearPath(path, mustBeGitIgnored)
    }

    private fun internalClearPath(path: String, mustBeGitIgnored: Boolean) {
        try {
            val virtualBaseDir =
                getBaseDir(project) ?: throw ScriptingError("No base directory found.")

            val virtualPath = getVirtualPath(virtualBaseDir, path)

            if (virtualPath == null || virtualPath.canonicalPath == null) {
                return
            }

            if (virtualBaseDir.canonicalPath == null) {
                return
            }

            if (!virtualPath.canonicalPath!!.startsWith(virtualBaseDir.canonicalPath!!)) {
                return
            }

            if (mustBeGitIgnored && !isIgnored(virtualBaseDir, virtualPath)) {
                throw ScriptingError("The path is not ignored by git: $path")
            }

            val virtualParent = virtualPath.parent
            val parentPath = virtualParent.canonicalPath

            WriteAction.run<IOException?> { virtualPath.delete(this) }

            if (parentPath != null) {
                this.localFileSystem.refreshAndFindFileByIoFile(File(parentPath))
            }
        } catch (e: ScriptingError) {
            getInstance(project).error(e.message!!)
        } catch (e: Exception) {
            LOG.warn("Error while cleaning path: " + path + " " + e.message)
        }
    }

    private fun getVirtualPath(virtualBaseDir: VirtualFile, path: String): VirtualFile? {
        val virtualPath = virtualBaseDir.findFileByRelativePath(path) ?: return null
        if (!virtualPath.exists()) {
            return null
        }
        return virtualPath
    }

    private fun isIgnored(virtualRoot: VirtualFile, virtualFile: VirtualFile): Boolean {
        var currentPath = virtualFile.parent
        while (currentPath != null && currentPath != virtualRoot.parent) {
            val ignoreFile = currentPath.findChild(".gitignore")
            if (ignoreFile == null) {
                currentPath = currentPath.parent
                continue
            }
            val ignoreNode = IgnoreNode()
            try {
                ignoreFile.inputStream.use {
                    ignoreNode.parse(it)
                    val ignored = ignoreNode.isIgnored(virtualFile.name, currentPath.isDirectory)
                    if (ignored == IgnoreNode.MatchResult.IGNORED) {
                        return true
                    }
                }
            } catch (_: IOException) {
                return false
            }
            currentPath = currentPath.parent
        }
        return false
    }

    companion object {
        private val LOG = Logger.getInstance(FileSystem::class.java.getSimpleName())
    }
}
