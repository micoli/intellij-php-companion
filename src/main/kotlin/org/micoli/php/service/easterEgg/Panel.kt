package org.micoli.php.service.easterEgg

import com.intellij.ui.Gray
import com.intellij.ui.JBColor
import com.intellij.util.ui.UIUtil
import java.awt.*
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import javax.swing.JPanel
import javax.swing.Timer

class Panel : JPanel() {
    private val cellSize = 20
    private val topMargin = 50
    private val game = Engine()
    private val gameTimer: Timer

    init {
        preferredSize = Dimension(Engine.GRID_WIDTH * cellSize, Engine.GRID_HEIGHT * cellSize + 50)

        isFocusable = true
        background = UIUtil.getPanelBackground()

        addKeyListener(
            object : KeyAdapter() {
                override fun keyPressed(e: KeyEvent) {
                    handleKeyPress(e)
                }
            })

        gameTimer =
            Timer(Engine.TIMER_DELAY) {
                game.update()
                repaint()
            }

        gameTimer.start()
    }

    private fun handleKeyPress(e: KeyEvent) {
        when (e.keyCode) {
            KeyEvent.VK_UP -> game.setDirection(Engine.Direction.UP)
            KeyEvent.VK_DOWN -> game.setDirection(Engine.Direction.DOWN)
            KeyEvent.VK_LEFT -> game.setDirection(Engine.Direction.LEFT)
            KeyEvent.VK_RIGHT -> game.setDirection(Engine.Direction.RIGHT)
            KeyEvent.VK_SPACE -> game.togglePause()
            KeyEvent.VK_R -> game.reset()
            KeyEvent.VK_G -> {
                game.gameMode =
                    if (game.gameMode == Engine.GameMode.SOLO) {
                        Engine.GameMode.VS_COMPUTER
                    } else {
                        Engine.GameMode.SOLO
                    }
                game.reset()
            }
            KeyEvent.VK_W -> {
                game.wallMode = game.wallMode.cycle()
            }
        }
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        val g2d = g as Graphics2D
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        g2d.color = Gray._30
        for (x in 0..Engine.GRID_WIDTH) {
            g2d.drawLine(
                x * cellSize,
                topMargin + 0,
                x * cellSize,
                topMargin + Engine.GRID_HEIGHT * cellSize)
        }
        for (y in 0..Engine.GRID_HEIGHT) {
            g2d.drawLine(
                0, topMargin + y * cellSize, Engine.GRID_WIDTH * cellSize, topMargin + y * cellSize)
        }

        game.player.segments {
            g2d.color = it.second
            g2d.fillRect(
                it.first.x * cellSize + 1,
                topMargin + it.first.y * cellSize + 1,
                cellSize - 2,
                cellSize - 2)
        }

        if (game.gameMode == Engine.GameMode.VS_COMPUTER) {
            game.computerPlayer.segments {
                g2d.color = it.second
                g2d.fillRect(
                    it.first.x * cellSize + 1,
                    topMargin + it.first.y * cellSize + 1,
                    cellSize - 2,
                    cellSize - 2)
            }
        }

        game.foods.forEach { food ->
            g2d.color = JBColor.RED
            g2d.fillOval(
                food.x * cellSize + 2,
                topMargin + food.y * cellSize + 2,
                cellSize - 4,
                cellSize - 4)
        }

        g2d.color = UIUtil.getLabelForeground()
        g2d.font = Font("Arial", Font.BOLD, 16)
        g2d.drawString(game.scoreAsText(), 5, 20)
        g2d.font = Font("Arial", Font.ITALIC, 12)
        g2d.drawString("Arrows to move, Space(Pause), R(Reset), G(Game mode), W(Wall mode)", 5, 45)

        if (game.isPaused) {
            drawCenteredText(g2d, "PAUSE", JBColor.YELLOW)
        }

        if (game.isGameOver) {
            val message =
                if (game.gameMode == Engine.GameMode.VS_COMPUTER) {
                    if (game.playerWon) "You won, R to restart" else "Compuer won, R to restart"
                } else {
                    "Game over, R to restart"
                }
            drawCenteredText(g2d, message, JBColor.RED)
        }
    }

    private fun drawCenteredText(g2d: Graphics2D, text: String, color: Color) {
        g2d.color = color
        g2d.font = Font("Arial", Font.BOLD, 30)
        val metrics = g2d.fontMetrics
        val x = (width - metrics.stringWidth(text)) / 2
        val y = topMargin + Engine.GRID_HEIGHT * cellSize / 2
        g2d.drawString(text, x, y)
    }

    fun dispose() {
        gameTimer.stop()
    }
}
