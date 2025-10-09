package org.micoli.php.ui.panels.symfonyProfiler

import com.intellij.openapi.application.ApplicationManager
import com.intellij.ui.components.JBPanel
import java.awt.BorderLayout
import java.awt.CardLayout
import javax.swing.JLabel
import javax.swing.SwingConstants
import org.micoli.php.symfony.profiler.SymfonyProfileDTO

abstract class AbstractProfilePanel() : JBPanel<AbstractProfilePanel>(BorderLayout()) {
    var symfonyProfileDTO: SymfonyProfileDTO = SymfonyProfileDTO.EMPTY
    var lastToken: String = ""
    private val loaderCardLayout = CardLayout()
    private val loaderCardPanel = JBPanel<JBPanel<*>>(loaderCardLayout)

    companion object {
        private const val REFRESH_VIEW = "refresh"
        private const val MAIN_VIEW = "main"
    }

    init {
        val refreshPanel =
            JBPanel<JBPanel<*>>(BorderLayout()).apply {
                add(JLabel("Refreshing", SwingConstants.CENTER), BorderLayout.CENTER)
            }
        loaderCardPanel.add(refreshPanel, REFRESH_VIEW)
        loaderCardPanel.add(getMainPanel(), MAIN_VIEW)
        add(loaderCardPanel, BorderLayout.CENTER)
    }

    fun showLoading() {
        loaderCardLayout.show(loaderCardPanel, REFRESH_VIEW)
    }

    fun showMain() {
        loaderCardLayout.show(loaderCardPanel, MAIN_VIEW)
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
            showMain()
        }
    }

    abstract fun refresh()

    abstract fun getMainPanel(): JBPanel<*>
}
