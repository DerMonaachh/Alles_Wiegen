package com.example.alleswiegen.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp // WICHTIG für .sp
import com.example.alleswiegen.GameViewModel
import com.example.alleswiegen.PlayerRank
import com.example.alleswiegen.PlayerScoreEntry
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.line.lineChart
import com.patrykandpatrick.vico.compose.chart.line.lineSpec

import com.patrykandpatrick.vico.compose.component.shapeComponent
import com.patrykandpatrick.vico.compose.component.textComponent
import com.patrykandpatrick.vico.compose.dimensions.dimensionsOf
import com.patrykandpatrick.vico.compose.legend.horizontalLegend
import com.patrykandpatrick.vico.compose.legend.legendItem
import com.patrykandpatrick.vico.core.chart.DefaultPointConnector
import com.patrykandpatrick.vico.core.entry.ChartEntryModelProducer
import com.patrykandpatrick.vico.core.entry.entryOf
import com.patrykandpatrick.vico.core.component.shape.Shapes as VicoShapes

@Composable
fun AdvancedPlayerChart(playerRanks: List<PlayerRank>,method: String) {
    val playerNames = playerRanks.map { it.name }.distinct()
    val playerColors = listOf(
        Color(0xFF2196F3), Color(0xFFF44336), Color(0xFF4CAF50),
        Color(0xFFFFEB3B), Color(0xFF9C27B0), Color(0xFF00BCD4),
        Color(0xFFFF9800), Color(0xFF795548), Color(0xFF9E9E9E),
        Color(0xFF607D8B), Color(0xFFE91E63), Color(0xFF3F51B5)
    )

    val textColor = MaterialTheme.colorScheme.onSurface

    // --- LEGENDE ---
    val legend = horizontalLegend(
        items = playerNames.mapIndexed { index, name ->
            val color = playerColors[index % playerColors.size]
            legendItem(
                icon = shapeComponent(shape = VicoShapes.pillShape, color = color),
                label = textComponent(
                    color = textColor,
                    textSize = 12.sp
                ),
                labelText = name
            )
        },
        iconSize = 8.dp,
        iconPadding = 4.dp,
        spacing = 12.dp,
        padding = dimensionsOf(top = 16.dp)
    )

    Chart(
        chart = lineChart(
            lines = playerColors.map { color ->
                lineSpec(
                    lineColor = color,
                    lineThickness = 2.dp,
                    lineBackgroundShader = null,
                    // Fix: Wir verwenden direkt das shapeComponent als point
                    point = shapeComponent(
                        shape = VicoShapes.pillShape,
                        color = color,
                        strokeWidth = 1.dp,
                        strokeColor = Color.White
                    ),
                    pointSize = 6.dp, // Hier wird die Größe definiert
                    pointConnector = DefaultPointConnector(cubicStrength = 0f)
                )
            }
        ),
        chartModelProducer = remember(playerRanks, method) {
            ChartEntryModelProducer(
                playerNames.map { name ->
                    playerRanks.filter { it.name == name }
                        .sortedBy { it.number }
                        .map { rank ->
                            // Hier wird der Y-Wert dynamisch basierend auf 'method' gewählt
                            val yValue = when (method) {
                                "Klassische Wertung" -> rank.pointsByErrorClassic.toFloat()
                                "Ohne Bagel" -> rank.pointsByErrorNoExxer.toFloat()
                                "Durschnittlicher Fehler" -> rank.averageError.toFloat()
                                "Bagel Quote" -> rank.bagelRate.toFloat()
                                else -> rank.pointsByErrorClassic.toFloat()
                            }
                            entryOf(rank.number.toFloat(), yValue)
                        }
                }
            )
        },
        legend = legend,
        startAxis = rememberStartAxis(
            label = textComponent(color = textColor)
        ),
        bottomAxis = rememberBottomAxis(
            label = textComponent(color = textColor),
            valueFormatter = { value, _ -> "${value.toInt() + 1}" }
        )
    )
}
@Composable
fun transformScoresToPlayerRanks(viewModel: GameViewModel, gameMode: String = "Normal"): List<PlayerRank> {
    // 1. Die Scores aus dem ViewModel beobachten
    val selectedScores by viewModel.filteredScores.collectAsState(initial = emptyList())

    // Filter by gameMode (extensible mapping)
    val modeInt = when (gameMode) {
        "Normal" -> 0
        "Items" -> 1
        else -> 0 // Default to Normal if mode is unknown
    }

    val scoresFilteredByMode = selectedScores.filter { it.gameMode == modeInt }

    // 2. Alle existierenden "Zeit-Indizes" (Int) finden und sortieren.
    // Das stellt sicher, dass wenn Spiel A (Int 10) und Spiel B (Int 25) existieren,
    // diese auf Index 0 und 1 gemappt werden.
    val distinctTime = scoresFilteredByMode
        .map { it.timestamp } // Das ist dein Int-Wert aus der DB
        .distinct()
        .sorted()

    // 3. Mapping: Zeit-Int -> Chart-X-Achse (0, 1, 2...)
    val timeToXAxisMap = distinctTime.withIndex()
        .associate { it.value to it.index.toLong() }

    // 4. Gruppieren nach Spielername
    return scoresFilteredByMode.groupBy { it.name }
        .flatMap { (playerName, scores) ->
            // Innerhalb des Spielers chronologisch sortieren
            val playerSortedScores = scores.sortedBy { it.timestamp }

            var runningBagelCount = 0
            var runningGameCount=0
            var runningSipCount=0
            var runningClassicPoints=0
            var runningExxerPoints=0
            var runningAverage=0.0


            playerSortedScores.map { score ->
                // Bagels addieren
                runningBagelCount += score.bagelCount
                runningGameCount+=1

                val newPoints=calculateGenericPoints(score)
                println(score.name)
                println(newPoints)
                runningClassicPoints+=newPoints.first
                runningExxerPoints+=newPoints.second

                val totalSips = runningSipCount + score.sipCount
                val average = if (totalSips > 0) {
                    (runningAverage * runningSipCount + score.averageError * score.sipCount) / totalSips.toDouble()
                } else 0.0
                runningAverage = average
                runningSipCount += score.sipCount

                val bagelRate = if (runningGameCount > 0) {
                    runningBagelCount.toDouble() / runningGameCount.toDouble()
                } else 0.0

                // Den globalen X-Achsen Index bestimmen
                val xAxisValue = timeToXAxisMap[score.timestamp] ?: 0L

                PlayerRank(
                    name = playerName,
                    number= xAxisValue, // Der gemappte Index (0, 1, 2...)

                    averageError = average,
                    bagelRate = bagelRate,
                    bagels = runningBagelCount,
                    // Deine generische Punkte-Logik
                    pointsByErrorClassic = runningClassicPoints,
                    pointsByErrorNoExxer = runningExxerPoints,
                    time = score.timestamp,
                )
            }
        }
        .sortedBy { it.time }
}
@Composable
fun getFinalPlayerRanks(allRanks :List<PlayerRank>): List<PlayerRank> {


    return allRanks
        .groupBy { it.name }
        .map { (_, ranks) ->
            // Da die Liste in transformScoresToPlayerRanks bereits nach Zeit sortiert wird,
            // ist der letzte Eintrag pro Gruppe der aktuellste.
            ranks.last()
        }
        .sortedByDescending { it.pointsByErrorClassic } // Optional: Direkt nach Punkten sortieren
}
// Deine Platzhalter-Funktion
fun calculateGenericPoints(score: PlayerScoreEntry): Pair<Int,Int> {
    val errorpoints =when {
        score.averageError <4.0 -> 3
        score.averageError <=6 -> 2
        score.averageError <=10 -> 1
        score.averageError <=20 -> 0
        score.averageError <=30 -> -1
        else -> -2
    }
    val classicPoints=(score.playerCount-score.classicPosition)-(score.playerCount/2)+1+errorpoints
    val exxerPoints=(score.playerCount-score.withoutExerPosition)-(score.playerCount/2)+1+errorpoints
    return Pair(classicPoints,exxerPoints)
}

