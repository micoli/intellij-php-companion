package org.micoli.php.ui.components.tasks

import com.intellij.icons.AllIcons
import com.intellij.ui.components.JBTextField
import java.awt.BorderLayout
import java.awt.Cursor
import java.awt.Dimension
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JLabel
import javax.swing.JPanel

class JBLinkedTextField(
    private val buttonAction: (JBTextField) -> Unit,
    icon: javax.swing.Icon = AllIcons.Actions.Lightning,
) : JPanel(BorderLayout()) {

    val textField = JBTextField()
    private val iconLabel = JLabel(icon)

    init {
        add(textField, BorderLayout.CENTER)
        add(iconLabel, BorderLayout.EAST)

        iconLabel.cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        iconLabel.addMouseListener(
            object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    buttonAction(textField)
                }
            })

        iconLabel.preferredSize = Dimension(20, textField.preferredSize.height)
    }

    fun getText(): String = textField.text

    fun setText(text: String) {
        textField.text = text
    }
}
