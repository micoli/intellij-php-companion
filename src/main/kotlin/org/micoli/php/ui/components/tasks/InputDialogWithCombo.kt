package org.micoli.php.ui.components.tasks

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.JBUI
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

class InputDialogWithCombo(
    project: Project,
    private val message: String,
    dialogTitle: String,
    selectValues: List<String>,
    selectInitialValue: String
) : DialogWrapper(project) {

    private val textField = JBTextField(20)
    private val comboBox = ComboBox(selectValues.toTypedArray())

    init {
        init()
        title = dialogTitle
        comboBox.selectedItem = selectInitialValue
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel(GridBagLayout())
        val gbc = GridBagConstraints()

        gbc.gridx = 0
        gbc.gridy = 0
        gbc.anchor = GridBagConstraints.WEST
        gbc.insets = JBUI.insetsBottom(10)
        panel.add(JLabel(message), gbc)

        gbc.gridy = 1
        gbc.fill = GridBagConstraints.HORIZONTAL
        gbc.weightx = 1.0
        panel.add(textField, gbc)

        gbc.gridy = 2
        gbc.insets = JBUI.insetsTop(10)
        panel.add(comboBox, gbc)

        return panel
    }

    override fun getPreferredFocusedComponent(): JComponent = textField

    fun getInputText(): String = textField.text

    fun getSelectedItem(): String = comboBox.selectedItem as? String ?: ""
}
