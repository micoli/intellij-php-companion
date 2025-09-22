package org.micoli.php.ui

import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DefaultActionGroup
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidget.TextPresentation
import com.intellij.openapi.wm.StatusBarWidgetFactory
import com.intellij.util.Consumer
import java.awt.event.MouseEvent
import org.jetbrains.annotations.Nls
import org.micoli.php.exportSourceToMarkdown.ExportSourceToMarkdownService
import org.micoli.php.symfony.messenger.service.MessengerService
import org.micoli.php.tasks.TasksService

class PhpCompanionStatusBarWidget(private val project: Project) : StatusBarWidget, TextPresentation {
    override fun ID(): String {
        return "PHPCompanion"
    }

    override fun getPresentation(): StatusBarWidget.WidgetPresentation {
        return this
    }

    override fun install(statusBar: StatusBar) {}

    override fun getText(): String {
        return "Companion"
    }

    override fun getAlignment(): Float {
        return 0f
    }

    override fun getTooltipText(): String {
        return "PHP Companion Options"
    }

    override fun getClickConsumer(): Consumer<MouseEvent> {
        return Consumer { e: MouseEvent? ->
            if (!e!!.isPopupTrigger && MouseEvent.BUTTON1 == e.getButton()) {
                showPopup(e)
            }
        }
    }

    private fun showPopup(e: MouseEvent) {
        val group = DefaultActionGroup()

        group.add(
          object : ToggleAction("SourceExport: Use .aiignore File", "", null) {
              override fun getActionUpdateThread(): ActionUpdateThread {
                  return ActionUpdateThread.BGT
              }

              override fun isSelected(e: AnActionEvent): Boolean {
                  return ExportSourceToMarkdownService.getInstance(project).useIgnoreFile
              }

              override fun setSelected(e: AnActionEvent, state: Boolean) {
                  ExportSourceToMarkdownService.getInstance(project).toggleUseIgnoreFile()
              }
          }
        )
        group.add(
          object : ToggleAction("SourceExport: Use Contextual Namespaces", "", null) {
              override fun getActionUpdateThread(): ActionUpdateThread {
                  return ActionUpdateThread.BGT
              }

              override fun isSelected(e: AnActionEvent): Boolean {
                  return ExportSourceToMarkdownService.getInstance(project).useContextualNamespaces
              }

              override fun setSelected(e: AnActionEvent, state: Boolean) {
                  ExportSourceToMarkdownService.getInstance(project).toggleUseContextualNamespaces()
              }
          }
        )

        group.add(
          object : ToggleAction("SymfonyMessenger: Use Native GoTo Declaration", "", null) {
              override fun getActionUpdateThread(): ActionUpdateThread {
                  return ActionUpdateThread.BGT
              }

              override fun isSelected(e: AnActionEvent): Boolean {
                  return MessengerService.getInstance(project).configuration.useNativeGoToDeclaration
              }

              override fun setSelected(e: AnActionEvent, state: Boolean) {
                  MessengerService.getInstance(project).configuration.toggleUseNativeGoToDeclaration()
              }
          }
        )

        group.add(
          object : ToggleAction("Watchers: Enabled", "", null) {
              override fun getActionUpdateThread(): ActionUpdateThread {
                  return ActionUpdateThread.BGT
              }

              override fun isSelected(e: AnActionEvent): Boolean {
                  return TasksService.getInstance(project).isWatcherEnabled
              }

              override fun setSelected(e: AnActionEvent, state: Boolean) {
                  TasksService.getInstance(project).toggleWatcherEnabled()
              }
          }
        )

        JBPopupFactory.getInstance()
          .createActionGroupPopup("Options", group, DataManager.getInstance().getDataContext(e.component), JBPopupFactory.ActionSelectionAid.SPEEDSEARCH, true)
          .showUnderneathOf(e.component)
    }

    class Factory : StatusBarWidgetFactory {
        override fun getId(): String {
            return "PHPCompanion"
        }

        override fun getDisplayName(): @Nls String {
            return "PHP Companion"
        }

        override fun isAvailable(project: Project): Boolean {
            return true
        }

        override fun createWidget(project: Project): StatusBarWidget {
            return PhpCompanionStatusBarWidget(project)
        }

        override fun disposeWidget(widget: StatusBarWidget) {
            widget.dispose()
        }
    }
}
