package org.micoli.php.exportSourceToMarkdown.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

abstract class AbstractExportSourceToMarkdownAction : AnAction() {
    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return

        val selectedFiles = e.getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY) ?: return
        doAction(project, selectedFiles)
    }

    override fun update(e: AnActionEvent) {
        val project = e.project
        val files = e.getData(PlatformDataKeys.VIRTUAL_FILE_ARRAY)

        e.presentation.isEnabledAndVisible = project != null && files != null && files.size > 0
    }

    protected abstract fun doAction(project: Project, selectedFiles: Array<VirtualFile>)
}
