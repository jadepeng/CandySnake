package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AcUnit
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Gamepad
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Swipe
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material.icons.rounded.VolumeOff
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import com.example.R
import com.example.game.ActiveEffect
import com.example.game.ControlMode
import com.example.game.Direction
import com.example.game.GameMode
import com.example.game.GamePhase
import com.example.game.GameUiState
import com.example.game.PowerUpType
import com.example.game.UserSettings
import com.example.ui.components.Candy3DButton
import com.example.ui.components.CandyCard
import com.example.ui.components.CandyDpad
import com.example.ui.components.CandyIconButton
import com.example.ui.components.SelectableCandyChip
import com.example.ui.components.SnakeBoardCanvas
import com.example.ui.theme.CandyBlueberry
import com.example.ui.theme.CandyBlueberryLight
import com.example.ui.theme.CandyBorder
import com.example.ui.theme.CandyCocoaInk
import com.example.ui.theme.CandyCocoaMuted
import com.example.ui.theme.CandyLemon
import com.example.ui.theme.CandyLemonDark
import com.example.ui.theme.CandyLemonLight
import com.example.ui.theme.CandyMint
import com.example.ui.theme.CandyMintDark
import com.example.ui.theme.CandyMintLight
import com.example.ui.theme.CandyPink
import com.example.ui.theme.CandyPinkDark
import com.example.ui.theme.CandyPinkLight
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
    // Auto-pause whenever activity moves to background
    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) {
        if (state.phase == GamePhase.PLAYING) {
            onTogglePause()
        }
    }

    // Intercept system back button during active play so progress isn't lost accidentally
    BackHandler {
        if (state.phase == GamePhase.PLAYING) {
            onTogglePause()
        } else {
            onBackHome()
        }
    }

    // Subtle pop animation whenever score increments
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(bgGradient)),
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 14.dp, vertical = 8.dp),
        ) {
            val isLandscape = maxWidth > maxHeight && maxWidth >= 600.dp

            if (isLandscape) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    // Left: Full-height Candy Board
                    Box(
                        modifier = Modifier
                            .weight(1.15f)
                            .fillMaxHeight(),
                        contentAlignment = Alignment.Center,
                    ) {
                        BoardWithReadyHint(
                            state = state,
                            settings = settings,
                            onDirection = onDirection,
                            onBoardTap = onBoardTap,
                            modifier = Modifier.fillMaxHeight(),
                        )
                    }
                    // Right: HUD + Active Effects + D-Pad
                    Column(
                        modifier = Modifier
                            .weight(0.85f)
                            .fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween,
                    ) {
                        GameHudBar(
                            state = state,
                            settings = settings,
                            scoreScale = scoreScale,
                            onBackHome = {
                                if (state.phase == GamePhase.PLAYING) onTogglePause() else onBackHome()
                            },
                            onCycleControlMode = onCycleControlMode,
                            onToggleSound = onToggleSound,
                            onTogglePause = onTogglePause,
                        )
                        ActiveEffectsStrip(
                            effects = state.activeEffects,
                            controlMode = settings.controlMode,
                            mode = state.mode,
                        )
                        AnimatedVisibility(
                            visible = settings.controlMode != ControlMode.SWIPE_ONLY,
                            enter = fadeIn() + scaleIn(),
                            exit = fadeOut() + scaleOut(),
                        ) {
                            CandyDpad(
                                onDirection = onDirection,
                                accentColor = settings.snakeSkin.primaryColor,
                                shadowColor = settings.snakeSkin.outlineColor,
                                buttonSize = 58.dp,
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            } else {
                // Portrait Mobile Layout
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    GameHudBar(
                        state = state,
                        settings = settings,
                        scoreScale = scoreScale,
                        onBackHome = {
                            if (state.phase == GamePhase.PLAYING) onTogglePause() else onBackHome()
                        },
                        onCycleControlMode = onCycleControlMode,
                        onToggleSound = onToggleSound,
                        onTogglePause = onTogglePause,
                    )

                    ActiveEffectsStrip(
                        effects = state.activeEffects,
                        controlMode = settings.controlMode,
                        mode = state.mode,
                    )

                    // Center Board takes all available flexible height
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        BoardWithReadyHint(
                            state = state,
                            settings = settings,
                            onDirection = onDirection,
                            onBoardTap = onBoardTap,
                            modifier = Modifier.fillMaxSize(),
                        )
                    }

                    // Bottom D-Pad (auto-collapses cleanly in Swipe-Only mode!)
                    AnimatedVisibility(
                        visible = settings.controlMode != ControlMode.SWIPE_ONLY,
                        enter = expandVertically(expandFrom = Alignment.Bottom) + fadeIn(),
                        exit = shrinkVertically(shrinkTowards = Alignment.Bottom) + fadeOut(),
                    ) {
                        CandyDpad(
                            onDirection = onDirection,
                            accentColor = settings.snakeSkin.primaryColor,
                            shadowColor = settings.snakeSkin.outlineColor,
                            buttonSize = 58.dp,
                            modifier = Modifier.padding(top = 4.dp, bottom = 6.dp),
                        )
                    }
                }
            }
        }

        // Pause Overlay
        AnimatedVisibility(
            visible = state.phase == GamePhase.PAUSED,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            PauseOverlay(
                settings = settings,
                onSelectControlMode = onSelectControlMode,
                onToggleSound = onToggleSound,
                onResume = onResume,
                onRestart = onRestart,
                onBackHome = onBackHome,
            )
        }

        // Game Over Celebration Overlay
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

