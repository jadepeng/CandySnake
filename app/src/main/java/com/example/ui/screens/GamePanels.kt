package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AcUnit
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Gamepad
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Keyboard
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.SwapHoriz
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
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

@Composable
internal fun LandscapeStatsPanel(
    state: GameUiState,
    settings: UserSettings,
    scoreScale: Float,
    isCompactHeight: Boolean,
    onBack: () -> Unit,
    onToggleSound: () -> Unit,
    onTogglePause: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = if (isCompactHeight) 8.dp else 12.dp
    val iconBtnSize = if (isCompactHeight) 44.dp else 48.dp

    Column(
        modifier = modifier
            .testTag("landscape_stats_panel")
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(spacing),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Action Bar (Back, Sound, Pause)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            CandyIconButton(
                icon = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = stringResource(R.string.cd_back),
                onClick = onBack,
                size = iconBtnSize,
                testTag = "game_back_button",
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CandyIconButton(
                    icon = if (settings.soundEnabled) Icons.Rounded.VolumeUp else Icons.Rounded.VolumeOff,
                    contentDescription = stringResource(R.string.cd_toggle_sound),
                    onClick = onToggleSound,
                    size = iconBtnSize,
                    testTag = "game_sound_toggle_button",
                )
                CandyIconButton(
                    icon = if (state.phase == GamePhase.PAUSED) Icons.Rounded.PlayArrow else Icons.Rounded.Pause,
                    contentDescription = stringResource(R.string.action_pause),
                    onClick = onTogglePause,
                    containerColor = CandyMintLight,
                    borderColor = CandyMint.copy(alpha = 0.55f),
                    iconTint = CandyMintDark,
                    size = iconBtnSize,
                    testTag = "game_pause_button",
                )
            }
        }

        // Mode Badge
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = if (state.mode == GameMode.CLASSIC) CandyMintLight else CandyBlueberryLight,
            border = BorderStroke(
                1.5.dp,
                if (state.mode == GameMode.CLASSIC) CandyMint.copy(alpha = 0.5f) else CandyBlueberry.copy(alpha = 0.5f),
            ),
        ) {
            Text(
                text = stringResource(state.mode.labelRes),
                style = MaterialTheme.typography.titleMedium,
                color = CandyCocoaInk,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(vertical = if (isCompactHeight) 5.dp else 8.dp),
            )
        }

        // Score & Personal Best Dashboard Card
        CandyCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(if (isCompactHeight) 10.dp else 16.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(if (isCompactHeight) 6.dp else 10.dp),
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = stringResource(R.string.label_score),
                        style = MaterialTheme.typography.labelMedium,
                        color = CandyCocoaMuted,
                    )
                    Text(
                        text = state.score.toString(),
                        style = if (isCompactHeight) MaterialTheme.typography.headlineLarge else MaterialTheme.typography.displayMedium,
                        color = CandyPinkDark,
                        modifier = Modifier
                            .scale(scoreScale)
                            .testTag("game_score_text"),
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(CandyBorder),
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.EmojiEvents,
                        contentDescription = null,
                        tint = CandyLemon,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = stringResource(R.string.label_best_score),
                        style = MaterialTheme.typography.labelMedium,
                        color = CandyCocoaMuted,
                    )
                    Text(
                        text = state.bestScore.toString(),
                        style = MaterialTheme.typography.titleLarge,
                        color = CandyLemonDark,
                        modifier = Modifier.testTag("game_best_score_text"),
                    )
                }
            }
        }

        // Live Telemetry Row (Length, Candies, Power-Ups)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
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

        // Active Power-Up Countdown Stack
        if (state.activeEffects.isNotEmpty()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                state.activeEffects.forEach { effect ->
                    ActiveEffectPill(
                        effect = effect,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }

        // Tablet Keyboard & Touch Hint Pill (shown when vertical room allows)
        if (!isCompactHeight) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.85f),
                border = BorderStroke(1.dp, CandyBorder),
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Keyboard,
                        contentDescription = null,
                        tint = CandyCocoaMuted,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = stringResource(R.string.label_tablet_keyboard_hint),
                        style = MaterialTheme.typography.labelSmall,
                        color = CandyCocoaMuted,
                    )
                }
            }
        }
    }
}

