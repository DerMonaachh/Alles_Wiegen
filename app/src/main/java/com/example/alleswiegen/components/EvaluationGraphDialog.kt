package com.example.alleswiegen.components

import android.content.res.Configuration
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.alleswiegen.Player
import kotlinx.coroutines.delay
import kotlin.math.max

data class PlayerRoundData(
    val roundIndex: Int, // 0-based round
    val errorInRound: Int,
    val cumulativeError: Int, // Positive absolute error value
    val hasBagelExxer: Boolean
)

data class PlayerChartSeries(
    val player: Player,
    val color: Color,
    val rounds: List<PlayerRoundData>
)

val PlayerChartColors = listOf(
    Color(0xFF2196F3), // Blue
    Color(0xFFE91E63), // Pink
    Color(0xFF4CAF50), // Green
    Color(0xFFFF9800), // Orange
    Color(0xFF9C27B0), // Purple
    Color(0xFF00BCD4), // Cyan
    Color(0xFFFFEB3B), // Yellow
    Color(0xFF795548), // Brown
    Color(0xFF673AB7), // Deep Purple
    Color(0xFF009688), // Teal
    Color(0xFFF44336), // Red
    Color(0xFF607D8B)  // Blue Grey
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EvaluationGraphDialog(
    players: List<Player>,
    onDismiss: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Process players sipsErrors into round series, starting with Round 0 (0g error)
    val seriesList = remember(players) {
        players.mapIndexed { index, player ->
            val rounds = mutableListOf<PlayerRoundData>()
            // Always start at Round 0 with 0 error
            rounds.add(
                PlayerRoundData(
                    roundIndex = 0,
                    errorInRound = 0,
                    cumulativeError = 0,
                    hasBagelExxer = false
                )
            )

            var currentRoundIndex = 0
            var cumulativeErr = 0

            for (sip in player.sipsErrors) {
                if (!sip.exxer) {
                    currentRoundIndex++
                    cumulativeErr += sip.amount
                    rounds.add(
                        PlayerRoundData(
                            roundIndex = currentRoundIndex,
                            errorInRound = sip.amount,
                            cumulativeError = cumulativeErr,
                            hasBagelExxer = sip.amount == 0
                        )
                    )
                } else {
                    if (rounds.size > 1) {
                        val last = rounds.removeAt(rounds.size - 1)
                        val updatedErrInRound = last.errorInRound + sip.amount
                        val updatedCumulative = last.cumulativeError + sip.amount
                        rounds.add(
                            PlayerRoundData(
                                roundIndex = last.roundIndex,
                                errorInRound = updatedErrInRound,
                                cumulativeError = updatedCumulative,
                                hasBagelExxer = true
                            )
                        )
                        cumulativeErr = updatedCumulative
                    } else {
                        currentRoundIndex++
                        cumulativeErr += sip.amount
                        rounds.add(
                            PlayerRoundData(
                                roundIndex = currentRoundIndex,
                                errorInRound = sip.amount,
                                cumulativeError = cumulativeErr,
                                hasBagelExxer = true
                            )
                        )
                    }
                }
            }
            PlayerChartSeries(
                player = player,
                color = PlayerChartColors[index % PlayerChartColors.size],
                rounds = rounds
            )
        }
    }

    val maxRounds = remember(seriesList) {
        seriesList.maxOfOrNull { it.rounds.maxOfOrNull { r -> r.roundIndex } ?: 0 } ?: 1
    }

    var currentAnimRound by remember { mutableIntStateOf(1) }
    var isPlaying by remember { mutableStateOf(true) }
    var speedMultiplier by remember { mutableFloatStateOf(1f) }

    // Base delay for 1x speed is 2000ms (2 seconds per round)
    LaunchedEffect(isPlaying, currentAnimRound, maxRounds, speedMultiplier) {
        if (isPlaying && currentAnimRound < maxRounds) {
            val delayMs = (2000L / speedMultiplier).toLong()
            delay(delayMs)
            currentAnimRound++
        } else if (currentAnimRound >= maxRounds) {
            isPlaying = false
        }
    }

    // Animated progress within current round
    val roundProgressAnim = remember { Animatable(0f) }
    LaunchedEffect(currentAnimRound) {
        roundProgressAnim.snapTo(0f)
        roundProgressAnim.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = (800 / speedMultiplier).toInt())
        )
    }

    val maxCumulativeErr = remember(seriesList) {
        val maxErr = seriesList.flatMap { it.rounds }.maxOfOrNull { it.cumulativeError } ?: 10
        max(maxErr, 10)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isLandscape) 4.dp else 8.dp),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            if (isLandscape) {
                // LANDSCAPE MODE: Maximize vertical space for Canvas, put controls on the side, no top title bar
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Chart Canvas takes full vertical height and remaining width
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .padding(8.dp)
                    ) {
                        EvaluationChartCanvas(
                            seriesList = seriesList,
                            currentAnimRound = currentAnimRound,
                            maxRounds = maxRounds,
                            maxCumulativeErr = maxCumulativeErr,
                            roundProgressAnimValue = roundProgressAnim.value
                        )
                    }

                    // Side Controls Column
                    Column(
                        modifier = Modifier
                            .width(170.dp)
                            .fillMaxHeight()
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .padding(8.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Header / Round Counter
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text(
                                text = "Runde $currentAnimRound / $maxRounds",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Playback Control Buttons
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                FilledIconButton(
                                    onClick = {
                                        if (currentAnimRound >= maxRounds) {
                                            currentAnimRound = 1
                                        }
                                        isPlaying = !isPlaying
                                    },
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = if (isPlaying) "Pause" else "Abspielen"
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        currentAnimRound = 1
                                        isPlaying = true
                                    },
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Refresh,
                                        contentDescription = "Neustart"
                                    )
                                }

                                IconButton(
                                    onClick = {
                                        currentAnimRound = maxRounds
                                        isPlaying = false
                                    },
                                    modifier = Modifier.size(38.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.FastForward,
                                        contentDescription = "Zum Ende springen"
                                    )
                                }
                            }

                            // Speed button
                            TextButton(
                                onClick = {
                                    speedMultiplier = when (speedMultiplier) {
                                        1f -> 1.5f
                                        1.5f -> 2f
                                        2f -> 0.5f
                                        else -> 1f
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = "Tempo",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("${speedMultiplier}x", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        // Legend in scrollable side list
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            itemsIndexed(seriesList) { _, series ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .clip(CircleShape)
                                            .background(series.color)
                                    )
                                    Text(
                                        text = series.player.name,
                                        style = MaterialTheme.typography.labelSmall,
                                        maxLines = 1
                                    )
                                }
                            }
                            item {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text("🥯", fontSize = 12.sp)
                                    Text(
                                        text = "= Exx/Bagel",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                        }

                        // Done / Close button
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            Text("Fertig", fontSize = 13.sp)
                        }
                    }
                }
            } else {
                // PORTRAIT MODE: Title at top, chart in center, controls & legend below
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Top Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Bewertung & Rundenverlauf",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Runde $currentAnimRound von $maxRounds",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Schließen"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Chart Area
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(12.dp)
                    ) {
                        EvaluationChartCanvas(
                            seriesList = seriesList,
                            currentAnimRound = currentAnimRound,
                            maxRounds = maxRounds,
                            maxCumulativeErr = maxCumulativeErr,
                            roundProgressAnimValue = roundProgressAnim.value
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Control Bar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilledIconButton(
                                onClick = {
                                    if (currentAnimRound >= maxRounds) {
                                        currentAnimRound = 1
                                    }
                                    isPlaying = !isPlaying
                                }
                            ) {
                                Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isPlaying) "Pause" else "Abspielen"
                                )
                            }

                            IconButton(
                                onClick = {
                                    currentAnimRound = 1
                                    isPlaying = true
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Neustart"
                                )
                            }

                            IconButton(
                                onClick = {
                                    currentAnimRound = maxRounds
                                    isPlaying = false
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FastForward,
                                    contentDescription = "Zum Ende springen"
                                )
                            }

                            TextButton(
                                onClick = {
                                    speedMultiplier = when (speedMultiplier) {
                                        1f -> 1.5f
                                        1.5f -> 2f
                                        2f -> 0.5f
                                        else -> 1f
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Speed,
                                    contentDescription = "Geschwindigkeit",
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("${speedMultiplier}x", fontWeight = FontWeight.Bold)
                            }
                        }

                        Button(onClick = onDismiss) {
                            Text("Fertig")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Legend
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        itemsIndexed(seriesList) { _, series ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(series.color)
                                )
                                Text(
                                    text = series.player.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text("🥯", fontSize = 14.sp)
                                Text(
                                    text = "= Bagel/Exx Schluck",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EvaluationChartCanvas(
    seriesList: List<PlayerChartSeries>,
    currentAnimRound: Int,
    maxRounds: Int,
    maxCumulativeErr: Int,
    roundProgressAnimValue: Float
) {
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    val labelTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)

    Canvas(modifier = Modifier.fillMaxSize()) {
        val paddingLeft = 90f
        val paddingBottom = 50f
        val paddingTop = 30f
        val paddingRight = 40f

        val chartWidth = size.width - paddingLeft - paddingRight
        val chartHeight = size.height - paddingTop - paddingBottom

        // Y-Axis: Negative error (0 at top, -maxCumulativeErr at bottom)
        val stepsY = 5
        for (i in 0..stepsY) {
            val ratio = i.toFloat() / stepsY
            val y = paddingTop + ratio * chartHeight
            val errVal = -(ratio * maxCumulativeErr).toInt()

            drawLine(
                color = gridColor,
                start = Offset(paddingLeft, y),
                end = Offset(size.width - paddingRight, y),
                strokeWidth = 1f
            )

            drawContext.canvas.nativeCanvas.drawText(
                "${errVal}g",
                paddingLeft - 12f,
                y + 10f,
                android.graphics.Paint().apply {
                    color = labelTextColor.hashCode()
                    textSize = 26f
                    textAlign = android.graphics.Paint.Align.RIGHT
                }
            )
        }

        // X-Axis: Rounds (0 to maxRounds)
        val roundCount = max(maxRounds, 1)
        for (r in 0..roundCount) {
            val xRatio = r.toFloat() / roundCount
            val x = paddingLeft + xRatio * chartWidth

            drawLine(
                color = gridColor,
                start = Offset(x, paddingTop),
                end = Offset(x, paddingTop + chartHeight),
                strokeWidth = 1f
            )

            drawContext.canvas.nativeCanvas.drawText(
                "R$r",
                x,
                paddingTop + chartHeight + 38f,
                android.graphics.Paint().apply {
                    color = labelTextColor.hashCode()
                    textSize = 24f
                    textAlign = android.graphics.Paint.Align.CENTER
                }
            )
        }

        // Draw player series
        seriesList.forEach { series ->
            val visibleRounds = series.rounds.filter { it.roundIndex <= currentAnimRound }
            if (visibleRounds.isNotEmpty()) {
                val points = visibleRounds.map { roundData ->
                    val xRatio = roundData.roundIndex.toFloat() / roundCount
                    val x = paddingLeft + xRatio * chartWidth
                    val yRatio = roundData.cumulativeError.toFloat() / maxCumulativeErr
                    val y = paddingTop + yRatio * chartHeight
                    Pair(Offset(x, y), roundData)
                }

                val drawPoints = if (points.size > 1 && roundProgressAnimValue < 1f) {
                    val prev = points[points.size - 2].first
                    val target = points.last().first
                    val interpolated = Offset(
                        prev.x + (target.x - prev.x) * roundProgressAnimValue,
                        prev.y + (target.y - prev.y) * roundProgressAnimValue
                    )
                    points.dropLast(1) + Pair(interpolated, points.last().second)
                } else points

                val path = Path()
                drawPoints.forEachIndexed { idx, (offset, _) ->
                    if (idx == 0) path.moveTo(offset.x, offset.y)
                    else path.lineTo(offset.x, offset.y)
                }

                drawPath(
                    path = path,
                    color = series.color,
                    style = Stroke(width = 4.dp.toPx())
                )

                drawPoints.forEach { (offset, roundData) ->
                    drawCircle(
                        color = series.color,
                        radius = 6.dp.toPx(),
                        center = offset
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 3.dp.toPx(),
                        center = offset
                    )

                    if (roundData.hasBagelExxer) {
                        drawCircle(
                            color = Color(0xFFFF5722),
                            radius = 12.dp.toPx(),
                            center = offset,
                            style = Stroke(width = 2.dp.toPx())
                        )

                        drawContext.canvas.nativeCanvas.drawText(
                            "🥯",
                            offset.x,
                            offset.y - 14.dp.toPx(),
                            android.graphics.Paint().apply {
                                textSize = 32f
                                textAlign = android.graphics.Paint.Align.CENTER
                            }
                        )
                    }
                }

                val lastPoint = drawPoints.last().first
                val lastRoundData = visibleRounds.last()
                val calloutText = "${series.player.name} (-${lastRoundData.cumulativeError}g)"

                drawContext.canvas.nativeCanvas.drawText(
                    calloutText,
                    lastPoint.x + 10f,
                    lastPoint.y + 8f,
                    android.graphics.Paint().apply {
                        color = series.color.hashCode()
                        textSize = 26f
                        isFakeBoldText = true
                    }
                )
            }
        }
    }
}
