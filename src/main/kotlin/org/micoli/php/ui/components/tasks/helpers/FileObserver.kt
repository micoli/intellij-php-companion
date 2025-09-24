package org.micoli.php.ui.components.tasks.helpers

import com.intellij.openapi.application.WriteAction
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader.getIcon
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VfsUtilCore
import com.intellij.openapi.vfs.VirtualFile
import java.io.IOException
import javax.swing.Icon
import org.micoli.php.service.filesystem.PathUtil
import org.micoli.php.tasks.configuration.runnableTask.ObservedFile
import org.micoli.php.ui.Notification
import org.micoli.php.ui.PhpCompanionIcon

class FileObserver(private val project: Project, val observedFile: ObservedFile) {
    class IconAndPrefix(var icon: Icon?, var innerPrefix: String?) {
        fun getPrefix(): String? {
            return innerPrefix
        }
    }

    private val projectRoot: VirtualFile? = PathUtil.getBaseDir(project)
    val activeRegularExpression: String = "^" + observedFile.variableName + "="
    val disabledRegularExpression: String =
        "^" + observedFile.commentPrefix + "\\s*" + observedFile.variableName + "="

    enum class Status {
        Active,
        Inactive,
        Unknown,
    }

    init {
        getStatus()
    }

    fun toggle(): Boolean {
        return when (getStatus()) {
            Status.Active -> {
                replaceInFile(false)
                true
            }

            Status.Inactive -> {
                replaceInFile(true)
                true
            }

            else -> false
        }
    }

    val iconAndPrefix: IconAndPrefix
        get() =
            when (this.getStatus()) {
                Status.Active ->
                    IconAndPrefix(
                        getIcon(observedFile.activeIcon, PhpCompanionIcon::class.java), "")

                Status.Inactive ->
                    IconAndPrefix(
                        getIcon(observedFile.inactiveIcon, PhpCompanionIcon::class.java), "# ")

                Status.Unknown ->
                    IconAndPrefix(
                        getIcon(observedFile.unknownIcon, PhpCompanionIcon::class.java), "? ")
            }

    fun getStatus(): Status {
        val relPath = observedFile.filePath ?: return Status.Unknown
        val file = projectRoot!!.findFileByRelativePath(relPath)
        if (file == null || !file.exists()) {
            return Status.Unknown
        }
        var result = Status.Unknown

        try {
            val content = VfsUtilCore.loadText(file)
            val lines = content.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

            for (line in lines) {
                if (line.matches(("$activeRegularExpression.*").toRegex())) {
                    result = Status.Active
                }
                if (line.matches(("$disabledRegularExpression.*").toRegex())) {
                    result = Status.Inactive
                }
            }
        } catch (e: IOException) {
            LOGGER.error(e)
        }
        return result
    }

    fun replaceInFile(toActive: Boolean) {
        val relPath = observedFile.filePath ?: return
        val file = projectRoot!!.findFileByRelativePath(relPath)
        if (file == null || !file.exists()) {
            return
        }

        try {
            WriteAction.run<IOException?> {
                VfsUtil.saveText(
                    file, replaceInFileContent(toActive, VfsUtilCore.loadText(file)).toString())
            }
        } catch (e: IOException) {
            LOGGER.error(e)
        }
        Notification.getInstance(project)
            .message(
                observedFile.variableName + " " + (if (toActive) "activated" else "deactivated"))
    }

    private fun replaceInFileContent(toActive: Boolean, content: String): StringBuilder {
        val result = StringBuilder()
        val lines = content.split("\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()

        for (line in lines) {
            if (toActive) {
                result.append(
                    line.replaceFirst(
                        disabledRegularExpression.toRegex(), observedFile.variableName + "="))
            } else {
                result.append(
                    line.replaceFirst(
                        activeRegularExpression.toRegex(),
                        observedFile.commentPrefix + observedFile.variableName + "=",
                    ))
            }
            result.append(System.lineSeparator())
        }
        return result
    }

    companion object {
        private val LOGGER = Logger.getInstance(FileObserver::class.java.getSimpleName())
    }
}
