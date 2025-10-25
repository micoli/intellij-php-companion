package org.micoli.php.service.easterEgg

import com.intellij.ui.JBColor
import java.awt.Color
import java.awt.Point

class Entity(val color: Color, val darkColor: Color) {
    private val points = ArrayDeque<Point>()
    var direction: Engine.Direction = Engine.Direction.RIGHT

    fun init(startPoint: Point) {
        val startDirection = Engine.Direction.entries.random()
        direction = startDirection
        points.clear()
        points.add(Point(startPoint.x, startPoint.y))
        points.add(Point(startPoint.x + startDirection.opposite().dx * 1, startPoint.y))
        points.add(Point(startPoint.x + startDirection.opposite().dx * 2, startPoint.y))
    }

    fun head() = points.first()

    fun has(point: Point) = points.contains(point)

    fun newHead(point: Point) = points.addFirst(point)

    fun removeTail() = points.removeLast()

    fun segments(callback: (Pair<Point, JBColor>) -> Unit) {
        points.forEachIndexed { index, segment ->
            callback(
                Pair(
                    segment,
                    if (points.size == 1) {
                        JBColor(color, darkColor)
                    } else {
                        val ratio = index.toFloat() / (points.size - 1)
                        JBColor(
                            Color(
                                (color.red + (40 - color.red) * ratio).toInt(),
                                (color.green + (40 - color.green) * ratio).toInt(),
                                (color.blue + (40 - color.blue) * ratio).toInt()),
                            Color(
                                (darkColor.red + (40 - darkColor.red) * ratio).toInt(),
                                (darkColor.green + (40 - darkColor.green) * ratio).toInt(),
                                (darkColor.blue + (40 - darkColor.blue) * ratio).toInt()))
                    }))
        }
    }
}
