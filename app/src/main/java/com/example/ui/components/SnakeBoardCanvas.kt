package com.example.ui.components

import android.graphics.Paint
import android.graphics.Typeface
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.game.BoardPowerUp
import com.example.game.ControlMode
import com.example.game.Direction
import com.example.game.FoodStyle
import com.example.game.GameMode
import com.example.game.GamePhase
import com.example.game.GameUiState
import com.example.game.GridPoint
import com.example.game.PowerUpType
import com.example.game.SnakeSkin
import com.example.ui.theme.BoardClassicTileDark
import com.example.ui.theme.BoardClassicTileLight
import com.example.ui.theme.BoardClassicWall
import com.example.ui.theme.BoardClassicWallShadow
import com.example.ui.theme.BoardEndlessPortalBorder
import com.example.ui.theme.BoardEndlessPortalGlow
import com.example.ui.theme.BoardEndlessTileDark
import com.example.ui.theme.BoardEndlessTileLight
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

@Composable
fun SnakeBoardCanvas(
    state: GameUiState,
    skin: SnakeSkin,
    controlMode: ControlMode,
    onDirection: (Direction) -> Unit,
    onBoardTap: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val swipeThresholdPx = with(LocalDensity.current) { 24.dp.toPx() }
    val swipeEnabled = controlMode != ControlMode.DPAD_ONLY

    val strokeTextPaint = remember {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
            style = Paint.Style.STROKE
            strokeWidth = 7f
            strokeJoin = Paint.Join.ROUND
            color = android.graphics.Color.WHITE
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
    }
    val fillTextPaint = remember {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            textAlign = Paint.Align.CENTER
            style = Paint.Style.FILL
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
    }

    val gestureModifier = if (swipeEnabled) {
        Modifier
            .pointerInput(Unit) {
                detectTapGestures(onTap = { onBoardTap() })
            }
            .pointerInput(swipeThresholdPx) {
                var accumX = 0f
                var accumY = 0f
                detectDragGestures(
                    onDragStart = {
                        accumX = 0f
                        accumY = 0f
                    },
                    onDragEnd = {
                        accumX = 0f
                        accumY = 0f
                    },
                    onDragCancel = {
                        accumX = 0f
                        accumY = 0f
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        accumX += dragAmount.x
                        accumY += dragAmount.y
                        if (abs(accumX) >= swipeThresholdPx || abs(accumY) >= swipeThresholdPx) {
                            val dir = if (abs(accumX) > abs(accumY)) {
                                if (accumX > 0f) Direction.RIGHT else Direction.LEFT
                            } else {
                                if (accumY > 0f) Direction.DOWN else Direction.UP
                            }
                            onDirection(dir)
                            accumX = 0f
                            accumY = 0f
                        }
                    },
                )
            }
    } else {
        Modifier.pointerInput(Unit) {
            detectTapGestures(onTap = { onBoardTap() })
        }
    }

    val boardAspect = state.cols.toFloat() / state.rows.toFloat()

    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = androidx.compose.ui.Alignment.Center,
    ) {
        val widthBound = maxWidth
        val heightBound = maxHeight
        val (targetWidth, targetHeight) = if (widthBound / heightBound > boardAspect) {
            val h = heightBound
            (h * boardAspect) to h
        } else {
            val w = widthBound
            w to (w / boardAspect)
        }

        Box(
            modifier = Modifier
                .size(width = targetWidth, height = targetHeight)
                .testTag("snake_game_board")
                .then(gestureModifier),
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
            val frameThickness = size.minDimension * 0.032f
            val innerWidth = size.width - frameThickness * 2f
            val innerHeight = size.height - frameThickness * 2f
            val cellW = innerWidth / state.cols
            val cellH = innerHeight / state.rows
            val boardLeft = frameThickness
            val boardTop = frameThickness
            val outerCorner = frameThickness * 2.1f

            // 1. Outer Candy Frame (Wafer Wall for Classic, Glowing Portal for Endless)
            drawBoardFrame(
                mode = state.mode,
                outerCorner = outerCorner,
                frameThickness = frameThickness,
                pulseSec = state.pulseClockSec,
            )

            // 2. Pastel Checkerboard Tiles
            val lightTile = if (state.mode == GameMode.CLASSIC) BoardClassicTileLight else BoardEndlessTileLight
            val darkTile = if (state.mode == GameMode.CLASSIC) BoardClassicTileDark else BoardEndlessTileDark
            drawRoundRect(
                color = lightTile,
                topLeft = Offset(boardLeft, boardTop),
                size = Size(innerWidth, innerHeight),
                cornerRadius = CornerRadius(outerCorner * 0.62f, outerCorner * 0.62f),
            )
            for (row in 0 until state.rows) {
                for (col in 0 until state.cols) {
                    if ((row + col) % 2 == 1) {
                        drawRoundRect(
                            color = darkTile,
                            topLeft = Offset(
                                boardLeft + col * cellW + 0.5f,
                                boardTop + row * cellH + 0.5f,
                            ),
                            size = Size(cellW - 1f, cellH - 1f),
                            cornerRadius = CornerRadius(cellW * 0.14f, cellW * 0.14f),
                        )
                    }
                }
            }

            // 3. Regular Candy Food
            drawCandyFood(
                pos = state.food,
                style = state.foodStyle,
                boardLeft = boardLeft,
                boardTop = boardTop,
                cellW = cellW,
                cellH = cellH,
                pulseSec = state.pulseClockSec,
            )

            // 4. Special Power-Up (with remaining life ring)
            state.boardPowerUp?.let { powerUp ->
                drawPowerUp(
                    powerUp = powerUp,
                    boardLeft = boardLeft,
                    boardTop = boardTop,
                    cellW = cellW,
                    cellH = cellH,
                    pulseSec = state.pulseClockSec,
                )
            }

            // 5. Smooth Interpolated Jelly Candy Snake
            if (state.snake.isNotEmpty()) {
                drawCandySnake(
                    state = state,
                    skin = skin,
                    boardLeft = boardLeft,
                    boardTop = boardTop,
                    cellW = cellW,
                    cellH = cellH,
                )
            }

            // 6. Confetti Sugar Particles
            state.particles.forEach { p ->
                val cx = boardLeft + p.x * cellW
                val cy = boardTop + p.y * cellH
                drawCircle(
                    color = p.color.copy(alpha = p.life.coerceIn(0f, 1f)),
                    radius = p.radius * cellW * (0.4f + 0.6f * p.life),
                    center = Offset(cx, cy),
                )
            }

            // 7. Floating Score Texts
            if (state.floatingScores.isNotEmpty()) {
                val fontSize = cellW * 0.85f
                strokeTextPaint.textSize = fontSize
                fillTextPaint.textSize = fontSize
                state.floatingScores.forEach { popup ->
                    val alpha = (1f - popup.progress).coerceIn(0f, 1f)
                    val px = boardLeft + popup.gridX * cellW
                    val py = boardTop + popup.gridY * cellH
                    strokeTextPaint.alpha = (alpha * 255).toInt()
                    fillTextPaint.color = popup.color.toArgb()
                    fillTextPaint.alpha = (alpha * 255).toInt()
                    drawContext.canvas.nativeCanvas.apply {
                        drawText(popup.text, px, py, strokeTextPaint)
                        drawText(popup.text, px, py, fillTextPaint)
                    }
                }
            }

            // 8. Power-up / New Record Border Flash Glow
            val flashColor = state.borderFlashColor
            if (flashColor != null && state.borderFlashAlpha > 0.01f) {
                drawRoundRect(
                    color = flashColor.copy(alpha = state.borderFlashAlpha * 0.65f),
                    topLeft = Offset(frameThickness * 0.5f, frameThickness * 0.5f),
                    size = Size(size.width - frameThickness, size.height - frameThickness),
                    cornerRadius = CornerRadius(outerCorner, outerCorner),
                    style = Stroke(width = frameThickness * 1.1f),
                )
            }
            }
        }
    }
}

