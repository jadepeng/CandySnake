package com.example.game

import androidx.compose.ui.graphics.Color
import com.example.R

data class GridPoint(val x: Int, val y: Int)

enum class Direction(val dx: Int, val dy: Int) {
    UP(0, -1),
    DOWN(0, 1),
    LEFT(-1, 0),
    RIGHT(1, 0);

    fun isOpposite(other: Direction): Boolean = dx + other.dx == 0 && dy + other.dy == 0
}

enum class GameMode(val storageKey: String, val labelRes: Int, val descRes: Int) {
    CLASSIC("classic", R.string.mode_classic, R.string.mode_classic_desc),
    ENDLESS("endless", R.string.mode_endless, R.string.mode_endless_desc);

    companion object {
        fun fromKey(key: String?): GameMode =
            entries.firstOrNull { it.storageKey == key } ?: CLASSIC
    }
}

enum class ControlMode(val storageKey: String, val labelRes: Int, val hintRes: Int) {
    BOTH("both", R.string.control_both, R.string.control_hint_both),
    SWIPE_ONLY("swipe", R.string.control_swipe, R.string.control_hint_swipe),
    DPAD_ONLY("dpad", R.string.control_dpad, R.string.control_hint_dpad);

    fun next(): ControlMode = when (this) {
        BOTH -> SWIPE_ONLY
        SWIPE_ONLY -> DPAD_ONLY
        DPAD_ONLY -> BOTH
    }

    companion object {
        fun fromKey(key: String?): ControlMode =
            entries.firstOrNull { it.storageKey == key } ?: BOTH
    }
}

enum class SpeedPreset(val storageKey: String, val baseTickMs: Long, val labelRes: Int) {
    CHILL("chill", 190L, R.string.speed_chill),
    NORMAL("normal", 148L, R.string.speed_normal),
    TURBO("turbo", 112L, R.string.speed_turbo);

    companion object {
        fun fromKey(key: String?): SpeedPreset =
            entries.firstOrNull { it.storageKey == key } ?: NORMAL
    }
}

enum class SnakeSkin(
    val storageKey: String,
    val labelRes: Int,
    val primaryColor: Color,
    val secondaryColor: Color,
    val outlineColor: Color,
    val cheekColor: Color,
    val highlightColor: Color,
) {
    MINT(
        storageKey = "mint",
        labelRes = R.string.skin_mint,
        primaryColor = Color(0xFF2ED59E),
        secondaryColor = Color(0xFF7BF2C9),
        outlineColor = Color(0xFF179B71),
        cheekColor = Color(0xFFFF85A2),
        highlightColor = Color(0xFFE0FFF4),
    ),
    STRAWBERRY(
        storageKey = "strawberry",
        labelRes = R.string.skin_strawberry,
        primaryColor = Color(0xFFFF6492),
        secondaryColor = Color(0xFFFFA5C0),
        outlineColor = Color(0xFFD63266),
        cheekColor = Color(0xFFFFD166),
        highlightColor = Color(0xFFFFEBF1),
    ),
    MANGO(
        storageKey = "mango",
        labelRes = R.string.skin_mango,
        primaryColor = Color(0xFFFFB72B),
        secondaryColor = Color(0xFFFFDC7A),
        outlineColor = Color(0xFFD9840B),
        cheekColor = Color(0xFFFF6B8B),
        highlightColor = Color(0xFFFFF8DF),
    ),
    BLUEBERRY(
        storageKey = "blueberry",
        labelRes = R.string.skin_blueberry,
        primaryColor = Color(0xFF836FFF),
        secondaryColor = Color(0xFFB5A8FF),
        outlineColor = Color(0xFF5642C9),
        cheekColor = Color(0xFFFF8BB3),
        highlightColor = Color(0xFFF0EDFF),
    );

    companion object {
        fun fromKey(key: String?): SnakeSkin =
            entries.firstOrNull { it.storageKey == key } ?: MINT
    }
}

