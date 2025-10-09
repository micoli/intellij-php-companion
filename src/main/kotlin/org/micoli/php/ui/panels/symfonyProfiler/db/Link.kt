package org.micoli.php.ui.panels.symfonyProfiler.db

import java.awt.Cursor
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JLabel

class Link(val labelText: String = "", onBack: () -> Unit) : JLabel() {
    fun labeledColor(color: String) =
        """
                <html>
                    <body style="font-family: sans-serif;">
                        <a href="#" style="color: ${color}; text-decoration: none; font-size: 12px;">
                            $labelText
                        </a>
                    </body>
                </html>
            """
            .trimIndent()

    init {
        text = labeledColor("#4A90E2")
        cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        addMouseListener(
            object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    onBack()
                }

                override fun mouseEntered(e: MouseEvent) {
                    text = labeledColor("#2E6BA8")
                }

                override fun mouseExited(e: MouseEvent) {
                    text = labeledColor("#4A90E2")
                }
            })
    }
}
