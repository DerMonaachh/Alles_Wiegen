package com.example.alleswiegen.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material3.ButtonDefaults
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
import com.example.alleswiegen.components.ServerQrDialog


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupScreen(viewModel: GameViewModel, uiState: GameUiState) {
    var nameInput by remember { mutableStateOf("") }
    var showPreviousPlayers by remember { mutableStateOf(false) }
    var expandedGameMode by remember { mutableStateOf(false) }
    var expandedItemDistro by remember { mutableStateOf(false) }
    var showGameSettings by remember { mutableStateOf(false) }
    var showHexCode by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        // Bluetooth Status Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = {},
                    onDoubleClick = {
                        if (uiState.isBluetoothConnected) {
                            showHexCode = !showHexCode
                        }
                    }
                ),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (uiState.isBluetoothConnected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (uiState.isBluetoothConnected) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (uiState.isBluetoothConnected) "Waage Verbunden ✅" else "Waage Nicht Verbunden ❌",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (uiState.isBluetoothConnected) {
                        Spacer(Modifier.height(2.dp))
                        Text("Aktueller Wert: ${viewModel.liveWeight} g", style = MaterialTheme.typography.bodyLarge)
                        if (showHexCode) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = "Hex: ${viewModel.rawDataString.ifEmpty { "Keine Daten" }}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    } else {
                        Spacer(Modifier.height(2.dp))
                        Text("Bitte Waage koppeln", style = MaterialTheme.typography.bodySmall)
                    }
                }
                Button(
                    onClick = {
                        viewModel.setupBluetooth(context)
                        viewModel.connectBluetooth()
                    },
                    modifier = Modifier.width(140.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        //contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Text(if (uiState.isBluetoothConnected) "Neu verbinden" else "Verbinden", maxLines = 1)
                }
            }
        }

        Spacer(Modifier.height(10.dp))

        // Web Server Status Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (uiState.isServerRunning) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (uiState.isServerRunning) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (uiState.isServerRunning) "Web-Server Aktiv 🌐" else "Web-Server Inaktiv 🌐",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = if (uiState.isServerRunning) "Spieler können beitreten" else "Tippe auf Server erstellen",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Button(
                    onClick = {
                        if (uiState.isServerRunning) {
                            viewModel.showServerQrDialog()
                        } else {
                            viewModel.startServer(context)
                        }
                    },
                    modifier = Modifier.width(140.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        //contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Text(if (uiState.isServerRunning) "QR Code" else "Erstellen", maxLines = 1)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Game Mode Selection & Settings Toggle Row
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
                    modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true).fillMaxWidth(),
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
                modifier = Modifier.height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Icon(Icons.Default.Settings, contentDescription = "Einstellungen")
            }
        }

        // Animated Game Settings Expandable Card
        AnimatedVisibility(visible = showGameSettings) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Spiel-Einstellungen", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { viewModel.toggleExenByNull(!uiState.exenByNull) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Bagel bei 0",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Switch(
                            checked = uiState.exenByNull,
                            onCheckedChange = null,
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
                        Spacer(Modifier.height(8.dp))
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

                        Box(modifier = Modifier.heightIn(max = 180.dp)) {
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

        Spacer(Modifier.height(12.dp))

        // Players Card Section
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.background
            ),
            border = BorderStroke(1.dp, Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SPIELER (${uiState.players.size})",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    TextButton(onClick = { showPreviousPlayers = true }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.size(4.dp))
                        Text("Letzte laden", style = MaterialTheme.typography.labelLarge)
                    }
                }

                Spacer(Modifier.height(4.dp))

                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Neuer Spieler Name") },
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

                if (uiState.players.isEmpty()) {
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
                            Surface(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(12.dp),
                                tonalElevation = 2.dp,
                                border = BorderStroke(1.dp, Color.White)
                            ) {
                                ListItem(
                                    headlineContent = {
                                        Text(player.name, fontWeight = FontWeight.Medium)
                                    },
                                    leadingContent = {
                                        Surface(
                                            modifier = Modifier.size(36.dp),
                                            color = MaterialTheme.colorScheme.primaryContainer,
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(
                                                    player.name.take(1).uppercase(),
                                                    style = MaterialTheme.typography.titleMedium,
                                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    },
                                    trailingContent = {
                                        IconButton(onClick = { viewModel.removePlayer(player.name) }) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = "Löschen",
                                                tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f),
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

        Spacer(Modifier.height(16.dp))

        // Bottom Navigation Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { viewModel.navigateTo(Screen.OPTIONS) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    //contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Text("Optionen")
            }
            Button(
                onClick = { viewModel.navigateTo(Screen.EVENT) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    //contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Text("Ergebnisse")
            }
            Button(
                onClick = { viewModel.navigateTo(Screen.INITIAL_WEIGHING) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                enabled = uiState.players.size >= 2,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Wiegen!", fontWeight = FontWeight.Bold)
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
                            onClick = {
                                viewModel.addPlayer(name)
                            },
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

    if (uiState.showWifiErrorDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissWifiErrorDialog() },
            title = { Text("Kein WLAN oder Hotspot") },
            text = { Text("Das Smartphone ist weder mit einem WLAN verbunden noch ist ein eigener Hotspot aktiv. Bitte verbinde dich mit einem WLAN oder aktiviere den Hotspot, um den Server zu starten.") },
            confirmButton = {
                TextButton(onClick = { viewModel.dismissWifiErrorDialog() }) {
                    Text("OK")
                }
            }
        )
    }

    if (uiState.showServerQrDialog && uiState.isServerRunning) {
        ServerQrDialog(
            serverUrl = uiState.serverIpUrl,
            onDismiss = { viewModel.dismissServerQrDialog() },
            onStopServer = { viewModel.stopServer() }
        )
    }
}
