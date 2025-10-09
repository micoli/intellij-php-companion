package org.micoli.php.ui.panels.symfonyProfiler

import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.FormBuilder
import java.awt.BorderLayout
import javax.swing.JPanel
import org.micoli.php.symfony.profiler.SymfonyProfileDTO

class ProfileDetailPanel : JPanel() {
    val method = JBTextField()
    val uri = JBTextField()
    val code = JBTextField()
    val type = JBTextField()

    init {
        setLayout(BorderLayout(0, 0))
        add(
            FormBuilder.createFormBuilder()
                .addLabeledComponent("Method:", method)
                .addLabeledComponent("URI:", uri)
                .addLabeledComponent("Code:", code)
                .addLabeledComponent("Type:", type)
                .addComponentFillVertically(JPanel(), 0)
                .panel,
            BorderLayout.CENTER)
    }

    fun setSymfonyProfile(symfonyProfileDTO: SymfonyProfileDTO) {
        method.text = symfonyProfileDTO.method
        uri.text = symfonyProfileDTO.url
        code.text = symfonyProfileDTO.statusCode
        type.text = symfonyProfileDTO.type
    }
}