private fun DrawScope.drawBoardFrame(
    mode: GameMode,
    outerCorner: Float,
    frameThickness: Float,
    pulseSec: Float,
) {
    if (mode == GameMode.CLASSIC) {
        // Thick Caramel Cookie Solid Wall Border
        drawRoundRect(
            color = BoardClassicWallShadow,
            topLeft = Offset(0f, frameThickness * 0.35f),
            size = Size(size.width, size.height - frameThickness * 0.35f),
            cornerRadius = CornerRadius(outerCorner, outerCorner),
        )
        drawRoundRect(
            color = BoardClassicWall,
            topLeft = Offset.Zero,
            size = Size(size.width, size.height - frameThickness * 0.4f),
            cornerRadius = CornerRadius(outerCorner, outerCorner),
        )
    } else {
        // Dreamy Portal Outer Frame with animated glowing dashed edge
        drawRoundRect(
            color = BoardEndlessPortalBorder.copy(alpha = 0.25f),
            topLeft = Offset.Zero,
            size = size,
            cornerRadius = CornerRadius(outerCorner, outerCorner),
        )
        val dashLen = frameThickness * 1.9f
        val phase = (pulseSec * 36f) % (dashLen * 2f)
        drawRoundRect(
            color = BoardEndlessPortalBorder,
            topLeft = Offset(frameThickness * 0.5f, frameThickness * 0.5f),
            size = Size(size.width - frameThickness, size.height - frameThickness),
            cornerRadius = CornerRadius(outerCorner * 0.88f, outerCorner * 0.88f),
            style = Stroke(
                width = frameThickness * 0.55f,
                cap = StrokeCap.Round,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashLen, dashLen * 0.85f), phase),
            ),
        )
        drawRoundRect(
            color = BoardEndlessPortalGlow.copy(alpha = 0.55f),
            topLeft = Offset(frameThickness * 0.5f, frameThickness * 0.5f),
            size = Size(size.width - frameThickness, size.height - frameThickness),
            cornerRadius = CornerRadius(outerCorner * 0.88f, outerCorner * 0.88f),
            style = Stroke(
                width = frameThickness * 0.25f,
                cap = StrokeCap.Round,
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(dashLen * 0.6f, dashLen * 1.25f), -phase),
            ),
        )
    }
}

