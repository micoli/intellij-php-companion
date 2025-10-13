package org.micoli.php.ui.panels.symfonyProfiler

import com.intellij.openapi.application.ApplicationManager
import com.intellij.ui.components.JBPanel
import java.awt.BorderLayout
import java.awt.CardLayout
import javax.swing.JLabel
import javax.swing.SwingConstants
import org.micoli.php.symfony.profiler.SymfonyProfileDTO
import org.micoli.php.ui.components.tasks.LoaderLabel

abstract class AbstractProfilePanel() : JBPanel<AbstractProfilePanel>(BorderLayout()) {
    var symfonyProfileDTO: SymfonyProfileDTO = SymfonyProfileDTO.EMPTY
    var lastToken: String = ""
    private val mainCardLayout = CardLayout()
    private val mainCardPanel = JBPanel<JBPanel<*>>(mainCardLayout)
    protected val loaderLabel = LoaderLabel("Refreshing")
    protected val errorLabel = JLabel("", SwingConstants.CENTER)

    companion object {
        private const val LOADER_VIEW = "loader"
        private const val MAIN_VIEW = "main"
        private const val ERROR_VIEW = "error"
    }

    init {
        val refreshPanel =
            JBPanel<JBPanel<*>>(BorderLayout()).apply { add(loaderLabel, BorderLayout.CENTER) }
        val errorPanel =
            JBPanel<JBPanel<*>>(BorderLayout()).apply { add(errorLabel, BorderLayout.CENTER) }
        mainCardPanel.add(errorPanel, ERROR_VIEW)
        mainCardPanel.add(refreshPanel, LOADER_VIEW)
        mainCardPanel.add(getMainPanel(), MAIN_VIEW)
        add(mainCardPanel, BorderLayout.CENTER)
    }

    protected fun loaderLogCallback(startTime: Long): (String) -> Unit = {
        loaderLabel.setLabel(
            String.format(
                "%s (%.02f)", it, ((System.nanoTime() - startTime) / 1_000_000.0) / 1_000))
    }

    fun showLoading() {
        loaderLabel.setLabel("Loading...")
        loaderLabel.startAnimation()
        mainCardLayout.show(mainCardPanel, LOADER_VIEW)
    }

    fun showError(error: String) {
        errorLabel.text = error
        mainCardLayout.show(mainCardPanel, ERROR_VIEW)
    }

    fun showMain() {
        loaderLabel.setLabel("")
        mainCardLayout.show(mainCardPanel, MAIN_VIEW)
    }

    fun updateSymfonyProfileDTO(symfonyProfileDTO: SymfonyProfileDTO) {
        this.symfonyProfileDTO = symfonyProfileDTO
    }

    fun loadPanel() {
        if (lastToken == symfonyProfileDTO.token) return
        showLoading()
        ApplicationManager.getApplication().executeOnPooledThread {
            lastToken = symfonyProfileDTO.token
            refresh()
        }
    }

    abstract fun refresh()

    abstract fun getMainPanel(): JBPanel<*>
}
