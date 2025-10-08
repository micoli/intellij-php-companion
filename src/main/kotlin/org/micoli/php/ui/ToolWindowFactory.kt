package org.micoli.php.ui

import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

internal class ToolWindowFactory : ToolWindowFactory, DumbAware {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val toolWindowContent = ToolWindowContent(project)
        toolWindow.contentManager.addContent(
            ContentFactory.getInstance().createContent(toolWindowContent.contentPanel, "", false))
        toolWindow.setTitleActions(toolWindowContent.titleActions)
    }
}
