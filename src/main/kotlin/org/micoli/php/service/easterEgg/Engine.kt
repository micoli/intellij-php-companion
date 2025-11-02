package org.micoli.php.service.easterEgg

import java.awt.Color
import java.awt.Point
import kotlin.math.max
import kotlin.random.Random

class Engine {
    companion object {
        const val GRID_WIDTH = 30
        const val GRID_HEIGHT = 20
        const val TIMER_DELAY = 50
        const val INITIAL_GAME_SPEED = 200
        const val MAX_GAME_SPEED = 50
    }

    var gameSpeed = INITIAL_GAME_SPEED
    private var updateCounter = 0

    enum class WallMode {
        CIRCULAR_BORDER,
        HARD_BORDER;

        override fun toString(): String {
            return when (this) {
                HARD_BORDER -> "Hard"
                CIRCULAR_BORDER -> "Circular"
            }
        }

        fun cycle(): WallMode {
            if (this == CIRCULAR_BORDER) {
                return HARD_BORDER
            }
            return CIRCULAR_BORDER
        }
    }

    enum class GameMode {
        SOLO,
        VS_COMPUTER
    }

    enum class Direction(val dx: Int, val dy: Int) {
        UP(0, -1),
        DOWN(0, 1),
        LEFT(-1, 0),
        RIGHT(1, 0);

        fun isOpposite(other: Direction): Boolean {
            return this.dx + other.dx == 0 && this.dy + other.dy == 0
        }

        fun opposite(): Direction {
            return when (this) {
                UP -> DOWN
                DOWN -> UP
                LEFT -> RIGHT
                RIGHT -> LEFT
            }
        }
    }

    val player = Entity(Color(80, 180, 80), Color(100, 200, 100))
    val computerPlayer = Entity(Color(180, 80, 180), Color(200, 100, 200))

    var gameMode = GameMode.SOLO
    var wallMode = WallMode.CIRCULAR_BORDER
    var computerGamer = AutoGamer()
    val foods = mutableListOf<Point>()

    private var nextDirection = Direction.RIGHT

    var isGameOver = false
        private set

    var playerWon = false
        private set

    var isPaused = false
        private set

    var score = 0
        private set

    var computerScore = 0
        private set

    fun increaseComputerScore() {
        computerScore += 10
    }

    private val random = Random.Default

    init {
        reset()
    }

    fun reset() {
        gameSpeed = INITIAL_GAME_SPEED
        updateCounter = 0
        player.init(Point(GRID_WIDTH / 4, GRID_HEIGHT / 2))
        computerPlayer.init(Point(3 * GRID_WIDTH / 4, GRID_HEIGHT / 2))

        nextDirection = Direction.RIGHT
        isGameOver = false
        playerWon = false
        isPaused = false
        score = 0
        computerScore = 0

        foods.clear()
        addFood()
    }

    fun update() {
        if (isGameOver || isPaused) {
            return
        }

        updateCounter += TIMER_DELAY

        if (updateCounter < gameSpeed) {
            return
        }
        updateCounter = 0

        val head = player.head()
        val newHead =
            when (wallMode) {
                WallMode.CIRCULAR_BORDER ->
                    Point(
                        (head.x + player.direction.dx + GRID_WIDTH) % GRID_WIDTH,
                        (head.y + player.direction.dy + GRID_HEIGHT) % GRID_HEIGHT)
                WallMode.HARD_BORDER ->
                    Point(head.x + player.direction.dx, head.y + player.direction.dy)
            }

        if ((newHead.x !in 0..<GRID_WIDTH) ||
            (newHead.y !in 0..<GRID_HEIGHT) ||
            player.has(newHead) ||
            (gameMode == GameMode.VS_COMPUTER && computerPlayer.has(newHead))) {
            isGameOver = true
            playerWon = false
            return
        }

        player.newHead(newHead)

        var playerAte = false
        if (foods.contains(newHead)) {
            score += 10
            gameSpeed = max(gameSpeed - 10, MAX_GAME_SPEED)
            foods.remove(newHead)
            playerAte = true
            addFood()
            when (score) {
                50 -> addFood()
                80 -> addFood()
                110 -> addFood()
                140 -> addFood()
                170 -> addFood()
                200 -> addFood()
            }
        }

        if (!playerAte) {
            player.removeTail()
        }

        if (gameMode == GameMode.VS_COMPUTER) {
            computerGamer.updateComputerPlayer(this)
        }
    }

    fun setDirection(newDirection: Direction) {
        if (!player.direction.isOpposite(newDirection)) {
            player.direction = newDirection
        }
    }

    fun addFood() {
        val availableSpots = mutableListOf<Point>()
        for (x in 0 until GRID_WIDTH) {
            for (y in 0 until GRID_HEIGHT) {
                val point = Point(x, y)
                if (!player.has(point) && !computerPlayer.has(point) && !foods.contains(point)) {
                    availableSpots.add(point)
                }
            }
        }

        if (availableSpots.isNotEmpty()) {
            foods.add(availableSpots[random.nextInt(availableSpots.size)])
        }
    }

    fun togglePause() {
        isPaused = !isPaused
    }

    fun scoreAsText(): String {
        val displaySpeed =
            ((INITIAL_GAME_SPEED - gameSpeed) / (INITIAL_GAME_SPEED - MAX_GAME_SPEED).toFloat() *
                    100)
                .toInt()
        var score = "Speed: $displaySpeed% | Wall: $wallMode"
        if (gameMode == GameMode.VS_COMPUTER) {
            score += " | You: $score | Computer: $computerScore"
        }
        return score
    }

    fun gameOver(winner: Entity) {
        isGameOver = true
        playerWon =
            when (winner) {
                player -> true
                else -> false
            }
    }
}
