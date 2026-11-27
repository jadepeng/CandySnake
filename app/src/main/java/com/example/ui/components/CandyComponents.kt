package com.example.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material.icons.rounded.TouchApp
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material.icons.rounded.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.game.ControlMode
import com.example.game.Direction
import com.example.game.SpeedPreset
import com.example.game.UserSettings
import com.example.ui.theme.CandyBorder
import com.example.ui.theme.CandyCocoaInk
import com.example.ui.theme.CandyCocoaMuted
import com.example.ui.theme.CandyLemon
import com.example.ui.theme.CandyMint
import com.example.ui.theme.CandyMintDark
import com.example.ui.theme.CandyPink
import com.example.ui.theme.CandyPinkDark
import com.example.ui.theme.CandyPinkLight
import com.example.ui.theme.CandyVanillaBg

@Composable
fun Candy3DButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = CandyPink,
    shadowColor: Color = CandyPinkDark,
    contentColor: Color = Color.White,
    icon: ImageVector? = null,
    height: Dp = 56.dp,
    cornerRadius: Dp = 24.dp,
    testTag: String? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressOffset by animateDpAsState(
        targetValue = if (isPressed) 4.dp else 0.dp,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "candy_btn_offset",
    )

    val shape = RoundedCornerShape(cornerRadius)
    Box(
        modifier = modifier
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
            .minimumInteractiveComponentSize()
            .height(height + 5.dp)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                role = Role.Button,
                onClick = onClick,
            ),
    ) {
        // Bottom 3D Candy Ledge
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(top = 5.dp)
                .clip(shape)
                .background(shadowColor),
        )
        // Top Glossy Candy Face
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .offset(y = pressOffset)
                .clip(shape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            containerColor.copy(alpha = 0.96f),
                            containerColor,
                        )
                    )
                )
                .border(2.dp, Color.White.copy(alpha = 0.35f), shape),
            contentAlignment = Alignment.Center,
        ) {
            // Glossy sugar highlight strip along top
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 18.dp, vertical = 4.dp)
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.28f)),
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.padding(horizontal = 18.dp),
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(22.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleLarge,
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
fun CandyIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = Color.White,
    borderColor: Color = CandyBorder,
    iconTint: Color = CandyCocoaInk,
    size: Dp = 48.dp,
    testTag: String? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "icon_btn_scale",
    )

    Surface(
        modifier = modifier
            .then(if (testTag != null) Modifier.testTag(testTag) else Modifier)
            .size(size)
            .scale(scale),
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        shadowElevation = 3.dp,
        border = BorderStroke(1.5.dp, borderColor),
        onClick = onClick,
        interactionSource = interactionSource,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                tint = iconTint,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Composable
fun CandyCard(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = CandyBorder,
    cornerRadius: Dp = 24.dp,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius),
        color = containerColor,
        border = BorderStroke(1.5.dp, borderColor),
        shadowElevation = 4.dp,
    ) {
        Box(modifier = Modifier.padding(contentPadding)) {
            content()
        }
    }
}

@Composable
fun CandyDpad(
    onDirection: (Direction) -> Unit,
    accentColor: Color,
    shadowColor: Color,
    modifier: Modifier = Modifier,
    buttonSize: Dp = 60.dp,
) {
    val gap = 8.dp
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(gap),
    ) {
        DpadArrowKey(
            direction = Direction.UP,
            icon = Icons.Rounded.KeyboardArrowUp,
            contentDescription = stringResource(R.string.cd_dir_up),
            accentColor = accentColor,
            shadowColor = shadowColor,
            size = buttonSize,
            onPress = onDirection,
            testTag = "dpad_up_button",
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(gap),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            DpadArrowKey(
                direction = Direction.LEFT,
                icon = Icons.Rounded.KeyboardArrowLeft,
                contentDescription = stringResource(R.string.cd_dir_left),
                accentColor = accentColor,
                shadowColor = shadowColor,
                size = buttonSize,
                onPress = onDirection,
                testTag = "dpad_left_button",
            )
            // Center decorative candy emblem
            Box(
                modifier = Modifier
                    .size(buttonSize * 0.72f)
                    .clip(CircleShape)
                    .background(CandyPinkLight)
                    .border(2.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Rounded.AutoAwesome,
                    contentDescription = null,
                    tint = CandyPink,
                    modifier = Modifier.size(20.dp),
                )
            }
            DpadArrowKey(
                direction = Direction.RIGHT,
                icon = Icons.Rounded.KeyboardArrowRight,
                contentDescription = stringResource(R.string.cd_dir_right),
                accentColor = accentColor,
                shadowColor = shadowColor,
                size = buttonSize,
                onPress = onDirection,
                testTag = "dpad_right_button",
            )
        }
        DpadArrowKey(
            direction = Direction.DOWN,
            icon = Icons.Rounded.KeyboardArrowDown,
            contentDescription = stringResource(R.string.cd_dir_down),
            accentColor = accentColor,
            shadowColor = shadowColor,
            size = buttonSize,
            onPress = onDirection,
            testTag = "dpad_down_button",
        )
    }
}

