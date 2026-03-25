package org.micoli.php.code.actions

import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.command.WriteCommandAction
import java.util.UUID

class GenerateUuidAction : AnAction() {
    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return

        WriteCommandAction.runWriteCommandAction(project) {
            val document = editor.document

            editor.caretModel.allCarets.reversed().forEach { caret ->
                if (caret.hasSelection()) {
                    document.replaceString(caret.selectionStart, caret.selectionEnd, generateUUID())
                    caret.removeSelection()
                    return@forEach
                }
                document.insertString(caret.offset, generateUUID())
            }
        }
    }

    private fun generateUUID(): String = UUID.randomUUID().toString()

    override fun update(e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR)
        e.presentation.isEnabledAndVisible = e.project != null && editor != null
    }
}