private fun DrawScope.drawCandyFood(
    pos: GridPoint,
    style: FoodStyle,
    boardLeft: Float,
    boardTop: Float,
    cellW: Float,
    cellH: Float,
    pulseSec: Float,
) {
    val cx = boardLeft + (pos.x + 0.5f) * cellW
    val cy = boardTop + (pos.y + 0.5f) * cellH
    val bounce = 1f + 0.09f * sin(pulseSec * 5.2f)
    val radius = (minOf(cellW, cellH) * 0.37f) * bounce

    // Soft shadow
    drawOval(
        color = Color(0x283A2338),
        topLeft = Offset(cx - radius * 0.9f, cy + radius * 0.65f),
        size = Size(radius * 1.8f, radius * 0.5f),
    )

    // Cute candy wrapper triangles on left and right
    val leftWrapper = Path().apply {
        moveTo(cx - radius * 0.75f, cy)
        lineTo(cx - radius * 1.38f, cy - radius * 0.52f)
        lineTo(cx - radius * 1.38f, cy + radius * 0.52f)
        close()
    }
    val rightWrapper = Path().apply {
        moveTo(cx + radius * 0.75f, cy)
        lineTo(cx + radius * 1.38f, cy - radius * 0.52f)
        lineTo(cx + radius * 1.38f, cy + radius * 0.52f)
        close()
    }
    drawPath(leftWrapper, color = style.stripeColor)
    drawPath(rightWrapper, color = style.stripeColor)

    // Main candy orb
    drawCircle(
        color = style.primaryColor,
        radius = radius,
        center = Offset(cx, cy),
    )
    // Decorative diagonal candy stripe
    drawLine(
        color = style.stripeColor.copy(alpha = 0.85f),
        start = Offset(cx - radius * 0.52f, cy + radius * 0.45f),
        end = Offset(cx + radius * 0.52f, cy - radius * 0.45f),
        strokeWidth = radius * 0.35f,
        cap = StrokeCap.Round,
    )
    // White candy rim
    drawCircle(
        color = Color.White.copy(alpha = 0.6f),
        radius = radius,
        center = Offset(cx, cy),
        style = Stroke(width = radius * 0.14f),
    )
    // Glossy specular shine dot
    drawCircle(
        color = Color.White.copy(alpha = 0.88f),
        radius = radius * 0.24f,
        center = Offset(cx - radius * 0.32f, cy - radius * 0.32f),
    )
}

