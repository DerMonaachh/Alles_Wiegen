package com.example.alleswiegen.screens

import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.alleswiegen.GameUiState
import com.example.alleswiegen.GameViewModel
import kotlin.collections.set
import kotlin.text.toDoubleOrNull
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.sp
import com.example.alleswiegen.components.NfcScannerEffect
import com.example.alleswiegen.components.HapticHelper
import com.example.alleswiegen.components.InitialNFCCommunication
import com.example.alleswiegen.components.InitialWeightTask
import com.example.alleswiegen.components.PlayerInitalWeightCard
import com.example.alleswiegen.components.TutorialHighlightBox

@Composable
fun InitialWeighingScreen(viewModel: GameViewModel, uiState: GameUiState) {


    val weights = uiState.currentWeights
    val liveWeight = viewModel.liveWeight
    val context = LocalContext.current
    val hapticHelper = remember { HapticHelper(context) }
    val lastTag = viewModel.lastScannedTagId
    val configuration = LocalConfiguration.current
    NfcScannerEffect { scannedId ->
        // Strategy: We could assign it to the first player who doesn't have a tag
        // OR provide a UI mechanism to "capture" it for a specific player.
        // For now, let's just toast or log it:
        if (uiState.vibrate){
            hapticHelper.vibrate(50) // Kurz vibrieren
        }
        viewModel.onNfcTagDetected(scannedId)
    }
    val allInputsValid = uiState.players.all { player ->
        val weightStr = weights[player.name] ?: ""
        val weightDouble = weightStr.toIntOrNull()
        // Check: Wert vorhanden && ist eine Zahl && Wert < vorheriges Gewicht
        weightDouble != null
    }&& uiState.players.let { players ->
        // 2. NFC-Check: Alle vergebenen Tags müssen unterschiedlich sein
        // Wir nehmen nur die Tags, die nicht leer sind
        val assignedTags = players.map { it.nfcId }.filter { it.isNotEmpty() }

        // Wenn die Größe der Liste gleich der Größe des Sets (eindeutige Werte) ist,
        // dann sind alle Tags unterschiedlich.
        assignedTags.size == assignedTags.distinct().size
    }

    /*Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        Text("NFC: ${viewModel.lastScannedTagId}")
        Text("Startgewichte (volles Glas)", style = MaterialTheme.typography.headlineSmall)
        if (uiState.isBluetoothConnected)
        {
            Text("Tippe auf den Namen, um '$liveWeight g' zu übernehmen.", style = MaterialTheme.typography.bodyMedium)
        }


        LazyColumn(modifier = Modifier
            .weight(1f)
            .padding(vertical = 16.dp)) {
            items(uiState.players) { player ->
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                    Text(
                        player.name,
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                if (uiState.isBluetoothConnected) {
                                    weights[player.name] = liveWeight
                                    handleNfcAssignment(player.name, viewModel.lastScannedTagId, viewModel, context)
                                }
                            }
                            .padding(8.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    OutlinedTextField(
                        value = weights[player.name] ?: "",
                        onValueChange = { weights[player.name] = it
                            handleNfcAssignment(player.name, viewModel.lastScannedTagId, viewModel, context)},
                        label = { Text("Gramm") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.width(120.dp)
                    )
                }
            }
        }*/
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    val tutorialAnnotatedText = remember {
        buildAnnotatedString {
            append("Hier das Startgewicht der vollen Getränke eintragen (manuell oder per Klick auf das Waagen-Symbol ")
            appendInlineContent("scaleIcon", "[Waage]")
            append(" bei verbundener Waage ggf. vorher den jeweiligen NFC Chips scannen).")
        }
    }
    val tutorialInlineContent = remember {
        mapOf(
            "scaleIcon" to InlineTextContent(
                Placeholder(
                    width = 20.sp,
                    height = 20.sp,
                    placeholderVerticalAlign = PlaceholderVerticalAlign.Center
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Scale,
                    contentDescription = "Waage",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        )
    }

    if (isLandscape) {
        Row(modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)) {
            Column(modifier = Modifier
                .weight(0.3f)
                .padding(16.dp)) {
                InitialWeightTask()
                
                if (uiState.showTutorialOverlay) {
                    TutorialHighlightBox(
                        annotatedText = tutorialAnnotatedText,
                        inlineContent = tutorialInlineContent,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- NFC Status Area ---
                InitialNFCCommunication(lastTag)
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        val weightMap = weights.mapValues { it.value.toIntOrNull() ?: 0 }
                        viewModel.startInitialWeighing(weightMap)

                    },
                    enabled = allInputsValid,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Spiel starten")
                }
            }
            LazyVerticalGrid(columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(2), modifier = Modifier.weight(0.7f),horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(uiState.players){player ->
                    val hasWeight = !weights[player.name].isNullOrEmpty()
                    val hasNfc = player.nfcId.isNotEmpty()
                    PlayerInitalWeightCard(hasWeight, player, hasNfc, uiState, viewModel, liveWeight, weights, context, lastTag)
                }
            }

        }
    }else{


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // --- Header Bereich ---
        InitialWeightTask()

        if (uiState.showTutorialOverlay) {
            TutorialHighlightBox(
                annotatedText = tutorialAnnotatedText,
                inlineContent = tutorialInlineContent,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- NFC Status Area ---
        InitialNFCCommunication(lastTag)

        // --- Spieler Liste ---
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(uiState.players) { player ->
                val hasWeight = !weights[player.name].isNullOrEmpty()
                val hasNfc = player.nfcId.isNotEmpty()
                PlayerInitalWeightCard(hasWeight, player, hasNfc, uiState, viewModel, liveWeight, weights, context, lastTag)
                /*Surface(
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                    tonalElevation = if (hasWeight) 0.dp else 2.dp,
                    border = if (hasWeight) BorderStroke(2.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)) else null,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Spieler Info & NFC Status
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                player.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            if (hasNfc) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Check, "Ready", Modifier.size(14.dp),
                                        Color(0xFF4CAF50)
                                    )
                                    Text(" NFC zugewiesen", style = MaterialTheme.typography.bodySmall, color = Color(0xFF4CAF50))
                                }
                            }
                        }

                        // Gewicht übernehmen Button
                        if (uiState.isBluetoothConnected) {
                            IconButton(
                                onClick = {
                                   // weights[player.name] = liveWeight
                                    viewModel.updateWeight(liveWeight, player.name)
                                    handleNfcAssignment(player.name, lastTag, viewModel, context)
                                },
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            ) {
                                Icon(Icons.Default.Scale, contentDescription = "Wiegen")
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Manuelle Eingabe
                        OutlinedTextField(
                            value = weights[player.name] ?: "",
                            onValueChange = {
                                //weights[player.name] = it
                                viewModel.updateWeight(it, player.name)
                                handleNfcAssignment(player.name, lastTag, viewModel, context)
                            },
                            label = { Text("Gramm") },
                            suffix = { Text("g") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.width(100.dp),
                            singleLine = true,
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
                        )
                    }
                }*/
            }
        }


        Button(
            onClick = {
                val weightMap = weights.mapValues { it.value.toIntOrNull() ?: 0 }
                viewModel.startInitialWeighing(weightMap)

            },
            enabled = allInputsValid,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Spiel starten")
        }
    }
    }
}

fun handleNfcAssignment(
    playerName: String,
    tagId: String?,
    viewModel: GameViewModel,
    context: android.content.Context
) {
    if (!tagId.isNullOrEmpty()) {
        viewModel.assignTagToPlayer(playerName, tagId)
        Toast.makeText(
            context,
            "NFC Tag zu $playerName zugewiesen",
            Toast.LENGTH_SHORT
        ).show()

        // Reset, damit der Tag nicht versehentlich dem nächsten Spieler zugewiesen wird
        viewModel.onNfcTagDetected("")
        viewModel.setNfcUsed(true)
    }
}