package com.example.alleswiegen.screens

import android.content.res.Configuration
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

import com.example.alleswiegen.GameUiState
import com.example.alleswiegen.GameViewModel
import com.example.alleswiegen.PlayerScoreEntry
import com.example.alleswiegen.getRankOf
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import androidx.core.graphics.set
import androidx.core.graphics.createBitmap
import com.example.alleswiegen.components.QrCodeDialog

// Enum für die verschiedenen Ranking-Modi
enum class RankingMode(val title: String) {
    Gesamt("Klassisch"),
    OhneExxer("Ohne Bagel"),
    OhneWorst("Ohne max. Fehler"),
    Profi("Ohne Bagel ohne max. Fehler")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RankingScreen(viewModel: GameViewModel, uiState: GameUiState) {
    var selectedMode by remember { mutableStateOf(RankingMode.Gesamt) }
    val context = LocalContext.current
    var showQrDialog by remember { mutableStateOf(false) }
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    // Berechnungen der verschiedenen Listen
    val displayList = when (selectedMode) {
        RankingMode.Gesamt -> {
            uiState.players.sortedBy { it.totalError }.map { Triple(it, it.totalError, 0) }
        }

        RankingMode.OhneWorst -> {
            uiState.players.map { player ->
                val worstSip = player.sipsErrors.maxOfOrNull { it.amount } ?: 0
                Triple(player, (player.totalError - worstSip).coerceAtLeast(0), worstSip)
            }.sortedBy { it.second }
        }

        RankingMode.OhneExxer -> {
            uiState.players.map { player ->
                val regularErrors = player.sipsErrors.filter { !it.exxer }.sumOf { it.amount }
                Triple(player, regularErrors, 0)
            }.sortedBy { it.second }
        }

        RankingMode.Profi -> {
            uiState.players.map { player ->
                val regularSips = player.sipsErrors.filter { !it.exxer }
                val sum = regularSips.sumOf { it.amount }
                val worst = regularSips.maxOfOrNull { it.amount } ?: 0
                Triple(player, (sum - worst).coerceAtLeast(0), worst)
            }.sortedBy { it.second }
        }
    }


    Column(modifier = Modifier.fillMaxSize()) {
        // Tab-Leiste oben
        PrimaryTabRow(selectedTabIndex = selectedMode.ordinal) {
            RankingMode.entries.forEach { mode ->
                Tab(
                    selected = selectedMode == mode,
                    onClick = { selectedMode = mode },
                    text = { Text(mode.title, style = MaterialTheme.typography.labelSmall) }
                )
            }
        }

        Column(modifier = Modifier
            .padding(16.dp)
            .weight(1f)) {
            // Header-Info
            Text(
                text = when (selectedMode) {
                    RankingMode.Gesamt -> "Klassische Wertung aller Schlucke"
                    RankingMode.OhneExxer -> "Fehler durch Bagel werden ignoriert"
                    RankingMode.OhneWorst -> "Der schlechteste Schluck wird abgezogen (kann auch von Bagel sein)"
                    RankingMode.Profi -> "Fehler durch Bagel werden ignoriert & schlechtester regulärer Schluck ignoriert."
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(Modifier.height(12.dp))

            if (isLandscape){
                LazyVerticalGrid(columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(3), modifier = Modifier.weight(0.7f),horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    items(displayList.withIndex().toList()) {  (index, data) ->
                        val (player, score, detail) = data
                        ListItem(
                            headlineContent = { Text("${index + 1}. ${player.name}") },
                            supportingContent = {
                                val detailText =
                                    if (detail > 0) " (Ignoriert: ${detail}g)" else ""
                                Text("Fehler: ${score}g$detailText")
                            },
                            trailingContent = {
                                if (index == 0) Text(
                                    "🏆",
                                    style = MaterialTheme.typography.headlineSmall
                                )
                            },
                            colors = ListItemDefaults.colors(
                                containerColor = if (index == 0) MaterialTheme.colorScheme.primaryContainer.copy(
                                    alpha = 0.3f
                                ) else MaterialTheme.colorScheme.surface
                            )
                        )
                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant
                        )}}
            }
            else{
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(displayList.withIndex().toList()) { (index, data) ->
                            val (player, score, detail) = data
                            ListItem(
                                headlineContent = { Text("${index + 1}. ${player.name}") },
                                supportingContent = {
                                    val detailText =
                                        if (detail > 0) " (Ignoriert: ${detail}g)" else ""
                                    Text("Fehler: ${score}g$detailText")
                                },
                                trailingContent = {
                                    if (index == 0) Text(
                                        "🏆",
                                        style = MaterialTheme.typography.headlineSmall
                                    )
                                },
                                colors = ListItemDefaults.colors(
                                    containerColor = if (index == 0) MaterialTheme.colorScheme.primaryContainer.copy(
                                        alpha = 0.3f
                                    ) else MaterialTheme.colorScheme.surface
                                )
                            )
                            HorizontalDivider(
                                thickness = 0.5.dp,
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                        }
                    }
            }


        }

        // Button unten fixiert
        Box(modifier = Modifier.padding(16.dp)) {
            Row() { OutlinedButton(
                onClick = { viewModel.resetGame(context) },
                modifier = Modifier.weight(0.5f)
            ) {
                Text("Hauptmenü")
            }
                OutlinedButton(
                    onClick = { showQrDialog=true },
                    modifier = Modifier.weight(0.5f)
                ) {
                    Text("CR Code")
                }
            }

        }
    }
    if (showQrDialog) {
        QrCodeDialog(csvData = viewModel.generateCsvForQr(), onDismiss = { showQrDialog = false })
    }
}

