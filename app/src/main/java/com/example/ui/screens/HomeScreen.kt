package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AcUnit
import androidx.compose.material.icons.rounded.AllInclusive
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.DonutLarge
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.game.GameMode
import com.example.game.SnakeSkin
import com.example.game.UserSettings
import com.example.ui.components.Candy3DButton
import com.example.ui.components.CandyCard
import com.example.ui.components.CandyIconButton
import com.example.ui.theme.CandyBlueberry
import com.example.ui.theme.CandyBlueberryLight
import com.example.ui.theme.CandyBorder
import com.example.ui.theme.CandyCocoaInk
import com.example.ui.theme.CandyCocoaMuted
import com.example.ui.theme.CandyLemon
import com.example.ui.theme.CandyLemonDark
import com.example.ui.theme.CandyLemonLight
import com.example.ui.theme.CandyMint
import com.example.ui.theme.CandyMintLight
import com.example.ui.theme.CandyPink
import com.example.ui.theme.CandyPinkDark
import com.example.ui.theme.CandyPinkLight
import com.example.ui.theme.CandyVanillaBg
import com.example.ui.theme.PowerUpDonutColor
import com.example.ui.theme.PowerUpDoubleColor
import com.example.ui.theme.PowerUpSlowColor
import com.example.ui.theme.PowerUpSpeedColor

@Composable
fun HomeScreen(
    settings: UserSettings,
    classicBest: Int,
    endlessBest: Int,
    onSelectMode: (GameMode) -> Unit,
    onSelectSkin: (SnakeSkin) -> Unit,
    onStartGame: (GameMode) -> Unit,
    onOpenRecords: () -> Unit,
    onOpenSettings: () -> Unit,
) {
    val activeBest = if (settings.selectedMode == GameMode.CLASSIC) classicBest else endlessBest

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        CandyVanillaBg,
                        Color(0xFFFFF0F6),
                        CandyVanillaBg,
                    )
                )
            )
            .drawBehind {
                // Playful pastel sugar sprinkle circles in background
                drawCircle(
                    color = CandyPinkLight.copy(alpha = 0.65f),
                    radius = size.width * 0.28f,
                    center = Offset(size.width * 0.92f, size.height * 0.08f),
                )
                drawCircle(
                    color = CandyMintLight.copy(alpha = 0.7f),
                    radius = size.width * 0.24f,
                    center = Offset(size.width * 0.06f, size.height * 0.34f),
                )
                drawCircle(
                    color = CandyLemonLight.copy(alpha = 0.7f),
                    radius = size.width * 0.22f,
                    center = Offset(size.width * 0.90f, size.height * 0.78f),
                )
            },
    ) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            contentAlignment = Alignment.TopCenter,
        ) {
            val isWide = maxWidth >= 660.dp
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 880.dp)
                    .padding(horizontal = 20.dp),
            ) {
                // Top Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.app_name),
                            style = MaterialTheme.typography.headlineLarge,
                            color = CandyCocoaInk,
                        )
                        Text(
                            text = stringResource(R.string.app_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = CandyCocoaMuted,
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        CandyIconButton(
                            icon = Icons.Rounded.EmojiEvents,
                            contentDescription = stringResource(R.string.action_records),
                            onClick = onOpenRecords,
                            containerColor = CandyLemonLight,
                            borderColor = CandyLemon.copy(alpha = 0.55f),
                            iconTint = CandyLemonDark,
                            testTag = "home_records_button",
                        )
                        CandyIconButton(
                            icon = Icons.Rounded.Settings,
                            contentDescription = stringResource(R.string.action_settings),
                            onClick = onOpenSettings,
                            containerColor = Color.White,
                            borderColor = CandyBorder,
                            iconTint = CandyCocoaInk,
                            testTag = "home_settings_button",
                        )
                    }
                }

                // Scrollable Content Body
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    if (isWide) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.Top,
                        ) {
                            HeroBannerCard(
                                selectedMode = settings.selectedMode,
                                bestScore = activeBest,
                                modifier = Modifier.weight(1.05f),
                            )
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(14.dp),
                            ) {
                                ModeSelectorSection(
                                    selectedMode = settings.selectedMode,
                                    classicBest = classicBest,
                                    endlessBest = endlessBest,
                                    onSelectMode = onSelectMode,
                                )
                                SkinSelectorCard(
                                    selectedSkin = settings.snakeSkin,
                                    onSelectSkin = onSelectSkin,
                                )
                            }
                        }
                    } else {
                        HeroBannerCard(
                            selectedMode = settings.selectedMode,
                            bestScore = activeBest,
                            modifier = Modifier.fillMaxWidth(),
                        )
                        ModeSelectorSection(
                            selectedMode = settings.selectedMode,
                            classicBest = classicBest,
                            endlessBest = endlessBest,
                            onSelectMode = onSelectMode,
                        )
                        SkinSelectorCard(
                            selectedSkin = settings.snakeSkin,
                            onSelectSkin = onSelectSkin,
                        )
                    }

                    PowerUpGuideCard()

                    Spacer(modifier = Modifier.height(6.dp))
                }

                // Bottom Sticky Start CTA
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp, bottom = 14.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Candy3DButton(
                        text = stringResource(R.string.action_start_game),
                        icon = Icons.Rounded.PlayArrow,
                        containerColor = CandyPink,
                        shadowColor = CandyPinkDark,
                        onClick = { onStartGame(settings.selectedMode) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 460.dp),
                        height = 58.dp,
                        cornerRadius = 26.dp,
                        testTag = "start_game_button",
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroBannerCard(
    selectedMode: GameMode,
    bestScore: Int,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.height(176.dp),
        shape = RoundedCornerShape(28.dp),
        border = BorderStroke(2.5.dp, Color.White),
        shadowElevation = 6.dp,
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.img_candy_hero),
                contentDescription = stringResource(R.string.cd_hero_banner),
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
            // Subtle bottom readability gradient
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(78.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color(0x993A2338))
                        )
                    ),
            )
            // Floating Best Score Trophy Pill
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(14.dp),
                shape = RoundedCornerShape(20.dp),
                color = Color.White.copy(alpha = 0.94f),
                shadowElevation = 4.dp,
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.EmojiEvents,
                        contentDescription = null,
                        tint = CandyLemon,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${stringResource(selectedMode.labelRes)} · ${stringResource(R.string.label_best_score)}",
                        style = MaterialTheme.typography.labelMedium,
                        color = CandyCocoaMuted,
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = bestScore.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = CandyPinkDark,
                    )
                }
            }
        }
    }
}

