package org.micoli.php.service.easterEgg

import com.intellij.codeHighlighting.BackgroundEditorHighlighter
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorLocation
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.FileEditorPolicy
import com.intellij.openapi.fileEditor.FileEditorProvider
import com.intellij.openapi.fileEditor.FileEditorState
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.UserDataHolderBase
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.LightVirtualFile
import java.beans.PropertyChangeListener
import javax.swing.JComponent

class EditorProvider : FileEditorProvider, DumbAware {
    companion object {
        private const val EDITOR_TYPE_ID = "5n4k3-game-editor"

        fun createGameEditorPanel(project: Project) {
            FileEditorManager.getInstance(project).openFile(LightVirtualFile("5n4k3.game"), true)
        }
    }

    override fun accept(project: Project, file: VirtualFile): Boolean {
        return file.name == "5n4k3.game"
    }

    override fun getEditorTypeId(): String = EDITOR_TYPE_ID

    override fun getPolicy(): FileEditorPolicy = FileEditorPolicy.HIDE_DEFAULT_EDITOR

    override fun createEditor(project: Project, file: VirtualFile): FileEditor {
        return object : UserDataHolderBase(), FileEditor {
            private val gamePanel = Panel()

            override fun getComponent(): JComponent = gamePanel

            override fun getPreferredFocusedComponent(): JComponent = gamePanel

            override fun getName(): String = "Snake Game"

            override fun setState(state: FileEditorState) {}

            override fun isModified(): Boolean = false

            override fun isValid(): Boolean = true

            override fun getFile(): VirtualFile = file

            override fun addPropertyChangeListener(listener: PropertyChangeListener) {}

            override fun removePropertyChangeListener(listener: PropertyChangeListener) {}

            override fun getBackgroundHighlighter(): BackgroundEditorHighlighter? = null

            override fun getCurrentLocation(): FileEditorLocation? = null

            override fun dispose() {
                gamePanel.dispose()
            }
        }
    }
}
