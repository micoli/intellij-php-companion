package org.micoli.php.symfony.cliDumpParser.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.Presentation
import com.intellij.openapi.project.Project

abstract class AbstractParseCliDumperAction : AnAction() {
    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    override fun actionPerformed(e: AnActionEvent) {
        val selection = getCurrentSelection(e) ?: return
        val project = e.project ?: return
        parseCliDumperOutput(selection, project)
    }

    override fun update(e: AnActionEvent) {
        val isVisible = getCurrentSelection(e) != null
        e.presentation.isVisible = isVisible
        e.presentation.isEnabled = isVisible
        setPresentationText(e.presentation, isVisible)
    }

    private fun getCurrentSelection(e: AnActionEvent): String? {
        val project = e.project
        val editor = e.getData(CommonDataKeys.EDITOR)

        if (editor == null || project == null) {
            return null
        }
        val selectedText = editor.selectionModel.selectedText
        if (selectedText == null || selectedText.trim { it <= ' ' }.isEmpty()) {
            return null
        }

        return selectedText
    }

    protected abstract fun parseCliDumperOutput(text: String, project: Project)

    protected abstract fun setPresentationText(presentation: Presentation, isVisible: Boolean)
}