@Composable
private fun BoardWithReadyHint(
    state: GameUiState,
    settings: UserSettings,
    onDirection: (Direction) -> Unit,
    onBoardTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        SnakeBoardCanvas(
            state = state,
            skin = settings.snakeSkin,
            controlMode = settings.controlMode,
            onDirection = onDirection,
            onBoardTap = onBoardTap,
        )

        androidx.compose.animation.AnimatedVisibility(
            visible = state.phase == GamePhase.READY,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 22.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = Color.White.copy(alpha = 0.94f),
                border = BorderStroke(2.dp, CandyPink.copy(alpha = 0.6f)),
                shadowElevation = 6.dp,
                onClick = onBoardTap,
                modifier = Modifier.testTag("ready_start_pill"),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = null,
                        tint = CandyPink,
                        modifier = Modifier.size(20.dp),
                    )
                    Text(
                        text = stringResource(R.string.label_tap_to_start),
                        style = MaterialTheme.typography.labelLarge,
                        color = CandyCocoaInk,
                    )
                }
            }
        }
    }
}

@Composable
private fun GameHudBar(
    state: GameUiState,
    settings: UserSettings,
    scoreScale: Float,
    onBackHome: () -> Unit,
    onCycleControlMode: () -> Unit,
    onToggleSound: () -> Unit,
    onTogglePause: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        CandyIconButton(
            icon = Icons.AutoMirrored.Rounded.ArrowBack,
            contentDescription = stringResource(R.string.cd_back),
            onClick = onBackHome,
            size = 44.dp,
            testTag = "game_back_button",
        )

        // Score & Best Score Candy Pill
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            border = BorderStroke(1.5.dp, CandyBorder),
            shadowElevation = 3.dp,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.label_score),
                        style = MaterialTheme.typography.labelSmall,
                        color = CandyCocoaMuted,
                    )
                    Text(
                        text = state.score.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        color = CandyPinkDark,
                        modifier = Modifier
                            .scale(scoreScale)
                            .testTag("game_score_text"),
                    )
                }
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(28.dp)
                        .background(CandyBorder),
                )
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.EmojiEvents,
                            contentDescription = null,
                            tint = CandyLemon,
                            modifier = Modifier.size(13.dp),
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = stringResource(R.string.label_best_score),
                            style = MaterialTheme.typography.labelSmall,
                            color = CandyCocoaMuted,
                        )
                    }
                    Text(
                        text = state.bestScore.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        color = CandyLemonDark,
                        modifier = Modifier.testTag("game_best_score_text"),
                    )
                }
            }
        }

        // Quick Control Mode Toggle Icon
        val controlIcon = when (settings.controlMode) {
            ControlMode.BOTH -> Icons.Rounded.TouchApp
            ControlMode.SWIPE_ONLY -> Icons.Rounded.Swipe
            ControlMode.DPAD_ONLY -> Icons.Rounded.Gamepad
        }
        CandyIconButton(
            icon = controlIcon,
            contentDescription = stringResource(R.string.cd_toggle_control),
            onClick = onCycleControlMode,
            containerColor = CandyPinkLight,
            borderColor = CandyPink.copy(alpha = 0.45f),
            iconTint = CandyPinkDark,
            size = 44.dp,
            testTag = "game_control_toggle_button",
        )

        CandyIconButton(
            icon = if (settings.soundEnabled) Icons.Rounded.VolumeUp else Icons.Rounded.VolumeOff,
            contentDescription = stringResource(R.string.cd_toggle_sound),
            onClick = onToggleSound,
            size = 44.dp,
            testTag = "game_sound_toggle_button",
        )

        CandyIconButton(
            icon = if (state.phase == GamePhase.PAUSED) Icons.Rounded.PlayArrow else Icons.Rounded.Pause,
            contentDescription = stringResource(R.string.action_pause),
            onClick = onTogglePause,
            containerColor = CandyMintLight,
            borderColor = CandyMint.copy(alpha = 0.55f),
            iconTint = CandyMintDark,
            size = 44.dp,
            testTag = "game_pause_button",
        )
    }
}

