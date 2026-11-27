package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.CareerStats
import com.example.data.GameRecordEntity
import com.example.game.GameMode
import com.example.ui.components.Candy3DButton
import com.example.ui.components.CandyCard
import com.example.ui.components.CandyIconButton
import com.example.ui.components.SelectableCandyChip
import com.example.ui.theme.CandyBlueberry
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun RecordsScreen(
    initialMode: GameMode,
    classicRecords: List<GameRecordEntity>,
    endlessRecords: List<GameRecordEntity>,
    careerStats: CareerStats,
    onStartGame: (GameMode) -> Unit,
    onClearRecords: () -> Unit,
    onBack: () -> Unit,
) {
    var selectedMode by remember { mutableStateOf(initialMode) }
    var showClearConfirm by remember { mutableStateOf(false) }

    val records = if (selectedMode == GameMode.CLASSIC) classicRecords else endlessRecords
    val hasAnyRecords = classicRecords.isNotEmpty() || endlessRecords.isNotEmpty()
    val dateFormatter = remember { SimpleDateFormat("MM/dd HH:mm", Locale.getDefault()) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(CandyVanillaBg, Color(0xFFFFF1F6), CandyVanillaBg)
                )
            ),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 760.dp)
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                CandyIconButton(
                    icon = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = stringResource(R.string.cd_back),
                    onClick = onBack,
                    testTag = "records_back_button",
                )
                Text(
                    text = stringResource(R.string.label_records_title),
                    style = MaterialTheme.typography.headlineMedium,
                    color = CandyCocoaInk,
                )
                if (hasAnyRecords) {
                    CandyIconButton(
                        icon = Icons.Rounded.DeleteOutline,
                        contentDescription = stringResource(R.string.action_clear_records),
                        onClick = { showClearConfirm = true },
                        iconTint = CandyPinkDark,
                        testTag = "records_clear_button",
                    )
                } else {
                    Spacer(modifier = Modifier.size(48.dp))
                }
            }

            // Career Stats Banner
            CandyCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(14.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    CareerStatBox(
                        label = stringResource(R.string.label_total_games),
                        value = careerStats.totalGames.toString(),
                        bg = CandyPinkLight,
                        valueColor = CandyPinkDark,
                        modifier = Modifier.weight(1f),
                    )
                    CareerStatBox(
                        label = stringResource(R.string.label_max_length),
                        value = careerStats.maxLength.toString(),
                        bg = CandyMintLight,
                        valueColor = Color(0xFF158A63),
                        modifier = Modifier.weight(1f),
                    )
                    CareerStatBox(
                        label = stringResource(R.string.label_total_candies),
                        value = careerStats.totalCandies.toString(),
                        bg = CandyLemonLight,
                        valueColor = CandyLemonDark,
                        modifier = Modifier.weight(1f),
                    )
                }
            }

            // Mode Filter Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                SelectableCandyChip(
                    label = stringResource(GameMode.CLASSIC.labelRes),
                    selected = selectedMode == GameMode.CLASSIC,
                    activeColor = CandyMint,
                    onClick = { selectedMode = GameMode.CLASSIC },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("records_tab_classic"),
                )
                SelectableCandyChip(
                    label = stringResource(GameMode.ENDLESS.labelRes),
                    selected = selectedMode == GameMode.ENDLESS,
                    activeColor = CandyBlueberry,
                    onClick = { selectedMode = GameMode.ENDLESS },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("records_tab_endless"),
                )
            }

            // Records List or Cute Empty Jar State
            if (records.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    CandyCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .widthIn(max = 360.dp),
                        contentPadding = PaddingValues(24.dp),
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.img_empty_jar),
                                contentDescription = stringResource(R.string.cd_empty_jar),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(164.dp)
                                    .clip(RoundedCornerShape(28.dp)),
                            )
                            Text(
                                text = stringResource(R.string.label_empty_records_title),
                                style = MaterialTheme.typography.titleLarge,
                                color = CandyCocoaInk,
                                textAlign = TextAlign.Center,
                            )
                            Text(
                                text = stringResource(R.string.label_empty_records_desc),
                                style = MaterialTheme.typography.bodyMedium,
                                color = CandyCocoaMuted,
                                textAlign = TextAlign.Center,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Candy3DButton(
                                text = stringResource(R.string.action_start_game),
                                icon = Icons.Rounded.PlayArrow,
                                onClick = { onStartGame(selectedMode) },
                                modifier = Modifier.fillMaxWidth(),
                                height = 50.dp,
                                testTag = "records_empty_start_button",
                            )
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 18.dp),
                ) {
                    itemsIndexed(records, key = { _, item -> item.id }) { index, record ->
                        RecordRowCard(
                            rank = index + 1,
                            record = record,
                            formattedTime = dateFormatter.format(Date(record.timestamp)),
                        )
                    }
                }
            }
        }
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            containerColor = CandyVanillaBg,
            title = {
                Text(
                    text = stringResource(R.string.label_clear_confirm_title),
                    style = MaterialTheme.typography.titleLarge,
                    color = CandyCocoaInk,
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.label_clear_confirm_desc),
                    style = MaterialTheme.typography.bodyLarge,
                    color = CandyCocoaMuted,
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearConfirm = false
                        onClearRecords()
                    },
                    modifier = Modifier.testTag("confirm_clear_records_button"),
                ) {
                    Text(
                        text = stringResource(R.string.action_confirm),
                        color = CandyPinkDark,
                        fontWeight = FontWeight.Bold,
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text(
                        text = stringResource(R.string.action_cancel),
                        color = CandyCocoaMuted,
                    )
                }
            },
        )
    }
}