private fun DrawScope.drawPowerUp(
    powerUp: BoardPowerUp,
    boardLeft: Float,
    boardTop: Float,
    cellW: Float,
    cellH: Float,
    pulseSec: Float,
) {
    val cx = boardLeft + (powerUp.position.x + 0.5f) * cellW
    val cy = boardTop + (powerUp.position.y + 0.5f) * cellH
    val pulse = 1f + 0.10f * sin(pulseSec * 7f)
    val baseR = minOf(cellW, cellH) * 0.44f

    // Glowing halo
    drawCircle(
        color = powerUp.type.accentColor.copy(alpha = 0.22f),
        radius = baseR * 1.25f * pulse,
        center = Offset(cx, cy),
    )

    // Countdown arc ring around the power-up
    val ringR = baseR * 1.08f
    drawArc(
        color = Color.White.copy(alpha = 0.75f),
        startAngle = -90f,
        sweepAngle = 360f,
        useCenter = false,
        topLeft = Offset(cx - ringR, cy - ringR),
        size = Size(ringR * 2f, ringR * 2f),
        style = Stroke(width = cellW * 0.09f),
    )
    drawArc(
        color = powerUp.type.accentColor,
        startAngle = -90f,
        sweepAngle = 360f * powerUp.lifeFraction,
        useCenter = false,
        topLeft = Offset(cx - ringR, cy - ringR),
        size = Size(ringR * 2f, ringR * 2f),
        style = Stroke(width = cellW * 0.10f, cap = StrokeCap.Round),
    )

    val r = baseR * 0.84f * pulse
    when (powerUp.type) {
        PowerUpType.SPEED_BOOST -> {
            // Golden Lightning Bubblegum Orb
            drawCircle(color = Color(0xFFFF9F1C), radius = r, center = Offset(cx, cy))
            val bolt = Path().apply {
                moveTo(cx + r * 0.12f, cy - r * 0.68f)
                lineTo(cx - r * 0.45f, cy + r * 0.05f)
                lineTo(cx + r * 0.02f, cy + r * 0.05f)
                lineTo(cx - r * 0.16f, cy + r * 0.68f)
                lineTo(cx + r * 0.45f, cy - r * 0.08f)
                lineTo(cx - r * 0.02f, cy - r * 0.08f)
                close()
            }
            drawPath(bolt, color = Color(0xFFFFF8D6))
        }
        PowerUpType.DOUBLE_SCORE -> {
            // Golden Star Lollipop
            drawCircle(color = Color(0xFFFF4D8D), radius = r, center = Offset(cx, cy))
            val star = createStarPath(cx, cy, r * 0.76f, r * 0.36f, 5)
            drawPath(star, color = Color(0xFFFFDF5D))
        }
        PowerUpType.SLOW_MO -> {
            // Mint Slush Berry with Frost Cross
            drawCircle(color = Color(0xFF2EC4E6), radius = r, center = Offset(cx, cy))
            val flakeStroke = r * 0.22f
            drawLine(
                color = Color.White,
                start = Offset(cx, cy - r * 0.56f),
                end = Offset(cx, cy + r * 0.56f),
                strokeWidth = flakeStroke,
                cap = StrokeCap.Round,
            )
            drawLine(
                color = Color.White,
                start = Offset(cx - r * 0.56f, cy),
                end = Offset(cx + r * 0.56f, cy),
                strokeWidth = flakeStroke,
                cap = StrokeCap.Round,
            )
            drawCircle(color = Color(0xFFE0FAFF), radius = r * 0.22f, center = Offset(cx, cy))
        }
        PowerUpType.RAINBOW_DONUT -> {
            // Glazed Rainbow Donut
            drawCircle(
                color = Color(0xFFF4B97A),
                radius = r * 0.96f,
                center = Offset(cx, cy),
                style = Stroke(width = r * 0.56f),
            )
            val colors = listOf(
                Color(0xFFFF5C8A),
                Color(0xFFFFB830),
                Color(0xFF2EC492),
                Color(0xFF38BDF8),
                Color(0xFFA855F7),
            )
            colors.forEachIndexed { idx, col ->
                drawArc(
                    color = col,
                    startAngle = idx * 72f + pulseSec * 40f,
                    sweepAngle = 72f,
                    useCenter = false,
                    topLeft = Offset(cx - r * 0.72f, cy - r * 0.72f),
                    size = Size(r * 1.44f, r * 1.44f),
                    style = Stroke(width = r * 0.38f, cap = StrokeCap.Round),
                )
            }
            // Donut hole
            drawCircle(color = Color.White.copy(alpha = 0.92f), radius = r * 0.28f, center = Offset(cx, cy))
        }
    }
}