@Composable
private fun DpadArrowKey(
    direction: Direction,
    icon: ImageVector,
    contentDescription: String,
    accentColor: Color,
    shadowColor: Color,
    size: Dp,
    onPress: (Direction) -> Unit,
    testTag: String,
) {
    var pressed by remember { mutableStateOf(false) }
    val pressOffset by animateDpAsState(
        targetValue = if (pressed) 4.dp else 0.dp,
        animationSpec = spring(stiffness = Spring.StiffnessHigh),
        label = "dpad_offset",
    )
    val shape = RoundedCornerShape(20.dp)

    Box(
        modifier = Modifier
            .testTag(testTag)
            .size(width = size, height = size + 4.dp)
            .semantics(mergeDescendants = true) {
                this.contentDescription = contentDescription
                this.role = Role.Button
                this.onClick {
                    onPress(direction)
                    true
                }
            }
            .pointerInput(direction) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    pressed = true
                    onPress(direction)
                    waitForUpOrCancellation()
                    pressed = false
                }
            },
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(top = 4.dp)
                .clip(shape)
                .background(shadowColor),
        )
        Box(
            modifier = Modifier
                .size(size)
                .offset(y = pressOffset)
                .clip(shape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.White, accentColor.copy(alpha = 0.22f))
                    )
                )
                .border(2.dp, accentColor.copy(alpha = 0.65f), shape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = shadowColor,
                modifier = Modifier.size(34.dp),
            )
        }
    }
}

@Composable
fun SettingsDialog(
    settings: UserSettings,
    onControlModeSelected: (ControlMode) -> Unit,
    onSpeedSelected: (SpeedPreset) -> Unit,
    onSoundToggle: (Boolean) -> Unit,
    onHapticsToggle: (Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        CandyCard(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 460.dp),
            containerColor = CandyVanillaBg,
            borderColor = CandyPink.copy(alpha = 0.35f),
            cornerRadius = 28.dp,
            contentPadding = PaddingValues(20.dp),
        ) {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = stringResource(R.string.action_settings),
                        style = MaterialTheme.typography.headlineMedium,
                        color = CandyCocoaInk,
                    )
                    CandyIconButton(
                        icon = Icons.Rounded.Close,
                        contentDescription = stringResource(R.string.action_close),
                        onClick = onDismiss,
                        size = 42.dp,
                        testTag = "settings_close_button",
                    )
                }

                // Control Mode selector
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.TouchApp,
                            contentDescription = null,
                            tint = CandyPink,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.label_control_mode),
                            style = MaterialTheme.typography.titleMedium,
                            color = CandyCocoaInk,
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        ControlMode.entries.forEach { mode ->
                            val selected = settings.controlMode == mode
                            SelectableCandyChip(
                                label = stringResource(mode.labelRes),
                                selected = selected,
                                activeColor = CandyPink,
                                onClick = { onControlModeSelected(mode) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("setting_control_${mode.storageKey}"),
                            )
                        }
                    }
                    Text(
                        text = stringResource(settings.controlMode.hintRes),
                        style = MaterialTheme.typography.bodyMedium,
                        color = CandyCocoaMuted,
                    )
                }

                // Speed Preset selector
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Speed,
                            contentDescription = null,
                            tint = CandyMintDark,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = stringResource(R.string.label_start_speed),
                            style = MaterialTheme.typography.titleMedium,
                            color = CandyCocoaInk,
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        SpeedPreset.entries.forEach { preset ->
                            SelectableCandyChip(
                                label = stringResource(preset.labelRes),
                                selected = settings.speedPreset == preset,
                                activeColor = CandyMint,
                                onClick = { onSpeedSelected(preset) },
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("setting_speed_${preset.storageKey}"),
                            )
                        }
                    }
                }

                // Sound & Haptics switches
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, CandyBorder),
                ) {
                    Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.VolumeUp,
                                    contentDescription = null,
                                    tint = CandyLemon,
                                    modifier = Modifier.size(20.dp),
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = stringResource(R.string.label_sound),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = CandyCocoaInk,
                                )
                            }
                            Switch(
                                checked = settings.soundEnabled,
                                onCheckedChange = onSoundToggle,
                                modifier = Modifier.testTag("setting_sound_switch"),
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = CandyPink,
                                ),
                            )
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.Vibration,
                                    contentDescription = null,
                                    tint = CandyMint,
                                    modifier = Modifier.size(20.dp),
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = stringResource(R.string.label_haptics),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = CandyCocoaInk,
                                )
                            }
                            Switch(
                                checked = settings.hapticsEnabled,
                                onCheckedChange = onHapticsToggle,
                                modifier = Modifier.testTag("setting_haptics_switch"),
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = CandyMint,
                                ),
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SelectableCandyChip(
    label: String,
    selected: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(16.dp),
        color = if (selected) activeColor else Color.White,
        border = BorderStroke(
            width = 1.5.dp,
            color = if (selected) activeColor else CandyBorder,
        ),
        shadowElevation = if (selected) 3.dp else 0.dp,
        onClick = onClick,
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 6.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (selected) Color.White else CandyCocoaInk,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
