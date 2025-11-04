package org.micoli.php.ui.panels.symfonyProfiler.detail

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.ui.JBSplitter
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import java.awt.BorderLayout
import javax.swing.JPanel
import org.micoli.php.service.intellij.psi.PhpUtil
import org.micoli.php.symfony.profiler.SymfonyProfileService
import org.micoli.php.symfony.profiler.models.RequestData
import org.micoli.php.ui.components.tasks.JBLinkedTextField
import org.micoli.php.ui.panels.symfonyProfiler.AbstractProfilePanel

class ProfileDetailPanel(project: Project) : AbstractProfilePanel(project) {
    val token = JBTextField()
    val method = JBTextField()
    val uri = JBTextField()
    val code = JBTextField()
    val type = JBTextField()
    val route = JBTextField()
    val controller = JBLinkedTextField({ PhpUtil.navigateToClassByFQN(project, it.getText()) })

    init {
        val contentPanel = JBSplitter(false, 0.50f)

        contentPanel.setFirstComponent(JBPanel<ProfileDetailPanel>(BorderLayout(0, 0)))
        contentPanel.firstComponent.add(
            FormBuilder.createFormBuilder()
                .addLabeledComponent("URI:", uri)
                .addComponentToRightColumn(method)
                .addLabeledComponent("Type:", type)
                .addLabeledComponent("Token:", token)
                .addComponentFillVertically(JPanel(), 0)
                .panel,
            BorderLayout.CENTER)

        contentPanel.setSecondComponent(JBPanel<ProfileDetailPanel>(BorderLayout(0, 0)))
        contentPanel.secondComponent.add(
            FormBuilder.createFormBuilder()
                .addLabeledComponent("Route:", route)
                .addComponentToRightColumn(controller)
                .addLabeledComponent("Code:", code)
                .addComponentFillVertically(JPanel(), 0)
                .panel,
            BorderLayout.CENTER)

        mainPanel.add(JBScrollPane(contentPanel), BorderLayout.CENTER)
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
        controller.setText("-")
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
                        controller.setText(item?.controller ?: "-")
                    })
        }
    }
}