@Composable
private fun ModeSelectorSection(
    selectedMode: GameMode,
    classicBest: Int,
    endlessBest: Int,
    onSelectMode: (GameMode) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = stringResource(R.string.label_choose_mode),
            style = MaterialTheme.typography.titleMedium,
            color = CandyCocoaInk,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            ModeOptionCard(
                mode = GameMode.CLASSIC,
                bestScore = classicBest,
                selected = selectedMode == GameMode.CLASSIC,
                icon = Icons.Rounded.GridView,
                accentColor = CandyMint,
                softBg = CandyMintLight,
                onClick = { onSelectMode(GameMode.CLASSIC) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("mode_card_classic"),
            )
            ModeOptionCard(
                mode = GameMode.ENDLESS,
                bestScore = endlessBest,
                selected = selectedMode == GameMode.ENDLESS,
                icon = Icons.Rounded.AllInclusive,
                accentColor = CandyBlueberry,
                softBg = CandyBlueberryLight,
                onClick = { onSelectMode(GameMode.ENDLESS) },
                modifier = Modifier
                    .weight(1f)
                    .testTag("mode_card_endless"),
            )
        }
    }
}

@Composable
private fun ModeOptionCard(
    mode: GameMode,
    bestScore: Int,
    selected: Boolean,
    icon: ImageVector,
    accentColor: Color,
    softBg: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bgColor by animateColorAsState(
        targetValue = if (selected) softBg else Color.White,
        label = "mode_card_bg",
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) accentColor else CandyBorder,
        label = "mode_card_border",
    )

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(22.dp),
        color = bgColor,
        border = BorderStroke(if (selected) 2.5.dp else 1.5.dp, borderColor),
        shadowElevation = if (selected) 5.dp else 2.dp,
        onClick = onClick,
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(accentColor),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp),
                    )
                }
                Surface(
                    shape = CircleShape,
                    color = Color.White.copy(alpha = 0.9f),
                ) {
                    Text(
                        text = "$bestScore 分",
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    )
                }
            }
            Text(
                text = stringResource(mode.labelRes),
                style = MaterialTheme.typography.titleMedium,
                color = CandyCocoaInk,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = stringResource(mode.descRes),
                style = MaterialTheme.typography.bodyMedium,
                color = CandyCocoaMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun SkinSelectorCard(
    selectedSkin: SnakeSkin,
    onSelectSkin: (SnakeSkin) -> Unit,
) {
    CandyCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(14.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.label_choose_skin),
                    style = MaterialTheme.typography.titleMedium,
                    color = CandyCocoaInk,
                )
                // Cute Live Mini Snake Preview
                MiniCandySnakePreview(
                    skin = selectedSkin,
                    modifier = Modifier
                        .width(104.dp)
                        .height(28.dp),
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                SnakeSkin.entries.forEach { skin ->
                    val isSelected = skin == selectedSkin
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onSelectSkin(skin) }
                            .padding(vertical = 4.dp)
                            .testTag("skin_option_${skin.storageKey}"),
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(skin.secondaryColor, skin.primaryColor)
                                    )
                                )
                                .border(
                                    width = if (isSelected) 3.dp else 1.5.dp,
                                    color = if (isSelected) CandyCocoaInk else skin.outlineColor.copy(alpha = 0.5f),
                                    shape = CircleShape,
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Rounded.Check,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(22.dp),
                                )
                            }
                        }
                        Text(
                            text = stringResource(skin.labelRes),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) CandyCocoaInk else CandyCocoaMuted,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MiniCandySnakePreview(
    skin: SnakeSkin,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier) {
        val r = size.height * 0.42f
        val y = size.height * 0.5f
        val startX = r * 1.1f
        val endX = size.width - r * 1.1f
        val segments = 5
        val step = (endX - startX) / (segments - 1)

        // Outer gummy outline
        drawLine(
            color = skin.outlineColor,
            start = Offset(startX, y),
            end = Offset(endX, y),
            strokeWidth = r * 2.15f,
            cap = StrokeCap.Round,
        )
        // Body
        drawLine(
            color = skin.primaryColor,
            start = Offset(startX, y),
            end = Offset(endX, y),
            strokeWidth = r * 1.75f,
            cap = StrokeCap.Round,
        )
        for (i in 0 until segments) {
            val cx = startX + i * step
            val segR = r * (0.72f + 0.25f * (i.toFloat() / (segments - 1)))
            if (i % 2 == 1 && i < segments - 1) {
                drawCircle(color = skin.secondaryColor, radius = segR * 0.72f, center = Offset(cx, y))
            }
            drawCircle(
                color = skin.highlightColor.copy(alpha = 0.75f),
                radius = segR * 0.24f,
                center = Offset(cx - segR * 0.25f, y - segR * 0.30f),
            )
        }
        // Cute Eyes on right-most head
        val headX = endX
        drawCircle(color = Color.White, radius = r * 0.34f, center = Offset(headX + r * 0.2f, y - r * 0.32f))
        drawCircle(color = Color.White, radius = r * 0.34f, center = Offset(headX + r * 0.2f, y + r * 0.32f))
        drawCircle(color = CandyCocoaInk, radius = r * 0.19f, center = Offset(headX + r * 0.30f, y - r * 0.32f))
        drawCircle(color = CandyCocoaInk, radius = r * 0.19f, center = Offset(headX + r * 0.30f, y + r * 0.32f))
    }
}

@Composable
private fun PowerUpGuideCard() {
    CandyCard(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(14.dp),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = stringResource(R.string.label_powerup_guide),
                style = MaterialTheme.typography.titleMedium,
                color = CandyCocoaInk,
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                PowerUpMiniPill(
                    icon = Icons.Rounded.Bolt,
                    label = "加速1.5x",
                    color = PowerUpSpeedColor,
                    modifier = Modifier.weight(1f),
                )
                PowerUpMiniPill(
                    icon = Icons.Rounded.Star,
                    label = "双倍2x",
                    color = PowerUpDoubleColor,
                    modifier = Modifier.weight(1f),
                )
                PowerUpMiniPill(
                    icon = Icons.Rounded.AcUnit,
                    label = "冰沙减速",
                    color = PowerUpSlowColor,
                    modifier = Modifier.weight(1f),
                )
                PowerUpMiniPill(
                    icon = Icons.Rounded.DonutLarge,
                    label = "+50分",
                    color = PowerUpDonutColor,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun PowerUpMiniPill(
    icon: ImageVector,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = color.copy(alpha = 0.14f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.35f)),
    ) {
        Column(
            modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(color),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp),
                )
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = CandyCocoaInk,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