@Composable
private fun ActiveEffectsStrip(
    effects: List<ActiveEffect>,
    controlMode: ControlMode,
    mode: GameMode,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(34.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (effects.isNotEmpty()) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                effects.forEach { effect ->
                    ActiveEffectPill(effect = effect)
                }
            }
        } else {
            // Clean subtle mode & control mode pill
            Surface(
                shape = CircleShape,
                color = if (mode == GameMode.CLASSIC) CandyMintLight else CandyBlueberryLight,
                border = BorderStroke(
                    1.dp,
                    if (mode == GameMode.CLASSIC) CandyMint.copy(alpha = 0.45f) else CandyBlueberry.copy(alpha = 0.45f),
                ),
            ) {
                Text(
                    text = "${stringResource(mode.labelRes)}  ·  ${stringResource(controlMode.labelRes)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = CandyCocoaInk,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                )
            }
        }
    }
}

@Composable
private fun ActiveEffectPill(effect: ActiveEffect) {
    val icon = when (effect.type) {
        PowerUpType.SPEED_BOOST -> Icons.Rounded.Bolt
        PowerUpType.DOUBLE_SCORE -> Icons.Rounded.Star
        PowerUpType.SLOW_MO -> Icons.Rounded.AcUnit
        PowerUpType.RAINBOW_DONUT -> Icons.Rounded.Star
    }
    val secsLeft = String.format("%.1fs", effect.remainingMs / 1000f)

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = effect.type.accentColor.copy(alpha = 0.16f),
        border = BorderStroke(1.5.dp, effect.type.accentColor),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = effect.type.accentColor,
                modifier = Modifier.size(16.dp),
            )
            Text(
                text = "${stringResource(effect.type.badgeRes)} $secsLeft",
                style = MaterialTheme.typography.labelMedium,
                color = CandyCocoaInk,
                fontWeight = FontWeight.Bold,
            )
            LinearProgressIndicator(
                progress = { effect.fraction },
                modifier = Modifier
                    .width(34.dp)
                    .height(6.dp)
                    .clip(CircleShape),
                color = effect.type.accentColor,
                trackColor = Color.White,
            )
        }
    }
}

@Composable
private fun PauseOverlay(
    settings: UserSettings,
    onSelectControlMode: (ControlMode) -> Unit,
    onToggleSound: () -> Unit,
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onBackHome: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x883A2338))
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        CandyCard(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 380.dp),
            containerColor = CandyVanillaBg,
            borderColor = CandyPink.copy(alpha = 0.45f),
            cornerRadius = 30.dp,
            contentPadding = PaddingValues(22.dp),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    text = stringResource(R.string.label_game_paused),
                    style = MaterialTheme.typography.headlineMedium,
                    color = CandyCocoaInk,
                )

                // Quick Control Switch inside Pause Overlay
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    ControlMode.entries.forEach { mode ->
                        SelectableCandyChip(
                            label = stringResource(mode.labelRes),
                            selected = settings.controlMode == mode,
                            activeColor = CandyPink,
                            onClick = { onSelectControlMode(mode) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }

                Candy3DButton(
                    text = stringResource(R.string.action_resume),
                    icon = Icons.Rounded.PlayArrow,
                    containerColor = CandyMint,
                    shadowColor = CandyMintDark,
                    onClick = onResume,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "pause_resume_button",
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Candy3DButton(
                        text = stringResource(R.string.action_restart),
                        icon = Icons.Rounded.Refresh,
                        containerColor = CandyLemon,
                        shadowColor = CandyLemonDark,
                        contentColor = CandyCocoaInk,
                        onClick = onRestart,
                        modifier = Modifier.weight(1f),
                        height = 50.dp,
                        testTag = "pause_restart_button",
                    )
                    Candy3DButton(
                        text = stringResource(R.string.action_back_home),
                        icon = Icons.Rounded.Home,
                        containerColor = CandyPink,
                        shadowColor = CandyPinkDark,
                        onClick = onBackHome,
                        modifier = Modifier.weight(1f),
                        height = 50.dp,
                        testTag = "pause_home_button",
                    )
                }
            }
        }
    }
}