@Composable
private fun CareerStatBox(
    label: String,
    value: String,
    bg: Color,
    valueColor: Color,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = bg,
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                color = valueColor,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = CandyCocoaMuted,
            )
        }
    }
}

@Composable
private fun RecordRowCard(
    rank: Int,
    record: GameRecordEntity,
    formattedTime: String,
) {
    val badgeBg = when (rank) {
        1 -> CandyLemon
        2 -> Color(0xFFD6DEE8)
        3 -> Color(0xFFF0B27A)
        else -> CandyPinkLight
    }
    val badgeInk = when (rank) {
        1, 2, 3 -> CandyCocoaInk
        else -> CandyPinkDark
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(
            width = if (rank == 1) 2.dp else 1.2.dp,
            color = if (rank == 1) CandyLemon else CandyBorder,
        ),
        shadowElevation = if (rank <= 3) 4.dp else 1.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(badgeBg),
                    contentAlignment = Alignment.Center,
                ) {
                    if (rank == 1) {
                        Icon(
                            imageVector = Icons.Rounded.EmojiEvents,
                            contentDescription = null,
                            tint = CandyCocoaInk,
                            modifier = Modifier.size(22.dp),
                        )
                    } else {
                        Text(
                            text = "#$rank",
                            style = MaterialTheme.typography.labelLarge,
                            color = badgeInk,
                        )
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = record.score.toString(),
                            style = MaterialTheme.typography.headlineMedium,
                            color = CandyCocoaInk,
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "分",
                            style = MaterialTheme.typography.labelMedium,
                            color = CandyCocoaMuted,
                            modifier = Modifier.padding(bottom = 3.dp),
                        )
                    }
                    Text(
                        text = "蛇长 ${record.snakeLength} · 糖果 ${record.candiesEaten} · 道具 ${record.powerUpsUsed}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CandyCocoaMuted,
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${record.durationSeconds}s",
                    style = MaterialTheme.typography.labelLarge,
                    color = CandyPinkDark,
                )
                Text(
                    text = formattedTime,
                    style = MaterialTheme.typography.labelSmall,
                    color = CandyCocoaMuted,
                )
            }
        }
    }
}
