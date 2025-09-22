package org.micoli.php.ui.components.tasks.toolbar

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader.getIcon
import org.micoli.php.tasks.TasksService
import org.micoli.php.tasks.configuration.runnableTask.RunnableTaskConfiguration
import org.micoli.php.ui.PhpCompanionIcon

class TaskToolbarButton(private val project: Project, action: RunnableTaskConfiguration) :
  AnAction(action.label, action.label, getIcon(action.getIcon(), PhpCompanionIcon::class.java)) {
    private val taskId: String? = action.id

    override fun actionPerformed(anActionEvent: AnActionEvent) {
        TasksService.getInstance(project).runTask(taskId)
    }
}