@Composable
private fun GameOverOverlay(
    state: GameUiState,
    onRestart: () -> Unit,
    onOpenRecords: () -> Unit,
    onBackHome: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x943A2338))
            .padding(24.dp),
        contentAlignment = Alignment.Center,
    ) {
        CandyCard(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 390.dp),
            containerColor = CandyVanillaBg,
            borderColor = if (state.isNewRecord) CandyLemon else CandyPink.copy(alpha = 0.5f),
            cornerRadius = 30.dp,
            contentPadding = PaddingValues(22.dp),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                if (state.isNewRecord) {
                    Surface(
                        shape = CircleShape,
                        color = CandyLemon,
                        shadowElevation = 4.dp,
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.EmojiEvents,
                                contentDescription = null,
                                tint = CandyCocoaInk,
                                modifier = Modifier.size(18.dp),
                            )
                            Text(
                                text = stringResource(R.string.label_new_record),
                                style = MaterialTheme.typography.labelLarge,
                                color = CandyCocoaInk,
                            )
                        }
                    }
                }

                Text(
                    text = stringResource(R.string.label_game_over),
                    style = MaterialTheme.typography.headlineMedium,
                    color = CandyCocoaInk,
                    textAlign = TextAlign.Center,
                )

                // Big Score Display
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    border = BorderStroke(2.dp, CandyPinkLight),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = stringResource(R.string.label_score),
                            style = MaterialTheme.typography.labelMedium,
                            color = CandyCocoaMuted,
                        )
                        Text(
                            text = state.score.toString(),
                            style = MaterialTheme.typography.displayLarge,
                            color = CandyPink,
                        )
                    }
                }

                // 4 Mini Stats
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    SummaryStatPill(
                        label = stringResource(R.string.label_best_score),
                        value = state.bestScore.toString(),
                        bg = CandyLemonLight,
                        modifier = Modifier.weight(1f),
                    )
                    SummaryStatPill(
                        label = stringResource(R.string.label_length),
                        value = state.snake.size.toString(),
                        bg = CandyMintLight,
                        modifier = Modifier.weight(1f),
                    )
                    SummaryStatPill(
                        label = stringResource(R.string.label_candies),
                        value = state.candiesEaten.toString(),
                        bg = CandyPinkLight,
                        modifier = Modifier.weight(1f),
                    )
                    SummaryStatPill(
                        label = stringResource(R.string.label_powerups),
                        value = state.powerUpsUsed.toString(),
                        bg = CandyBlueberryLight,
                        modifier = Modifier.weight(1f),
                    )
                }

                Candy3DButton(
                    text = stringResource(R.string.action_restart),
                    icon = Icons.Rounded.Refresh,
                    containerColor = CandyPink,
                    shadowColor = CandyPinkDark,
                    onClick = onRestart,
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "gameover_restart_button",
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    Candy3DButton(
                        text = stringResource(R.string.action_records),
                        icon = Icons.Rounded.EmojiEvents,
                        containerColor = CandyLemon,
                        shadowColor = CandyLemonDark,
                        contentColor = CandyCocoaInk,
                        onClick = onOpenRecords,
                        modifier = Modifier.weight(1f),
                        height = 48.dp,
                        testTag = "gameover_records_button",
                    )
                    Candy3DButton(
                        text = stringResource(R.string.action_back_home),
                        icon = Icons.Rounded.Home,
                        containerColor = CandyMint,
                        shadowColor = CandyMintDark,
                        onClick = onBackHome,
                        modifier = Modifier.weight(1f),
                        height = 48.dp,
                        testTag = "gameover_home_button",
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryStatPill(
    label: String,
    value: String,
    bg: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = bg,
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = CandyCocoaInk,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = CandyCocoaMuted,
            )
        }
    }
}
