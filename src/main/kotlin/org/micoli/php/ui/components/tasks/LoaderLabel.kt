package org.micoli.php.ui.components.tasks

import com.intellij.ui.JBColor
import java.awt.*
import javax.swing.JLabel
import javax.swing.Timer

class LoaderLabel(initialText: String = "Loading...") : JLabel(initialText) {
    private val animationChars = arrayOf("⠋", "⠙", "⠹", "⠸", "⠼", "⠴", "⠦", "⠧", "⠇", "⠏")
    private var currentIndex = 0
    private var timer: Timer? = null
    private var baseText = initialText

    init {
        horizontalAlignment = CENTER
        verticalAlignment = CENTER
        font = Font("Arial", Font.PLAIN, 14)
        foreground = JBColor.LIGHT_GRAY

        startAnimation()
    }

    fun startAnimation() {
        if (timer?.isRunning == true) return

        timer =
            Timer(100) {
                    currentIndex = (currentIndex + 1) % animationChars.size
                    text = "${animationChars[currentIndex]} $baseText"
                }
                .apply { start() }
    }

    fun stopAnimation() {
        timer?.stop()
        timer = null
        text = baseText
    }

    fun setLabel(newText: String) {
        baseText = newText
        if (timer?.isRunning != true) {
            text = newText
        }
    }
}