private fun DrawScope.drawCandySnake(
    state: GameUiState,
    skin: SnakeSkin,
    boardLeft: Float,
    boardTop: Float,
    cellW: Float,
    cellH: Float,
) {
    val snake = state.snake
    val prev = state.previousSnake
    val t = state.moveProgress.coerceIn(0f, 1f)

    // Compute smoothly interpolated center positions for each segment
    val centers = List(snake.size) { index ->
        val currPt = snake[index]
        val prevPt = prev.getOrNull(index) ?: currPt
        val isWrapJump = abs(currPt.x - prevPt.x) > 1 || abs(currPt.y - prevPt.y) > 1
        val interpX = if (isWrapJump) currPt.x.toFloat() else prevPt.x + (currPt.x - prevPt.x) * t
        val interpY = if (isWrapJump) currPt.y.toFloat() else prevPt.y + (currPt.y - prevPt.y) * t
        Offset(
            x = boardLeft + (interpX + 0.5f) * cellW,
            y = boardTop + (interpY + 0.5f) * cellH,
        )
    }

    val baseDiameter = minOf(cellW, cellH) * 0.82f
    val hasSpeed = state.activeEffects.any { it.type == PowerUpType.SPEED_BOOST }
    val hasDouble = state.activeEffects.any { it.type == PowerUpType.DOUBLE_SCORE }
    val hasSlow = state.activeEffects.any { it.type == PowerUpType.SLOW_MO }

    val auraColor = when {
        hasDouble -> Color(0xFFFF4D8D)
        hasSpeed -> Color(0xFFFFB830)
        hasSlow -> Color(0xFF2EC4E6)
        else -> null
    }

    // 1. Soft shadow & optional power-up aura beneath body
    for (i in centers.lastIndex downTo 0) {
        val ratio = if (centers.size <= 1) 1f else 1f - (i.toFloat() / centers.size) * 0.28f
        val r = (baseDiameter * 0.5f) * ratio
        val c = centers[i]
        if (auraColor != null) {
            drawCircle(
                color = auraColor.copy(alpha = 0.22f),
                radius = r * 1.32f,
                center = c,
            )
        }
        drawCircle(
            color = Color(0x243A2338),
            radius = r,
            center = Offset(c.x, c.y + cellH * 0.14f),
        )
    }

    // 2. Outer gummy outline layer (connecting adjacent non-wrapping segments)
    for (i in centers.lastIndex downTo 1) {
        val a = centers[i]
        val b = centers[i - 1]
        val dist = hypot(b.x - a.x, b.y - a.y)
        if (dist < cellW * 2.1f) {
            val ratio = 1f - (i.toFloat() / centers.size) * 0.25f
            drawLine(
                color = skin.outlineColor,
                start = a,
                end = b,
                strokeWidth = baseDiameter * ratio + cellW * 0.10f,
                cap = StrokeCap.Round,
            )
        }
    }
    centers.forEachIndexed { idx, c ->
        val ratio = if (idx == 0) 1.06f else 1f - (idx.toFloat() / centers.size) * 0.25f
        drawCircle(
            color = skin.outlineColor,
            radius = (baseDiameter * 0.5f) * ratio + cellW * 0.05f,
            center = c,
        )
    }

    // 3. Vibrant inner jelly fill + alternating candy belly spots + specular gloss
    for (i in centers.lastIndex downTo 1) {
        val a = centers[i]
        val b = centers[i - 1]
        val dist = hypot(b.x - a.x, b.y - a.y)
        if (dist < cellW * 2.1f) {
            val ratio = 1f - (i.toFloat() / centers.size) * 0.25f
            drawLine(
                color = skin.primaryColor,
                start = a,
                end = b,
                strokeWidth = baseDiameter * ratio,
                cap = StrokeCap.Round,
            )
        }
    }
    for (i in centers.lastIndex downTo 0) {
        val c = centers[i]
        val ratio = if (i == 0) 1.06f else 1f - (i.toFloat() / centers.size) * 0.25f
        val r = (baseDiameter * 0.5f) * ratio
        drawCircle(
            color = if (i % 2 == 1) skin.secondaryColor.copy(alpha = 0.92f) else skin.primaryColor,
            radius = r,
            center = c,
        )
        // Specular jelly shine dot
        drawCircle(
            color = skin.highlightColor.copy(alpha = 0.68f),
            radius = r * 0.26f,
            center = Offset(c.x - r * 0.30f, c.y - r * 0.32f),
        )
    }

    // 4. Expressive Cartoon Head Details (Eyes, Cheeks, Tongue, Record Crown)
    val headCenter = centers.first()
    val headR = baseDiameter * 0.53f
    val dir = state.direction
    val fx = dir.dx.toFloat()
    val fy = dir.dy.toFloat()
    // Perpendicular vector for left/right eye placement
    val px = -fy
    val py = fx

    // Cute flickering pink candy tongue when moving
    if (state.phase == GamePhase.PLAYING && sin(state.pulseClockSec * 7.5f) > 0.35f) {
        val tongueBase = Offset(headCenter.x + fx * headR * 0.85f, headCenter.y + fy * headR * 0.85f)
        val tongueTip = Offset(headCenter.x + fx * headR * 1.48f, headCenter.y + fy * headR * 1.48f)
        drawLine(
            color = Color(0xFFFF4D8D),
            start = tongueBase,
            end = tongueTip,
            strokeWidth = headR * 0.24f,
            cap = StrokeCap.Round,
        )
    }

    // Rosy cartoon cheeks
    val cheekOffsetForward = headR * 0.08f
    val cheekOffsetSide = headR * 0.58f
    drawCircle(
        color = skin.cheekColor.copy(alpha = 0.72f),
        radius = headR * 0.22f,
        center = Offset(
            headCenter.x + fx * cheekOffsetForward + px * cheekOffsetSide,
            headCenter.y + fy * cheekOffsetForward + py * cheekOffsetSide,
        ),
    )
    drawCircle(
        color = skin.cheekColor.copy(alpha = 0.72f),
        radius = headR * 0.22f,
        center = Offset(
            headCenter.x + fx * cheekOffsetForward - px * cheekOffsetSide,
            headCenter.y + fy * cheekOffsetForward - py * cheekOffsetSide,
        ),
    )

    // Check if snake is excited (close to food!)
    val headGrid = snake.first()
    val distToFood = hypot((state.food.x - headGrid.x).toFloat(), (state.food.y - headGrid.y).toFloat())
    val excitedScale = if (distToFood <= 2.4f) 1.15f else 1f

    val eyeForward = headR * 0.28f
    val eyeSide = headR * 0.42f
    val eyeR = headR * 0.30f * excitedScale

    val leftEye = Offset(
        headCenter.x + fx * eyeForward + px * eyeSide,
        headCenter.y + fy * eyeForward + py * eyeSide,
    )
    val rightEye = Offset(
        headCenter.x + fx * eyeForward - px * eyeSide,
        headCenter.y + fy * eyeForward - py * eyeSide,
    )

    if (state.phase == GamePhase.GAME_OVER) {
        // Comical cartoon "X" dizzy eyes on crash
        listOf(leftEye, rightEye).forEach { eye ->
            drawCircle(color = Color.White, radius = eyeR, center = eye)
            val d = eyeR * 0.46f
            drawLine(
                color = Color(0xFF3A2338),
                start = Offset(eye.x - d, eye.y - d),
                end = Offset(eye.x + d, eye.y + d),
                strokeWidth = headR * 0.13f,
                cap = StrokeCap.Round,
            )
            drawLine(
                color = Color(0xFF3A2338),
                start = Offset(eye.x + d, eye.y - d),
                end = Offset(eye.x - d, eye.y + d),
                strokeWidth = headR * 0.13f,
                cap = StrokeCap.Round,
            )
        }
    } else {
        // Sparkling expressive eyes tracking movement direction
        listOf(leftEye, rightEye).forEach { eye ->
            drawCircle(color = Color.White, radius = eyeR, center = eye)
            val pupilCenter = Offset(eye.x + fx * eyeR * 0.28f, eye.y + fy * eyeR * 0.28f)
            drawCircle(color = Color(0xFF2D192B), radius = eyeR * 0.56f, center = pupilCenter)
            drawCircle(
                color = Color.White,
                radius = eyeR * 0.22f,
                center = Offset(pupilCenter.x - eyeR * 0.18f, pupilCenter.y - eyeR * 0.18f),
            )
        }
    }

    // Mini Golden Crown when beating Personal Best!
    if (state.isNewRecord) {
        val crownCenter = Offset(headCenter.x, headCenter.y - headR * 1.08f)
        val cw = headR * 0.95f
        val ch = headR * 0.58f
        val crownPath = Path().apply {
            moveTo(crownCenter.x - cw * 0.5f, crownCenter.y + ch * 0.4f)
            lineTo(crownCenter.x - cw * 0.55f, crownCenter.y - ch * 0.35f)
            lineTo(crownCenter.x - cw * 0.18f, crownCenter.y)
            lineTo(crownCenter.x, crownCenter.y - ch * 0.55f)
            lineTo(crownCenter.x + cw * 0.18f, crownCenter.y)
            lineTo(crownCenter.x + cw * 0.55f, crownCenter.y - ch * 0.35f)
            lineTo(crownCenter.x + cw * 0.5f, crownCenter.y + ch * 0.4f)
            close()
        }
        drawPath(crownPath, color = Color(0xFFFFC93C))
        drawPath(
            crownPath,
            color = Color(0xFFD98E0E),
            style = Stroke(width = headR * 0.08f, join = StrokeJoin.Round),
        )
    }
}

private fun createStarPath(
    cx: Float,
    cy: Float,
    outerR: Float,
    innerR: Float,
    points: Int,
): Path {
    val path = Path()
    val step = PI / points
    var angle = -PI / 2.0
    for (i in 0 until points * 2) {
        val r = if (i % 2 == 0) outerR else innerR
        val x = cx + (cos(angle) * r).toFloat()
        val y = cy + (sin(angle) * r).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        angle += step
    }
    path.close()
    return path
}