enum class FoodStyle(val primaryColor: Color, val stripeColor: Color) {
    STRAWBERRY(Color(0xFFFF4F7E), Color(0xFFFFB3C8)),
    LEMON_DROP(Color(0xFFFFC233), Color(0xFFFFF0B8)),
    GRAPE_GUMMY(Color(0xFFA855F7), Color(0xFFE2C3FF)),
    PEACH_POP(Color(0xFFFF8A5B), Color(0xFFFFD5C2)),
    BLUE_RASPBERRY(Color(0xFF38BDF8), Color(0xFFBAE6FD)),
}

enum class PowerUpType(
    val labelRes: Int,
    val badgeRes: Int,
    val durationMs: Long,
    val accentColor: Color,
) {
    SPEED_BOOST(
        labelRes = R.string.powerup_speed,
        badgeRes = R.string.powerup_speed_badge,
        durationMs = 6_000L,
        accentColor = Color(0xFFFF9F1C),
    ),
    DOUBLE_SCORE(
        labelRes = R.string.powerup_double,
        badgeRes = R.string.powerup_double_badge,
        durationMs = 8_000L,
        accentColor = Color(0xFFFF4D8D),
    ),
    SLOW_MO(
        labelRes = R.string.powerup_slow,
        badgeRes = R.string.powerup_slow_badge,
        durationMs = 7_000L,
        accentColor = Color(0xFF2EC4E6),
    ),
    RAINBOW_DONUT(
        labelRes = R.string.powerup_donut,
        badgeRes = R.string.powerup_donut,
        durationMs = 0L,
        accentColor = Color(0xFFA855F7),
    ),
}

data class BoardPowerUp(
    val type: PowerUpType,
    val position: GridPoint,
    val remainingLifeMs: Long,
    val totalLifeMs: Long = 9_500L,
) {
    val lifeFraction: Float
        get() = (remainingLifeMs.toFloat() / totalLifeMs.toFloat()).coerceIn(0f, 1f)
}

data class ActiveEffect(
    val type: PowerUpType,
    val remainingMs: Long,
    val totalMs: Long,
) {
    val fraction: Float
        get() = (remainingMs.toFloat() / totalMs.coerceAtLeast(1L).toFloat()).coerceIn(0f, 1f)
}

data class CandyParticle(
    val id: Long,
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val color: Color,
    val radius: Float,
    val life: Float, // 1f down to 0f
)

data class FloatingScore(
    val id: Long,
    val gridX: Float,
    val gridY: Float,
    val text: String,
    val color: Color,
    val progress: Float, // 0f up to 1f
)

enum class GamePhase {
    READY,
    PLAYING,
    PAUSED,
    GAME_OVER,
}

data class UserSettings(
    val selectedMode: GameMode = GameMode.CLASSIC,
    val controlMode: ControlMode = ControlMode.BOTH,
    val snakeSkin: SnakeSkin = SnakeSkin.MINT,
    val speedPreset: SpeedPreset = SpeedPreset.NORMAL,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
)

data class GameUiState(
    val cols: Int = 16,
    val rows: Int = 18,
    val phase: GamePhase = GamePhase.READY,
    val mode: GameMode = GameMode.CLASSIC,
    val snake: List<GridPoint> = emptyList(),
    val previousSnake: List<GridPoint> = emptyList(),
    val moveProgress: Float = 1f,
    val direction: Direction = Direction.RIGHT,
    val food: GridPoint = GridPoint(11, 9),
    val foodStyle: FoodStyle = FoodStyle.STRAWBERRY,
    val boardPowerUp: BoardPowerUp? = null,
    val activeEffects: List<ActiveEffect> = emptyList(),
    val score: Int = 0,
    val bestScore: Int = 0,
    val isNewRecord: Boolean = false,
    val candiesEaten: Int = 0,
    val powerUpsUsed: Int = 0,
    val elapsedMs: Long = 0L,
    val particles: List<CandyParticle> = emptyList(),
    val floatingScores: List<FloatingScore> = emptyList(),
    val borderFlashColor: Color? = null,
    val borderFlashAlpha: Float = 0f,
    val pulseClockSec: Float = 0f,
)
