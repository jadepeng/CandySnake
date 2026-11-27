package com.example.viewmodel

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.CandySnakeDatabase
import com.example.data.CareerStats
import com.example.data.GameRecordEntity
import com.example.data.ScoreRepository
import com.example.data.SettingsRepository
import com.example.game.CandyParticle
import com.example.game.ControlMode
import com.example.game.Direction
import com.example.game.FloatingScore
import com.example.game.GameMode
import com.example.game.GamePhase
import com.example.game.GameUiState
import com.example.game.GridPoint
import com.example.game.PowerUpType
import com.example.game.SnakeGameEngine
import com.example.game.SnakeSkin
import com.example.game.SoundAndHapticsEngine
import com.example.game.SpeedPreset
import com.example.game.UserSettings
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

class CandySnakeViewModel(
    private val scoreRepository: ScoreRepository,
    private val settingsRepository: SettingsRepository,
    private val feedbackEngine: SoundAndHapticsEngine,
) : ViewModel() {

    private val engine = SnakeGameEngine(cols = 16, rows = 18)
    private var gameLoopJob: Job? = null
    private var idlePulseJob: Job? = null
    private var nextEffectId: Long = 1L
    private var tickAccumulatorMs: Long = 0L
    private var initialBestForRun: Int = 0
    private var announcedNewRecord: Boolean = false

    val settings: StateFlow<UserSettings> = settingsRepository.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UserSettings(),
    )

    val classicBestScore: StateFlow<Int> = scoreRepository.bestScoreFlow(GameMode.CLASSIC).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = 0,
    )

    val endlessBestScore: StateFlow<Int> = scoreRepository.bestScoreFlow(GameMode.ENDLESS).stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = 0,
    )

    val classicRecords: StateFlow<List<GameRecordEntity>> =
        scoreRepository.topRecordsFlow(GameMode.CLASSIC).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    val endlessRecords: StateFlow<List<GameRecordEntity>> =
        scoreRepository.topRecordsFlow(GameMode.ENDLESS).stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    val careerStats: StateFlow<CareerStats> = scoreRepository.careerStats.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CareerStats(),
    )

    private val _gameState = MutableStateFlow(GameUiState())
    val gameState: StateFlow<GameUiState> = _gameState.asStateFlow()

    init {
        prepareNewGame(GameMode.CLASSIC)
        startIdlePulseLoop()
    }

    private fun startIdlePulseLoop() {
        idlePulseJob?.cancel()
        idlePulseJob = viewModelScope.launch {
            while (isActive) {
                delay(32L)
                if (_gameState.value.phase != GamePhase.PLAYING) {
                    _gameState.update { it.copy(pulseClockSec = it.pulseClockSec + 0.032f) }
                }
            }
        }
    }

    fun prepareNewGame(mode: GameMode = settings.value.selectedMode) {
        gameLoopJob?.cancel()
        val currentSettings = settings.value
        engine.reset(mode = mode, speedPreset = currentSettings.speedPreset)
        tickAccumulatorMs = 0L
        initialBestForRun = when (mode) {
            GameMode.CLASSIC -> classicBestScore.value
            GameMode.ENDLESS -> endlessBestScore.value
        }
        announcedNewRecord = false

        _gameState.value = GameUiState(
            cols = engine.cols,
            rows = engine.rows,
            phase = GamePhase.READY,
            mode = mode,
            snake = engine.snake,
            previousSnake = engine.previousSnake,
            moveProgress = 1f,
            direction = engine.direction,
            food = engine.food,
            foodStyle = engine.foodStyle,
            boardPowerUp = engine.boardPowerUp,
            activeEffects = emptyList(),
            score = 0,
            bestScore = initialBestForRun,
            isNewRecord = false,
            candiesEaten = 0,
            powerUpsUsed = 0,
            elapsedMs = 0L,
            particles = emptyList(),
            floatingScores = emptyList(),
            borderFlashColor = null,
            borderFlashAlpha = 0f,
            pulseClockSec = _gameState.value.pulseClockSec,
        )
    }

    fun startFromReady(initialDirection: Direction? = null) {
        if (_gameState.value.phase != GamePhase.READY) return
        if (initialDirection != null) {
            engine.enqueueDirection(initialDirection)
        }
        _gameState.update { it.copy(phase = GamePhase.PLAYING) }
        startGameLoop()
    }

    fun onDirectionInput(direction: Direction) {
        when (_gameState.value.phase) {
            GamePhase.READY -> {
                feedbackEngine.onTurn(settings.value.hapticsEnabled)
                startFromReady(direction)
            }
            GamePhase.PLAYING -> {
                if (engine.enqueueDirection(direction)) {
                    feedbackEngine.onTurn(settings.value.hapticsEnabled)
                }
            }
            GamePhase.PAUSED, GamePhase.GAME_OVER -> Unit
        }
    }

    fun pauseGame() {
        if (_gameState.value.phase == GamePhase.PLAYING) {
            gameLoopJob?.cancel()
            _gameState.update { it.copy(phase = GamePhase.PAUSED) }
        }
    }

    fun resumeGame() {
        if (_gameState.value.phase == GamePhase.PAUSED) {
            _gameState.update { it.copy(phase = GamePhase.PLAYING) }
            startGameLoop()
        }
    }

    fun togglePause() {
        when (_gameState.value.phase) {
            GamePhase.PLAYING -> pauseGame()
            GamePhase.PAUSED -> resumeGame()
            else -> Unit
        }
    }

    fun restartGame() {
        prepareNewGame(_gameState.value.mode)
    }

    private fun startGameLoop() {
        gameLoopJob?.cancel()
        gameLoopJob = viewModelScope.launch {
            var lastNanos = System.nanoTime()
            while (isActive && _gameState.value.phase == GamePhase.PLAYING) {
                delay(16L)
                val nowNanos = System.nanoTime()
                val deltaMs = ((nowNanos - lastNanos) / 1_000_000L).coerceIn(1L, 64L)
                lastNanos = nowNanos
                val dtSec = deltaMs / 1000f

                engine.advanceTimers(deltaMs)
                tickAccumulatorMs += deltaMs

                var particles = advanceParticles(_gameState.value.particles, dtSec)
                var floatingScores = advanceFloatingScores(_gameState.value.floatingScores, dtSec)
                var flashColor = _gameState.value.borderFlashColor
                var flashAlpha = (_gameState.value.borderFlashAlpha - dtSec * 1.8f).coerceAtLeast(0f)
                if (flashAlpha <= 0.01f) flashColor = null

                val tickInterval = engine.currentTickIntervalMs()
                var crashed = false

                if (tickAccumulatorMs >= tickInterval) {
                    tickAccumulatorMs -= tickInterval
                    // Prevent spiral of death if thread hiccups
                    if (tickAccumulatorMs > tickInterval) tickAccumulatorMs = 0L

                    val outcome = engine.tick()
                    if (outcome.crashed) {
                        crashed = true
                    } else {
                        val cfg = settings.value
                        if (outcome.ateFood && outcome.foodPosition != null) {
                            feedbackEngine.onEatCandy(
                                soundEnabled = cfg.soundEnabled,
                                hapticsEnabled = cfg.hapticsEnabled,
                                isDouble = engine.hasDoubleScore(),
                            )
                            val foodColor = _gameState.value.foodStyle.primaryColor
                            particles = particles + spawnCandyBurst(
                                pos = outcome.foodPosition,
                                palette = listOf(foodColor, Color(0xFFFFD166), Color.White),
                                count = 12,
                            )
                            floatingScores = floatingScores + FloatingScore(
                                id = nextEffectId++,
                                gridX = outcome.foodPosition.x + 0.5f,
                                gridY = outcome.foodPosition.y + 0.3f,
                                text = "+${outcome.foodPoints}",
                                color = if (engine.hasDoubleScore()) Color(0xFFFF4D8D) else foodColor,
                                progress = 0f,
                            )
                        }

                        if (outcome.atePowerUp != null && outcome.powerUpPosition != null) {
                            feedbackEngine.onPowerUp(
                                soundEnabled = cfg.soundEnabled,
                                hapticsEnabled = cfg.hapticsEnabled,
                            )
                            val pType = outcome.atePowerUp
                            val burstPalette = if (pType == PowerUpType.RAINBOW_DONUT) {
                                listOf(
                                    Color(0xFFFF5C8A),
                                    Color(0xFFFFB830),
                                    Color(0xFF2EC492),
                                    Color(0xFF38BDF8),
                                    Color(0xFFA855F7),
                                )
                            } else {
                                listOf(pType.accentColor, Color(0xFFFFF4D6), Color.White)
                            }
                            particles = particles + spawnCandyBurst(
                                pos = outcome.powerUpPosition,
                                palette = burstPalette,
                                count = if (pType == PowerUpType.RAINBOW_DONUT) 22 else 16,
                            )
                            val popupLabel = when (pType) {
                                PowerUpType.DOUBLE_SCORE -> "2x! +${outcome.powerUpPoints}"
                                PowerUpType.SPEED_BOOST -> "极速! +${outcome.powerUpPoints}"
                                PowerUpType.SLOW_MO -> "减速! +${outcome.powerUpPoints}"
                                PowerUpType.RAINBOW_DONUT -> "彩虹! +${outcome.powerUpPoints}"
                            }
                            floatingScores = floatingScores + FloatingScore(
                                id = nextEffectId++,
                                gridX = outcome.powerUpPosition.x + 0.5f,
                                gridY = outcome.powerUpPosition.y + 0.2f,
                                text = popupLabel,
                                color = pType.accentColor,
                                progress = 0f,
                            )
                            flashColor = pType.accentColor
                            flashAlpha = 0.85f
                        }

                        if (!announcedNewRecord && engine.score > initialBestForRun && engine.score > 0) {
                            announcedNewRecord = true
                            if (initialBestForRun > 0) {
                                feedbackEngine.onNewRecord(
                                    soundEnabled = cfg.soundEnabled,
                                    hapticsEnabled = cfg.hapticsEnabled,
                                )
                                flashColor = Color(0xFFFFB830)
                                flashAlpha = 0.9f
                            }
                        }
                    }
                }

                if (crashed) {
                    handleGameOver(particles, floatingScores)
                    break
                } else {
                    val progress = (tickAccumulatorMs.toFloat() / engine.currentTickIntervalMs().toFloat())
                        .coerceIn(0f, 1f)
                    val displayBest = maxOf(initialBestForRun, engine.score)
                    _gameState.update { state ->
                        state.copy(
                            snake = engine.snake,
                            previousSnake = engine.previousSnake,
                            moveProgress = progress,
                            direction = engine.direction,
                            food = engine.food,
                            foodStyle = engine.foodStyle,
                            boardPowerUp = engine.boardPowerUp,
                            activeEffects = engine.activeEffects.toList(),
                            score = engine.score,
                            bestScore = displayBest,
                            isNewRecord = engine.score > initialBestForRun && engine.score > 0,
                            candiesEaten = engine.candiesEaten,
                            powerUpsUsed = engine.powerUpsUsed,
                            elapsedMs = state.elapsedMs + deltaMs,
                            particles = particles,
                            floatingScores = floatingScores,
                            borderFlashColor = flashColor,
                            borderFlashAlpha = flashAlpha,
                            pulseClockSec = state.pulseClockSec + dtSec,
                        )
                    }
                }
            }
        }
    }

    private fun handleGameOver(
        existingParticles: List<CandyParticle>,
        existingFloats: List<FloatingScore>,
    ) {
        val cfg = settings.value
        feedbackEngine.onGameOver(
            soundEnabled = cfg.soundEnabled,
            hapticsEnabled = cfg.hapticsEnabled,
        )
        val head = engine.snake.firstOrNull()
        val crashParticles = if (head != null) {
            existingParticles + spawnCandyBurst(
                pos = head,
                palette = listOf(Color(0xFFFF5C8A), Color(0xFFFFB830), Color.White),
                count = 18,
            )
        } else {
            existingParticles
        }

        val finalScore = engine.score
        val finalLength = engine.snake.size
        val finalCandies = engine.candiesEaten
        val finalPowerUps = engine.powerUpsUsed
        val finalDurationSec = (_gameState.value.elapsedMs / 1000L).toInt().coerceAtLeast(1)
        val isNewBest = finalScore > initialBestForRun && finalScore > 0

        _gameState.update { state ->
            state.copy(
                phase = GamePhase.GAME_OVER,
                moveProgress = 1f,
                score = finalScore,
                bestScore = maxOf(initialBestForRun, finalScore),
                isNewRecord = isNewBest,
                particles = crashParticles,
                floatingScores = existingFloats,
                borderFlashColor = Color(0xFFFF4D6D),
                borderFlashAlpha = 0.9f,
            )
        }

        viewModelScope.launch {
            scoreRepository.saveRecord(
                mode = engine.mode,
                score = finalScore,
                snakeLength = finalLength,
                candiesEaten = finalCandies,
                powerUpsUsed = finalPowerUps,
                durationSeconds = finalDurationSec,
            )
        }
    }

    private fun spawnCandyBurst(
        pos: GridPoint,
        palette: List<Color>,
        count: Int,
    ): List<CandyParticle> {
        return List(count) { idx ->
            val angle = (2.0 * PI * idx / count) + Random.nextDouble(-0.25, 0.25)
            val speed = Random.nextFloat() * 3.8f + 1.8f
            CandyParticle(
                id = nextEffectId++,
                x = pos.x + 0.5f,
                y = pos.y + 0.5f,
                vx = (cos(angle) * speed).toFloat(),
                vy = (sin(angle) * speed).toFloat() - 0.8f,
                color = palette[idx % palette.size],
                radius = Random.nextFloat() * 0.16f + 0.10f,
                life = 1f,
            )
        }
    }

    private fun advanceParticles(list: List<CandyParticle>, dtSec: Float): List<CandyParticle> {
        if (list.isEmpty()) return emptyList()
        return list.mapNotNull { p ->
            val nextLife = p.life - dtSec * 1.9f
            if (nextLife <= 0f) {
                null
            } else {
                p.copy(
                    x = p.x + p.vx * dtSec,
                    y = p.y + p.vy * dtSec,
                    vy = p.vy + 5.5f * dtSec,
                    life = nextLife,
                )
            }
        }
    }

    private fun advanceFloatingScores(list: List<FloatingScore>, dtSec: Float): List<FloatingScore> {
        if (list.isEmpty()) return emptyList()
        return list.mapNotNull { item ->
            val next = item.progress + dtSec * 1.35f
            if (next >= 1f) null else item.copy(progress = next, gridY = item.gridY - dtSec * 1.4f)
        }
    }

    // Settings actions
    fun selectMode(mode: GameMode) {
        viewModelScope.launch { settingsRepository.updateMode(mode) }
    }

    fun selectSkin(skin: SnakeSkin) {
        viewModelScope.launch { settingsRepository.updateSkin(skin) }
    }

    fun setControlMode(controlMode: ControlMode) {
        viewModelScope.launch { settingsRepository.updateControlMode(controlMode) }
    }

    fun cycleControlMode() {
        setControlMode(settings.value.controlMode.next())
    }

    fun setSpeedPreset(preset: SpeedPreset) {
        viewModelScope.launch { settingsRepository.updateSpeedPreset(preset) }
    }

    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.updateSoundEnabled(enabled) }
    }

    fun toggleSound() {
        setSoundEnabled(!settings.value.soundEnabled)
    }

    fun setHapticsEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.updateHapticsEnabled(enabled) }
    }

    fun clearAllRecords() {
        viewModelScope.launch { scoreRepository.clearAllRecords() }
    }

    override fun onCleared() {
        super.onCleared()
        gameLoopJob?.cancel()
        idlePulseJob?.cancel()
    }

    class Factory(private val context: Context) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val db = CandySnakeDatabase.getInstance(context)
            return CandySnakeViewModel(
                scoreRepository = ScoreRepository(db.gameRecordDao()),
                settingsRepository = SettingsRepository(context.applicationContext),
                feedbackEngine = SoundAndHapticsEngine(context.applicationContext),
            ) as T
        }
    }
}
