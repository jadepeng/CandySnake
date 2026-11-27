package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.example.game.ControlMode
import com.example.game.Direction
import com.example.game.GameMode
import com.example.game.GamePhase
import com.example.game.GameUiState
import com.example.game.UserSettings
import com.example.ui.components.CandyDpad
import com.example.ui.theme.CandyVanillaBg
import kotlinx.coroutines.delay

@Composable
fun GameScreen(
    state: GameUiState,
    settings: UserSettings,
    onDirection: (Direction) -> Unit,
    onBoardTap: () -> Unit,
    onTogglePause: () -> Unit,
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onCycleControlMode: () -> Unit,
    onSelectControlMode: (ControlMode) -> Unit,
    onToggleSound: () -> Unit,
    onOpenRecords: () -> Unit,
    onBackHome: () -> Unit,
) {
    // Allow left-handed / right-handed swap of flanking Stats & Control panels in landscape
    var dpadOnLeft by rememberSaveable { mutableStateOf(false) }

    // Physical keyboard support (Arrow keys, WASD, Space/P) for tablets & foldables
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) {
        runCatching { focusRequester.requestFocus() }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) {
        if (state.phase == GamePhase.PLAYING) {
            onTogglePause()
        }
    }

    BackHandler {
        if (state.phase == GamePhase.PLAYING) {
            onTogglePause()
        } else {
            onBackHome()
        }
    }

    var scorePopped by remember { mutableStateOf(false) }
    LaunchedEffect(state.score) {
        if (state.score > 0) {
            scorePopped = true
            delay(130L)
            scorePopped = false
        }
    }
    val scoreScale by animateFloatAsState(
        targetValue = if (scorePopped) 1.16f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = 0.45f),
        label = "score_bounce",
    )

    val bgGradient = if (state.mode == GameMode.CLASSIC) {
        listOf(CandyVanillaBg, Color(0xFFFFF2EA))
    } else {
        listOf(Color(0xFFF9F6FF), Color(0xFFF1ECFF))
    }

    val handleBack = {
        if (state.phase == GamePhase.PLAYING) onTogglePause() else onBackHome()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(bgGradient))
            .focusRequester(focusRequester)
            .focusable()
            .onPreviewKeyEvent { keyEvent ->
                if (keyEvent.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                when (keyEvent.key) {
                    Key.DirectionUp, Key.W -> {
                        onDirection(Direction.UP)
                        true
                    }
                    Key.DirectionDown, Key.S -> {
                        onDirection(Direction.DOWN)
                        true
                    }
                    Key.DirectionLeft, Key.A -> {
                        onDirection(Direction.LEFT)
                        true
                    }
                    Key.DirectionRight, Key.D -> {
                        onDirection(Direction.RIGHT)
                        true
                    }
                    Key.Spacebar, Key.P -> {
                        when (state.phase) {
                            GamePhase.READY -> onBoardTap()
                            GamePhase.PLAYING, GamePhase.PAUSED -> onTogglePause()
                            GamePhase.GAME_OVER -> onRestart()
                        }
                        true
                    }
                    else -> false
                }
            },
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .safeDrawingPadding()
                .padding(horizontal = 14.dp, vertical = 8.dp),
        ) {
            val isLandscape = maxWidth > maxHeight

            if (isLandscape) {
                // Unified Adaptive Landscape Layout:
                // Score & Statistics Panel on one side | Full-Height Game Canvas in Center | Control Zone on the other side
                val isCompactHeight = maxHeight < 490.dp
                val gap = if (maxWidth >= 840.dp) 18.dp else 12.dp
                val boardAspect = state.cols.toFloat() / state.rows.toFloat()
                val idealBoardWidth = maxHeight * boardAspect
                val remainingPerWing = (maxWidth - idealBoardWidth - gap * 2) / 2f
                val minWing = if (maxWidth >= 720.dp) 188.dp else 156.dp
                val maxWing = if (maxWidth >= 1000.dp) 296.dp else 256.dp
                val wingWidth = remainingPerWing.coerceIn(minWing, maxWing)

                val controlHeaderReserve = if (isCompactHeight) 64.dp else 210.dp
                val dpadByHeight = (maxHeight - controlHeaderReserve) / 3.3f
                val dpadByWidth = (wingWidth - 20.dp) / 3.2f
                val dpadSize = minOf(dpadByHeight, dpadByWidth).coerceIn(48.dp, 74.dp)

                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(gap),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    if (dpadOnLeft) {
                        LandscapeControlPanel(
                            settings = settings,
                            dpadOnLeft = dpadOnLeft,
                            dpadSize = dpadSize,
                            isCompactHeight = isCompactHeight,
                            onDirection = onDirection,
                            onSelectControlMode = onSelectControlMode,
                            onCycleControlMode = onCycleControlMode,
                            onSwapHands = { dpadOnLeft = !dpadOnLeft },
                            modifier = Modifier
                                .width(wingWidth)
                                .fillMaxHeight(),
                        )
                    } else {
                        LandscapeStatsPanel(
                            state = state,
                            settings = settings,
                            scoreScale = scoreScale,
                            isCompactHeight = isCompactHeight,
                            onBack = handleBack,
                            onToggleSound = onToggleSound,
                            onTogglePause = onTogglePause,
                            modifier = Modifier
                                .width(wingWidth)
                                .fillMaxHeight(),
                        )
                    }

                    // Center Stage: Full-Height Aspect-Preserving Game Canvas
                    BoardWithReadyHint(
                        state = state,
                        settings = settings,
                        onDirection = onDirection,
                        onBoardTap = onBoardTap,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                    )

                    if (dpadOnLeft) {
                        LandscapeStatsPanel(
                            state = state,
                            settings = settings,
                            scoreScale = scoreScale,
                            isCompactHeight = isCompactHeight,
                            onBack = handleBack,
                            onToggleSound = onToggleSound,
                            onTogglePause = onTogglePause,
                            modifier = Modifier
                                .width(wingWidth)
                                .fillMaxHeight(),
                        )
                    } else {
                        LandscapeControlPanel(
                            settings = settings,
                            dpadOnLeft = dpadOnLeft,
                            dpadSize = dpadSize,
                            isCompactHeight = isCompactHeight,
                            onDirection = onDirection,
                            onSelectControlMode = onSelectControlMode,
                            onCycleControlMode = onCycleControlMode,
                            onSwapHands = { dpadOnLeft = !dpadOnLeft },
                            modifier = Modifier
                                .width(wingWidth)
                                .fillMaxHeight(),
                        )
                    }
                }
            } else {
                // Portrait (Phones & Portrait Tablets)
                val portraitDpadSize = if (maxWidth >= 600.dp) 66.dp else 58.dp
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.TopCenter,
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = 660.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        GameHudBar(
                            state = state,
                            settings = settings,
                            scoreScale = scoreScale,
                            onBackHome = handleBack,
                            onCycleControlMode = onCycleControlMode,
                            onToggleSound = onToggleSound,
                            onTogglePause = onTogglePause,
                        )

                        ActiveEffectsStrip(
                            effects = state.activeEffects,
                            controlMode = settings.controlMode,
                            mode = state.mode,
                        )

                        BoardWithReadyHint(
                            state = state,
                            settings = settings,
                            onDirection = onDirection,
                            onBoardTap = onBoardTap,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                        )

                        AnimatedVisibility(
                            visible = settings.controlMode != ControlMode.SWIPE_ONLY,
                            enter = expandVertically(expandFrom = Alignment.Bottom) + fadeIn(),
                            exit = shrinkVertically(shrinkTowards = Alignment.Bottom) + fadeOut(),
                        ) {
                            CandyDpad(
                                onDirection = onDirection,
                                accentColor = settings.snakeSkin.primaryColor,
                                shadowColor = settings.snakeSkin.outlineColor,
                                buttonSize = portraitDpadSize,
                                modifier = Modifier.padding(top = 4.dp, bottom = 6.dp),
                            )
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = state.phase == GamePhase.PAUSED,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            PauseOverlay(
                settings = settings,
                onSelectControlMode = onSelectControlMode,
                onResume = onResume,
                onRestart = onRestart,
                onBackHome = onBackHome,
            )
        }

        AnimatedVisibility(
            visible = state.phase == GamePhase.GAME_OVER,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            GameOverOverlay(
                state = state,
                onRestart = onRestart,
                onOpenRecords = onOpenRecords,
                onBackHome = onBackHome,
            )
        }
    }
}