@Composable
internal fun LandscapeControlPanel(
    settings: UserSettings,
    dpadOnLeft: Boolean,
    dpadSize: Dp,
    isCompactHeight: Boolean,
    onDirection: (Direction) -> Unit,
    onSelectControlMode: (ControlMode) -> Unit,
    onCycleControlMode: () -> Unit,
    onSwapHands: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.testTag("landscape_control_panel"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        if (isCompactHeight) {
            // Sleek single-row control bar to preserve vertical room for D-pad
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val controlIcon = when (settings.controlMode) {
                    ControlMode.BOTH -> Icons.Rounded.TouchApp
                    ControlMode.SWIPE_ONLY -> Icons.Rounded.Swipe
                    ControlMode.DPAD_ONLY -> Icons.Rounded.Gamepad
                }
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("game_control_toggle_button"),
                    shape = RoundedCornerShape(16.dp),
                    color = CandyPinkLight,
                    border = BorderStroke(1.5.dp, CandyPink.copy(alpha = 0.45f)),
                    onClick = onCycleControlMode,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            imageVector = controlIcon,
                            contentDescription = stringResource(R.string.cd_toggle_control),
                            tint = CandyPinkDark,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(settings.controlMode.labelRes),
                            style = MaterialTheme.typography.labelMedium,
                            color = CandyPinkDark,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
                CandyIconButton(
                    icon = Icons.Rounded.SwapHoriz,
                    contentDescription = stringResource(R.string.cd_swap_hands),
                    onClick = onSwapHands,
                    size = 44.dp,
                    testTag = "tablet_swap_hands_button",
                )
            }
        } else {
            // Full Tablet Control Mode Card with Hand-Swap Toggle
            CandyCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(12.dp),
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = stringResource(R.string.label_control_mode),
                            style = MaterialTheme.typography.labelMedium,
                            color = CandyCocoaInk,
                        )
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = CandyPinkLight,
                            onClick = onSwapHands,
                            modifier = Modifier.testTag("tablet_swap_hands_button"),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.SwapHoriz,
                                    contentDescription = stringResource(R.string.cd_swap_hands),
                                    tint = CandyPinkDark,
                                    modifier = Modifier.size(16.dp),
                                )
                                Text(
                                    text = stringResource(
                                        if (dpadOnLeft) R.string.label_swap_hand_left else R.string.label_swap_hand_right
                                    ),
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CandyPinkDark,
                                    fontWeight = FontWeight.Bold,
                                )
                            }
                        }
                    }
                    ControlMode.entries.forEach { mode ->
                        SelectableCandyChip(
                            label = stringResource(mode.labelRes),
                            selected = settings.controlMode == mode,
                            activeColor = CandyPink,
                            onClick = { onSelectControlMode(mode) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("tablet_control_${mode.storageKey}"),
                        )
                    }
                }
            }
        }

        // Ergonomic Tablet D-Pad (or Swipe Mode Guide Card)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            if (settings.controlMode != ControlMode.SWIPE_ONLY) {
                CandyDpad(
                    onDirection = onDirection,
                    accentColor = settings.snakeSkin.primaryColor,
                    shadowColor = settings.snakeSkin.outlineColor,
                    buttonSize = dpadSize,
                )
            } else {
                CandyCard(
                    modifier = Modifier.fillMaxWidth(),
                    containerColor = CandyPinkLight.copy(alpha = 0.55f),
                    borderColor = CandyPink.copy(alpha = 0.35f),
                    contentPadding = PaddingValues(14.dp),
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Swipe,
                            contentDescription = null,
                            tint = CandyPinkDark,
                            modifier = Modifier.size(32.dp),
                        )
                        Text(
                            text = stringResource(R.string.control_hint_swipe),
                            style = MaterialTheme.typography.bodyMedium,
                            color = CandyCocoaInk,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }
        }
    }
}

@Composable
internal fun BoardWithReadyHint(
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
            modifier = Modifier.fillMaxSize(),
        )

        AnimatedVisibility(
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
internal fun GameHudBar(
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
internal fun ActiveEffectsStrip(
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
internal fun ActiveEffectPill(
    effect: ActiveEffect,
    modifier: Modifier = Modifier,
) {
    val icon = when (effect.type) {
        PowerUpType.SPEED_BOOST -> Icons.Rounded.Bolt
        PowerUpType.DOUBLE_SCORE -> Icons.Rounded.Star
        PowerUpType.SLOW_MO -> Icons.Rounded.AcUnit
        PowerUpType.RAINBOW_DONUT -> Icons.Rounded.Star
    }
    val secsLeft = String.format("%.1fs", effect.remainingMs / 1000f)

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = effect.type.accentColor.copy(alpha = 0.16f),
        border = BorderStroke(1.5.dp, effect.type.accentColor),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
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
internal fun PauseOverlay(
    settings: UserSettings,
    onSelectControlMode: (ControlMode) -> Unit,
    onResume: () -> Unit,
    onRestart: () -> Unit,
    onBackHome: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x883A2338))
            .safeDrawingPadding()
            .padding(20.dp),
        contentAlignment = Alignment.Center,
    ) {
        CandyCard(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 400.dp),
            containerColor = CandyVanillaBg,
            borderColor = CandyPink.copy(alpha = 0.45f),
            cornerRadius = 30.dp,
            contentPadding = PaddingValues(22.dp),
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Text(
                    text = stringResource(R.string.label_game_paused),
                    style = MaterialTheme.typography.headlineMedium,
                    color = CandyCocoaInk,
                )

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
internal fun GameOverOverlay(
    state: GameUiState,
    onRestart: () -> Unit,
    onOpenRecords: () -> Unit,
    onBackHome: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0x943A2338))
            .safeDrawingPadding()
            .padding(20.dp),
        contentAlignment = Alignment.Center,
    ) {
        CandyCard(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 410.dp),
            containerColor = CandyVanillaBg,
            borderColor = if (state.isNewRecord) CandyLemon else CandyPink.copy(alpha = 0.5f),
            cornerRadius = 30.dp,
            contentPadding = PaddingValues(22.dp),
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
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

                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    border = BorderStroke(2.dp, CandyPinkLight),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 12.dp),
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
internal fun SummaryStatPill(
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
