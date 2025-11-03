package org.micoli.php.ui.panels.symfonyProfiler.db

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.Project
import com.intellij.ui.EditorTextField
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.datatransfer.StringSelection
import javax.swing.JComponent
import org.micoli.php.service.SqlUtils
import org.micoli.php.symfony.profiler.models.DBQuery
import org.micoli.php.ui.Notification
import org.micoli.php.ui.PhpCompanionIcon
import org.micoli.php.ui.panels.symfonyProfiler.BackTraceTable

class SqlDetailPanel(project: Project, private val dbQuery: DBQuery, onBack: () -> Unit) :
    JBPanel<SqlDetailPanel>(BorderLayout()) {
    private val rightActionGroup = DefaultActionGroup()
    private val backTraceTable = BackTraceTable(project, dbQuery.backtrace)

    init {
        border = JBUI.Borders.empty()
        rightActionGroup.add(
            object : AnAction("Back", "Back to list", PhpCompanionIcon.Back) {
                override fun actionPerformed(p0: AnActionEvent) {
                    onBack()
                }
            })
        rightActionGroup.add(
            object : AnAction("Copy SQL", "Copy SQL to clipboard", PhpCompanionIcon.Copy) {
                override fun actionPerformed(p0: AnActionEvent) {
                    CopyPasteManager.getInstance().setContents(StringSelection(dbQuery.runnableSql))
                    Notification.getInstance(project)
                        .messageWithTimeout("Content copied to clipboard", 500)
                }
            })
        val rightToolbar =
            ActionManager.getInstance()
                .createActionToolbar(
                    "PhpCompanionProfilerRightToolbarSqlDetail", rightActionGroup, true)
        rightToolbar.targetComponent = this

        add(
            JBPanel<JBPanel<*>>(BorderLayout()).apply {
                add(rightToolbar.component, BorderLayout.CENTER)
                preferredSize = Dimension(preferredSize.width, 40) // Hauteur fixe
                minimumSize = Dimension(minimumSize.width, 40)
                maximumSize = Dimension(maximumSize.width, 40)
            },
            BorderLayout.NORTH)
        add(
            createSqlEditorPanel(project, dbQuery.runnableSql).apply {
                preferredSize = Dimension(preferredSize.width, 100)
                minimumSize = Dimension(minimumSize.width, 100)
                maximumSize = Dimension(maximumSize.width, 100)
            },
            BorderLayout.CENTER)
        add(JBScrollPane(backTraceTable), BorderLayout.SOUTH)
    }

    fun createSqlEditorPanel(project: Project, runnableSql: String): JComponent {
        val editor =
            EditorTextField(
                SqlUtils.Companion.formatSql(runnableSql),
                project,
                FileTypeManager.getInstance().findFileTypeByName("SQL"))

        editor.addSettingsProvider { editor ->
            editor.apply {
                setHorizontalScrollbarVisible(true)
                setVerticalScrollbarVisible(true)
                settings.apply {
                    isLineNumbersShown = true
                    isFoldingOutlineShown = false
                }
            }
        }

        return editor
    }
}
