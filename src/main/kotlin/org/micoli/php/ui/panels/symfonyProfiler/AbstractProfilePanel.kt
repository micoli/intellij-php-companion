package org.micoli.php.ui.panels.symfonyProfiler

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBPanel
import java.awt.BorderLayout
import java.awt.CardLayout
import javax.swing.JLabel
import javax.swing.SwingConstants
import org.micoli.php.symfony.profiler.SymfonyProfileDTO
import org.micoli.php.ui.components.tasks.LoaderLabel

abstract class AbstractProfilePanel(val project: Project) :
    JBPanel<AbstractProfilePanel>(CardLayout()) {
    var symfonyProfileDTO: SymfonyProfileDTO = SymfonyProfileDTO.EMPTY
    var lastToken: String = ""
    protected val loaderLabel = LoaderLabel("Refreshing")
    protected val errorLabel = JLabel("", SwingConstants.CENTER)
    val loaderPanel =
        JBPanel<JBPanel<*>>(BorderLayout()).apply { add(loaderLabel, BorderLayout.CENTER) }
    val errorPanel =
        JBPanel<JBPanel<*>>(BorderLayout()).apply { add(errorLabel, BorderLayout.CENTER) }
    val mainPanel = JBPanel<JBPanel<*>>(BorderLayout())

    companion object {
        private const val LOADER_VIEW = "loader"
        private const val MAIN_VIEW = "main"
        private const val ERROR_VIEW = "error"
    }

    protected fun initialize() {
        add(ERROR_VIEW, errorPanel)
        add(LOADER_VIEW, loaderPanel)
        add(MAIN_VIEW, mainPanel)
    }

    protected fun loaderLogCallback(startTime: Long): (String) -> Unit = {
        loaderLabel.setLabel(
            String.format(
                "%s (%.02f)", it, ((System.nanoTime() - startTime) / 1_000_000.0) / 1_000))
    }

    fun showLoading() {
        loaderLabel.setLabel("Loading...")
        loaderLabel.startAnimation()
        (layout as CardLayout).show(this, LOADER_VIEW)
    }

    fun showError(error: String) {
        errorLabel.text = error
        (layout as CardLayout).show(this, ERROR_VIEW)
    }

    fun showMainPanel() {
        loaderLabel.setLabel("")
        (layout as CardLayout).show(this, MAIN_VIEW)
    }

    fun updateSymfonyProfileDTO(symfonyProfileDTO: SymfonyProfileDTO) {
        this.symfonyProfileDTO = symfonyProfileDTO
    }

    fun refreshPanel() {
        if (lastToken == symfonyProfileDTO.token) return
        showLoading()
        ApplicationManager.getApplication().executeOnPooledThread {
            lastToken = symfonyProfileDTO.token
            refresh()
        }
    }

    abstract fun refresh()
}
