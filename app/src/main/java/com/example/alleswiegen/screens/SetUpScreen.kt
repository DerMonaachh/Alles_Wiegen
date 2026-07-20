package com.example.alleswiegen.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.alleswiegen.GameUiState
import com.example.alleswiegen.GameViewModel
import com.example.alleswiegen.Screen

import android.media.AudioAttributes

import android.os.VibratorManager


@Composable
fun SetupScreen(viewModel: GameViewModel, uiState: GameUiState) {
    var nameInput by remember { mutableStateOf("") }
    var showPreviousPlayers by remember { mutableStateOf(false) }
    var showBluetoothSettings by remember { mutableStateOf(false) }
    val context = LocalContext.current






    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (uiState.isBluetoothConnected) Color(0xFF2E7D32) else Color(0xFFB71C1C),
                contentColor = Color.White
            )
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (uiState.isBluetoothConnected) "Verbunden ✅" else "Nicht verbunden ❌",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (uiState.isBluetoothConnected) {
                        Text("Aktueller Wert: ${viewModel.liveWeight} g", style = MaterialTheme.typography.bodyLarge)
                    } else {
                        Text("Bitte Waage einschalten, MAC Adresse eingeben und koppeln", style = MaterialTheme.typography.bodySmall)
                    }
                }
                Button(onClick = { viewModel.setupBluetooth(context)
                    viewModel.connectBluetooth() }) {
                    Text(if (uiState.isBluetoothConnected) "Neu verbinden" else "Verbinden")
                }
            }

        }




        // Sektion: Spielregeln / Optionen


        /*Text("Spieler Setup", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = nameInput,
                onValueChange = { nameInput = it },
                label = { Text("Name") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            IconButton(onClick = { if (nameInput.isNotBlank()) { viewModel.addPlayer(nameInput); nameInput = "" } }) {
                Icon(Icons.Default.Add, contentDescription = "Zufügen")
            }
            IconButton(onClick = { showPreviousPlayers = true }) {
                Icon(Icons.Default.Refresh, contentDescription = "Letzte Spieler")
            }
        }

        LazyColumn(modifier = Modifier
            .weight(1f)
            .padding(vertical = 8.dp)) {
            items(uiState.players) { player ->
                ListItem(
                    headlineContent = { Text(player.name) },
                    trailingContent = {
                        IconButton(onClick = { viewModel.removePlayer(player.name) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Löschen")
                        }
                    }
                )
            }
        }*/
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f), // Nimmt den restlichen Platz ein
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Überschrift mit Spieleranzahl
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SPIELER (${uiState.players.size})",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    // Button für letzte Spieler (History)
                    TextButton(onClick = { showPreviousPlayers = true }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp) // Hier wird die Größe richtig gesetzt
                        )
                        Spacer(Modifier.size(4.dp))
                        Text("Letzte laden", style = MaterialTheme.typography.labelLarge)
                    }
                }

                // Eingabefeld
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Neuer Spieler") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    trailingIcon = {
                        if (nameInput.isNotBlank()) {
                            IconButton(onClick = {
                                viewModel.addPlayer(nameInput)
                                nameInput = ""
                            }) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Zufügen",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                )

                Spacer(Modifier.height(12.dp))

                // Spielerliste
                if (uiState.players.isEmpty()) {
                    // Empty State
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "Noch keine Spieler hinzugefügt",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.players) { player ->
                            // Stylischerer Listeneintrag
                            Surface(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(12.dp),
                                tonalElevation = 2.dp
                            ) {
                                ListItem(
                                    headlineContent = {
                                        Text(player.name, fontWeight = FontWeight.Medium)
                                    },
                                    leadingContent = {
                                        // Ein kleiner Avatar oder Kreis mit Initialen wirkt Wunder
                                        Surface(
                                            modifier = Modifier.size(32.dp),
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    player.name.take(1).uppercase(),
                                                    style = MaterialTheme.typography.labelLarge,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                                )
                                            }
                                        }
                                    },
                                    trailingContent = {
                                        IconButton(onClick = { viewModel.removePlayer(player.name) }) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Löschen",
                                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }


        Row() {
            Button(
                onClick = {

                    viewModel.navigateTo(Screen.OPTIONS)
                    // val vibrationEffect = VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE)

                    // HIER DIE ÄNDERUNG: Wir übergeben die audioAttributes mit!
                    // vibrator.vibrate(vibrationEffect, audioAttributes)
                    //viewModel.navigateTo(Screen.OPTIONS)
                },
                modifier = Modifier.weight(0.33f),

                ) {
                Text("Optionen")
            }
            Button(
                onClick = { viewModel.navigateTo(Screen.EVENT) },
                modifier = Modifier.weight(0.33f),

                ) {
                Text("Ergebnisse")
            }
            Button(
                onClick = { viewModel.navigateTo(Screen.INITIAL_WEIGHING) },
                modifier = Modifier.weight(0.33f),
                enabled = uiState.players.size >= 2
            ) {
                Text("Wiegen!")
            }




        }

    }

    if (showPreviousPlayers) {
        AlertDialog(
            onDismissRequest = { showPreviousPlayers = false },
            title = { Text("Letzte Spieler laden") },
            text = {
                Column {
                    uiState.previousPlayers.forEach { name ->
                        TextButton(
                            onClick = { viewModel.addPlayer(name) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(name, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showPreviousPlayers = false }) { Text("Fertig") } }
        )
    }
}
