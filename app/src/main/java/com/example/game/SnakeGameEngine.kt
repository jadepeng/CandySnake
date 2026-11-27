package com.example.game

import kotlin.random.Random

data class TickOutcome(
    val ateFood: Boolean = false,
    val foodPoints: Int = 0,
    val foodPosition: GridPoint? = null,
    val atePowerUp: PowerUpType? = null,
    val powerUpPoints: Int = 0,
    val powerUpPosition: GridPoint? = null,
    val crashed: Boolean = false,
)

class SnakeGameEngine(
    val cols: Int = 16,
    val rows: Int = 18,
    private val random: Random = Random.Default,
) {
    private val directionQueue = ArrayDeque<Direction>(3)
    private var candiesUntilNextPowerUp: Int = 3

    var mode: GameMode = GameMode.CLASSIC
        private set
    var speedPreset: SpeedPreset = SpeedPreset.NORMAL
        private set

    var snake: List<GridPoint> = emptyList()
        private set
    var previousSnake: List<GridPoint> = emptyList()
        private set
    var direction: Direction = Direction.RIGHT
        private set
    var food: GridPoint = GridPoint(11, 9)
        private set
    var foodStyle: FoodStyle = FoodStyle.STRAWBERRY
        private set
    var boardPowerUp: BoardPowerUp? = null
        private set
    var activeEffects: MutableList<ActiveEffect> = mutableListOf()
        private set

    var score: Int = 0
        private set
    var candiesEaten: Int = 0
        private set
    var powerUpsUsed: Int = 0
        private set

    fun reset(mode: GameMode, speedPreset: SpeedPreset) {
        this.mode = mode
        this.speedPreset = speedPreset
        directionQueue.clear()
        direction = Direction.RIGHT

        val startY = rows / 2
        val startX = cols / 2 - 1
        snake = listOf(
            GridPoint(startX, startY),
            GridPoint(startX - 1, startY),
            GridPoint(startX - 2, startY),
            GridPoint(startX - 3, startY),
        )
        previousSnake = snake
        score = 0
        candiesEaten = 0
        powerUpsUsed = 0
        boardPowerUp = null
        activeEffects.clear()
        candiesUntilNextPowerUp = random.nextInt(3, 5)
        spawnFood()
    }

    fun enqueueDirection(candidate: Direction): Boolean {
        val lastPlanned = directionQueue.lastOrNull() ?: direction
        if (candidate == lastPlanned || candidate.isOpposite(lastPlanned)) {
            return false
        }
        if (directionQueue.size >= 2) {
            directionQueue.removeLast()
        }
        directionQueue.addLast(candidate)
        return true
    }

    fun currentTickIntervalMs(): Long {
        // Gentle speed curve as the snake grows
        val growthBonusMs = (candiesEaten * 2L).coerceAtMost(42L)
        var interval = (speedPreset.baseTickMs - growthBonusMs).coerceAtLeast(78L).toFloat()

        val hasSpeed = activeEffects.any { it.type == PowerUpType.SPEED_BOOST }
        val hasSlow = activeEffects.any { it.type == PowerUpType.SLOW_MO }
        if (hasSpeed && !hasSlow) {
            interval *= 0.72f
        } else if (hasSlow && !hasSpeed) {
            interval *= 1.42f
        }
        return interval.toLong().coerceIn(66L, 260L)
    }

    fun hasDoubleScore(): Boolean = activeEffects.any { it.type == PowerUpType.DOUBLE_SCORE }

    fun hasSpeedBoost(): Boolean = activeEffects.any { it.type == PowerUpType.SPEED_BOOST }

    fun advanceTimers(deltaMs: Long) {
        if (deltaMs <= 0L) return
        boardPowerUp = boardPowerUp?.let { item ->
            val left = item.remainingLifeMs - deltaMs
            if (left <= 0L) null else item.copy(remainingLifeMs = left)
        }
        val updated = activeEffects.mapNotNull { effect ->
            val left = effect.remainingMs - deltaMs
            if (left <= 0L) null else effect.copy(remainingMs = left)
        }
        activeEffects.clear()
        activeEffects.addAll(updated)
    }

    fun tick(): TickOutcome {
        previousSnake = snake
        if (directionQueue.isNotEmpty()) {
            val nextDir = directionQueue.removeFirst()
            if (!nextDir.isOpposite(direction)) {
                direction = nextDir
            }
        }

        val head = snake.first()
        var nextX = head.x + direction.dx
        var nextY = head.y + direction.dy

        if (mode == GameMode.ENDLESS) {
            nextX = (nextX + cols) % cols
            nextY = (nextY + rows) % rows
        } else {
            if (nextX !in 0 until cols || nextY !in 0 until rows) {
                return TickOutcome(crashed = true)
            }
        }

        val nextHead = GridPoint(nextX, nextY)
        val willEatFood = nextHead == food
        val hitPowerUp = boardPowerUp?.takeIf { it.position == nextHead }

        // Tail vacates on non-growing move, so stepping onto current tail is legal
        val collisionBody = if (willEatFood) snake else snake.dropLast(1)
        if (nextHead in collisionBody) {
            return TickOutcome(crashed = true)
        }

        val newSnake = buildList(snake.size + if (willEatFood) 1 else 0) {
            add(nextHead)
            addAll(if (willEatFood) snake else snake.dropLast(1))
        }
        snake = newSnake

        var foodPoints = 0
        var foodPos: GridPoint? = null
        var powerUpPoints = 0
        var powerUpPos: GridPoint? = null
        var eatenPowerUpType: PowerUpType? = null

        val scoreMultiplier = (if (hasDoubleScore()) 2f else 1f) *
            (if (hasSpeedBoost()) 1.5f else 1f)

        if (willEatFood) {
            candiesEaten++
            foodPoints = (10f * scoreMultiplier).toInt()
            score += foodPoints
            foodPos = nextHead
            spawnFood()

            candiesUntilNextPowerUp--
            if (candiesUntilNextPowerUp <= 0 && boardPowerUp == null) {
                spawnPowerUp()
                candiesUntilNextPowerUp = random.nextInt(3, 6)
            }
        }

        if (hitPowerUp != null) {
            boardPowerUp = null
            powerUpsUsed++
            eatenPowerUpType = hitPowerUp.type
            powerUpPos = nextHead

            when (hitPowerUp.type) {
                PowerUpType.RAINBOW_DONUT -> {
                    powerUpPoints = if (hasDoubleScore()) 100 else 50
                    score += powerUpPoints
                }
                PowerUpType.SPEED_BOOST -> {
                    powerUpPoints = if (hasDoubleScore()) 30 else 15
                    score += powerUpPoints
                    activateEffect(PowerUpType.SPEED_BOOST)
                    // Replace opposing slow effect so player gets crisp speed boost
                    activeEffects.removeAll { it.type == PowerUpType.SLOW_MO }
                }
                PowerUpType.SLOW_MO -> {
                    powerUpPoints = if (hasDoubleScore()) 20 else 10
                    score += powerUpPoints
                    activateEffect(PowerUpType.SLOW_MO)
                    activeEffects.removeAll { it.type == PowerUpType.SPEED_BOOST }
                }
                PowerUpType.DOUBLE_SCORE -> {
                    powerUpPoints = 20
                    score += powerUpPoints
                    activateEffect(PowerUpType.DOUBLE_SCORE)
                }
            }
        }

        return TickOutcome(
            ateFood = willEatFood,
            foodPoints = foodPoints,
            foodPosition = foodPos,
            atePowerUp = eatenPowerUpType,
            powerUpPoints = powerUpPoints,
            powerUpPosition = powerUpPos,
            crashed = false,
        )
    }

    private fun activateEffect(type: PowerUpType) {
        activeEffects.removeAll { it.type == type }
        activeEffects.add(
            ActiveEffect(
                type = type,
                remainingMs = type.durationMs,
                totalMs = type.durationMs,
            )
        )
    }

    private fun spawnFood() {
        val occupied = HashSet<GridPoint>(snake.size + 2)
        occupied.addAll(snake)
        boardPowerUp?.let { occupied.add(it.position) }
        val freeCells = ArrayList<GridPoint>(cols * rows - occupied.size)
        for (y in 0 until rows) {
            for (x in 0 until cols) {
                val p = GridPoint(x, y)
                if (p !in occupied) freeCells.add(p)
            }
        }
        if (freeCells.isNotEmpty()) {
            food = freeCells[random.nextInt(freeCells.size)]
            val styles = FoodStyle.entries
            foodStyle = styles[random.nextInt(styles.size)]
        }
    }

    private fun spawnPowerUp() {
        val occupied = HashSet<GridPoint>(snake.size + 2)
        occupied.addAll(snake)
        occupied.add(food)
        val freeCells = ArrayList<GridPoint>(cols * rows - occupied.size)
        for (y in 0 until rows) {
            for (x in 0 until cols) {
                val p = GridPoint(x, y)
                if (p !in occupied) freeCells.add(p)
            }
        }
        if (freeCells.isEmpty()) return

        val pos = freeCells[random.nextInt(freeCells.size)]
        val roll = random.nextInt(100)
        val type = when {
            roll < 30 -> PowerUpType.DOUBLE_SCORE
            roll < 55 -> PowerUpType.SPEED_BOOST
            roll < 80 -> PowerUpType.SLOW_MO
            else -> PowerUpType.RAINBOW_DONUT
        }
        boardPowerUp = BoardPowerUp(
            type = type,
            position = pos,
            remainingLifeMs = 9_500L,
            totalLifeMs = 9_500L,
        )
    }
}
