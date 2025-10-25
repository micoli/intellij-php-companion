package org.micoli.php.service.easterEgg

import java.awt.Point
import kotlin.math.abs

class AutoGamer {
    fun updateComputerPlayer(snakeGame: Engine) {
        val computerHead = snakeGame.computerPlayer.head()

        if (snakeGame.foods.isNotEmpty()) {
            val targetFood =
                snakeGame.foods.minByOrNull { food ->
                    abs(food.x - computerHead.x) + abs(food.y - computerHead.y)
                }!!

            val possibleDirections = mutableListOf<Engine.Direction>()

            if (targetFood.x < computerHead.x &&
                !snakeGame.computerPlayer.direction.isOpposite(Engine.Direction.LEFT)) {
                possibleDirections.add(Engine.Direction.LEFT)
            }
            if (targetFood.x > computerHead.x &&
                !snakeGame.computerPlayer.direction.isOpposite(Engine.Direction.RIGHT)) {
                possibleDirections.add(Engine.Direction.RIGHT)
            }
            if (targetFood.y < computerHead.y &&
                !snakeGame.computerPlayer.direction.isOpposite(Engine.Direction.UP)) {
                possibleDirections.add(Engine.Direction.UP)
            }
            if (targetFood.y > computerHead.y &&
                !snakeGame.computerPlayer.direction.isOpposite(Engine.Direction.DOWN)) {
                possibleDirections.add(Engine.Direction.DOWN)
            }

            val safeDirections =
                possibleDirections.filter { dir ->
                    val testPoint =
                        when (snakeGame.wallMode) {
                            Engine.WallMode.CIRCULAR_BORDER ->
                                Point(
                                    (computerHead.x + dir.dx + Engine.GRID_WIDTH) %
                                        Engine.GRID_WIDTH,
                                    (computerHead.y + dir.dy + Engine.GRID_HEIGHT) %
                                        Engine.GRID_HEIGHT)
                            Engine.WallMode.HARD_BORDER ->
                                Point(computerHead.x + dir.dx, computerHead.y + dir.dy)
                        }
                    testPoint.x in 0..<Engine.GRID_WIDTH &&
                        testPoint.y in 0..<Engine.GRID_HEIGHT &&
                        !snakeGame.computerPlayer.has(testPoint) &&
                        !snakeGame.player.has(testPoint)
                }

            if (safeDirections.isNotEmpty()) {
                snakeGame.computerPlayer.direction = safeDirections.random()
            } else if (possibleDirections.isNotEmpty()) {
                snakeGame.computerPlayer.direction = possibleDirections.random()
            }
        }

        val newComputerHead =
            when (snakeGame.wallMode) {
                Engine.WallMode.CIRCULAR_BORDER ->
                    Point(
                        (computerHead.x +
                            snakeGame.computerPlayer.direction.dx +
                            Engine.GRID_WIDTH) % Engine.GRID_WIDTH,
                        (computerHead.y +
                            snakeGame.computerPlayer.direction.dy +
                            Engine.GRID_HEIGHT) % Engine.GRID_HEIGHT)
                Engine.WallMode.HARD_BORDER ->
                    Point(
                        computerHead.x + snakeGame.computerPlayer.direction.dx,
                        computerHead.y + snakeGame.computerPlayer.direction.dy)
            }

        if ((newComputerHead.x !in 0..<Engine.GRID_WIDTH) ||
            (newComputerHead.y !in 0..<Engine.GRID_HEIGHT) ||
            snakeGame.computerPlayer.has(newComputerHead) ||
            snakeGame.player.has(newComputerHead)) {
            snakeGame.gameOver(snakeGame.player)
            return
        }

        snakeGame.computerPlayer.newHead(newComputerHead)

        var computerAte = false
        if (snakeGame.foods.contains(newComputerHead)) {
            snakeGame.increaseComputerScore()
            snakeGame.foods.remove(newComputerHead)
            computerAte = true
            snakeGame.addFood()
        }

        if (!computerAte) {
            snakeGame.computerPlayer.removeTail()
        }
    }
}
