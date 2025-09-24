package org.micoli.php.ui.popup

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.highlighter.EditorHighlighterFactory
import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.ide.CopyPasteManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.datatransfer.StringSelection
import java.awt.event.ActionEvent
import javax.swing.*
import org.micoli.php.ui.Notification

class ParsedContentDisplayPopup(
    private val popupType: PopupType,
    private val project: Project,
    private val jsonContent: String,
) : DialogWrapper(project) {
    enum class PopupType {
        JSON,
        PHP,
        MARKDOWN,
    }

    private var editor: Editor? = null

    init {
        isModal = true
        isResizable = true

        init()
    }

    override fun createCenterPanel(): JComponent {
        val mainPanel = JPanel(BorderLayout())
        mainPanel.preferredSize = JBUI.size(600, 400)
        editor = createJsonEditor(popupType)
        val editorComponent = editor!!.component

        val scrollPane = JScrollPane(editorComponent)
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED)
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED)

        mainPanel.add(scrollPane, BorderLayout.CENTER)

        return mainPanel
    }

    private fun createJsonEditor(popupType: PopupType?): Editor {
        val fileType =
            FileTypeManager.getInstance()
                .getFileTypeByExtension(if (popupType == PopupType.PHP) "php" else "json")

        val editorFactory = EditorFactory.getInstance()
        val document = editorFactory.createDocument(jsonContent)
        val editor = editorFactory.createViewer(document, project)

        val settings = editor.settings
        settings.isLineNumbersShown = true
        settings.isFoldingOutlineShown = false
        settings.isLineMarkerAreaShown = false
        settings.isIndentGuidesShown = true
        settings.isVirtualSpace = false
        settings.isWheelFontChangeEnabled = false

        if (editor is EditorEx) {
            val highlighter =
                EditorHighlighterFactory.getInstance().createEditorHighlighter(project, fileType)
            editor.highlighter = highlighter
        }

        return editor
    }

    override fun createActions(): Array<Action> {
        return arrayOf(this.CopyAction(), cancelAction)
    }

    private inner class CopyAction : AbstractAction("Copy") {
        init {
            putValue(
                SHORT_DESCRIPTION, "Copy " + (if (popupType == PopupType.PHP) "PHP" else "JSON"))
        }

        override fun actionPerformed(e: ActionEvent?) {
            val stringSelection = StringSelection(jsonContent)
            CopyPasteManager.getInstance().setContents(stringSelection)
            Notification.getInstance(project).messageWithTimeout("Content copied to clipboard", 500)
            close(OK_EXIT_CODE)
        }
    }

    override fun doOKAction() {
        super.doOKAction()
    }

    override fun dispose() {
        if (editor != null) {
            EditorFactory.getInstance().releaseEditor(editor!!)
        }
        super.dispose()
    }

    companion object {
        @JvmStatic
        fun showJsonPopup(project: Project, jsonContent: String) {
            SwingUtilities.invokeLater {
                val dialog = ParsedContentDisplayPopup(PopupType.JSON, project, jsonContent)
                dialog.show()
            }
        }

        @JvmStatic
        fun showMarkdownPopup(project: Project, jsonContent: String) {
            SwingUtilities.invokeLater {
                val dialog = ParsedContentDisplayPopup(PopupType.MARKDOWN, project, jsonContent)
                dialog.show()
            }
        }

        @JvmStatic
        fun showPhpPopup(project: Project, jsonContent: String) {
            SwingUtilities.invokeLater {
                val dialog = ParsedContentDisplayPopup(PopupType.PHP, project, jsonContent)
                dialog.show()
            }
        }
    }
}
