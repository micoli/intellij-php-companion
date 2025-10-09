package org.micoli.php.ui.panels.symfonyProfiler.detail

import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBPanel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import java.awt.BorderLayout
import javax.swing.JPanel
import org.micoli.php.ui.panels.symfonyProfiler.AbstractProfilePanel

class ProfileDetailPanel(val project: Project) : AbstractProfilePanel() {
    lateinit var method: JBTextField
    lateinit var uri: JBTextField
    lateinit var code: JBTextField
    lateinit var type: JBTextField

    override fun getMainPanel(): JBPanel<*> {
        method = JBTextField()
        uri = JBTextField()
        code = JBTextField()
        type = JBTextField()

        val mainPanel = JBPanel<ProfileDetailPanel>()
        mainPanel.setLayout(BorderLayout(0, 0))
        mainPanel.add(
            FormBuilder.createFormBuilder()
                .addLabeledComponent("Method:", method)
                .addLabeledComponent("URI:", uri)
                .addLabeledComponent("Code:", code)
                .addLabeledComponent("Type:", type)
                .addComponentFillVertically(JPanel(), 0)
                .panel,
            BorderLayout.CENTER)
        return mainPanel
    }

    override fun refresh() {
        method.text = symfonyProfileDTO.method
        uri.text = symfonyProfileDTO.url
        code.text = symfonyProfileDTO.statusCode
        type.text = symfonyProfileDTO.type
        lastToken = symfonyProfileDTO.token
    }
}
