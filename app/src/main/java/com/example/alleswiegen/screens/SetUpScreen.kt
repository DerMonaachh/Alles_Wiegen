package com.example.alleswiegen.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MenuAnchorType
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.alleswiegen.AvailableItems
import com.example.alleswiegen.GameMode
import com.example.alleswiegen.GameUiState
import com.example.alleswiegen.GameViewModel
import com.example.alleswiegen.ItemDistro
import com.example.alleswiegen.Screen

import android.media.AudioAttributes

import android.os.VibratorManager
import androidx.compose.ui.modifier.modifierLocalConsumer


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(viewModel: GameViewModel, uiState: GameUiState) {
    var nameInput by remember { mutableStateOf("") }
    var showPreviousPlayers by remember { mutableStateOf(false) }
    var showBluetoothSettings by remember { mutableStateOf(false) }
    var expandedGameMode by remember { mutableStateOf(false) }
    var expandedItemDistro by remember { mutableStateOf(false) }
    var showGameSettings by remember { mutableStateOf(false) }
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

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ExposedDropdownMenuBox(
                expanded = expandedGameMode,
                onExpandedChange = { expandedGameMode = !expandedGameMode },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = uiState.gameMode.displayName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Spielmodus") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedGameMode) },
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true),
                    shape = RoundedCornerShape(12.dp)
                )
                ExposedDropdownMenu(
                    expanded = expandedGameMode,
                    onDismissRequest = { expandedGameMode = false }
                ) {
                    GameMode.entries.forEach { mode ->
                        DropdownMenuItem(
                            text = { Text(mode.displayName) },
                            onClick = {
                                viewModel.updateGameMode(mode)
                                expandedGameMode = false
                            }
                        )
                    }
                }
            }

            Button(
                onClick = { showGameSettings = !showGameSettings },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.height(56.dp)
            ) {
                Icon(Icons.Default.Settings, contentDescription = "Einstellungen")
            }
        }

        AnimatedVisibility(visible = showGameSettings) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Spiel-Einstellungen", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp)) // Clip für den Ripple-Effekt
                            .clickable { viewModel.toggleExenByNull(!uiState.exenByNull) } // Ganze Zeile klickbar
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Exen bei 0",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Oder seid ihr Muschis?",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Der Switch
                        Switch(
                            checked = uiState.exenByNull,
                            onCheckedChange = null, // Auf null, da clickable in der Row das übernimmt
                            thumbContent = if (uiState.exenByNull) {
                                {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = null,
                                        modifier = Modifier.size(SwitchDefaults.IconSize),
                                    )
                                }
                            } else null
                        )
                    }

                    if (uiState.gameMode == GameMode.Items) {
                        ExposedDropdownMenuBox(
                            expanded = expandedItemDistro,
                            onExpandedChange = { expandedItemDistro = !expandedItemDistro },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = uiState.itemVerteilung,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Item-Verteilung") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedItemDistro) },
                                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true).fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = expandedItemDistro,
                                onDismissRequest = { expandedItemDistro = false }
                            ) {
                                ItemDistro.entries.forEach { distro ->
                                    DropdownMenuItem(
                                        text = { Text(distro.displayName) },
                                        onClick = {
                                            viewModel.updateItemVerteilung(distro.displayName)
                                            expandedItemDistro = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        OutlinedTextField(
                            value = uiState.itemsProRunde.toString(),
                            onValueChange = { newValue ->
                                newValue.toIntOrNull()?.let { viewModel.updateItemsProRunde(it) }
                            },
                            label = { Text("Items pro Runde") },
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(Modifier.height(12.dp))

                        Text("Erlaubte Items", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                        Spacer(Modifier.height(4.dp))

                        Box(modifier = Modifier.heightIn(max = 200.dp)) {
                            LazyColumn {
                                items(uiState.getEffectiveItems()) { item ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { viewModel.toggleErlaubteItem(item.id) }
                                            .padding(vertical = 4.dp)
                                    ) {
                                        Checkbox(
                                            checked = uiState.erlaubteItems.contains(item.id),
                                            onCheckedChange = { viewModel.toggleErlaubteItem(item.id) }
                                        )
                                        Text(item.name, style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                            }
                        }
                    } else {
                        Text(
                            "Keine spezifischen Einstellungen für ${uiState.gameMode.displayName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
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


        Row(horizontalArrangement = Arrangement.SpaceBetween) {
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
            Spacer(modifier = Modifier.width(2.dp))
            Button(
                onClick = { viewModel.navigateTo(Screen.EVENT) },
                modifier = Modifier.weight(0.33f),

                ) {
                Text("Ergebnisse")
            }
            Spacer(modifier = Modifier.width(2.dp))
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
