package org.micoli.php.ui.panels.symfonyProfiler.detail

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import java.awt.BorderLayout
import javax.swing.JPanel
import org.micoli.php.symfony.profiler.SymfonyProfileService
import org.micoli.php.symfony.profiler.models.RequestData
import org.micoli.php.ui.panels.symfonyProfiler.AbstractProfilePanel

class ProfileDetailPanel(project: Project) : AbstractProfilePanel(project) {
    val token = JBTextField()
    val method = JBTextField()
    val uri = JBTextField()
    val code = JBTextField()
    val type = JBTextField()
    val route = JBTextField()
    val controller = JBTextField()

    init {
        val panel = JBPanel<ProfileDetailPanel>(BorderLayout(0, 0))
        panel.add(
            FormBuilder.createFormBuilder()
                .addLabeledComponent("Method:", method)
                .addLabeledComponent("URI:", uri)
                .addLabeledComponent("Code:", code)
                .addLabeledComponent("Route:", route)
                .addLabeledComponent("Controller:", controller)
                .addLabeledComponent("Type:", type)
                .addLabeledComponent("Token:", token)
                .addComponentFillVertically(JPanel(), 0)
                .panel,
            BorderLayout.CENTER)
        mainPanel.add(JBScrollPane(panel), BorderLayout.CENTER)
        initialize()
    }

    override fun refresh() {
        token.text = symfonyProfileDTO.token
        method.text = symfonyProfileDTO.method
        uri.text = symfonyProfileDTO.url
        code.text = symfonyProfileDTO.statusCode
        type.text = symfonyProfileDTO.type
        lastToken = symfonyProfileDTO.token
        route.text = "-"
        controller.text = "-"
        showMainPanel()
        ApplicationManager.getApplication().invokeLater {
            SymfonyProfileService.getInstance(project)
                .loadProfilerDumpPage(
                    RequestData::class.java,
                    symfonyProfileDTO.token,
                    loaderLogCallback(System.nanoTime()),
                    { showError(it) },
                    { item ->
                        route.text = item?.route ?: "-"
                        controller.text = item?.controller ?: "-"
                    })
        }
    }
}
