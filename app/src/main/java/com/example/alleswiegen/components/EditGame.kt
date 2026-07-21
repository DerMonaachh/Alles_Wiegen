package com.example.alleswiegen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.alleswiegen.GameViewModel
import com.example.alleswiegen.PlayerScoreEntry
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun EditGameScreen(
    timestamp: Long,
    players: List<PlayerScoreEntry>,
    onDismiss: () -> Unit,
    viewModel: GameViewModel
) {
    // Local state to track changes before saving
    var editedPlayers by remember(players) { mutableStateOf(players) }


    val formattedDate = remember(timestamp) {
        SimpleDateFormat("dd. MMMM yyyy, HH:mm", Locale.getDefault()).format(Date(timestamp))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Spiel Details bearbeiten",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Zeitpunkt: $formattedDate",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        HorizontalDivider(modifier = Modifier.padding(bottom = 16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(editedPlayers) { player ->
                PlayerEditCard(
                    player = player,
                    onPlayerChange = { updated ->
                        editedPlayers = editedPlayers.map {
                            if (it.id == updated.id) updated else it
                        }
                    }
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) {
                Text("Abbrechen")
            }
            Button(
                onClick = {
                    editedPlayers.forEach { viewModel.updatePlayerScore(it) }
                    onDismiss()
                },
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Speichern")
            }
        }
    }
}

@Composable
fun PlayerEditCard(
    player: PlayerScoreEntry,
    onPlayerChange: (PlayerScoreEntry) -> Unit
) {
    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Player Name
            OutlinedTextField(
                value = player.name,
                onValueChange = { onPlayerChange(player.copy(name = it)) },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Row 1: General Stats
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                EditField(
                    label = "Bagels",
                    value = player.bagelCount.toString(),
                    modifier = Modifier.weight(0.33f),
                    onValueChange = {
                        onPlayerChange(
                            player.copy(
                                bagelCount = it.toIntOrNull() ?: 0
                            )
                        )
                    }
                )
                // Row 4: Precision
                EditField(
                    label = "Durchschnittlicher Fehler",
                    value = player.averageError.toString(),
                    modifier = Modifier.weight(0.33f),
                    isDecimal = true,
                    onValueChange = {
                        onPlayerChange(
                            player.copy(
                                averageError = it.toDoubleOrNull() ?: 0.0
                            )
                        )
                    }
                )
                EditField( //TODO add auto update for all SPieler anzahl
                    label = "Spieler Anzahl",
                    value = player.playerCount.toString(),
                    modifier = Modifier.weight(0.33f),
                    isDecimal = true,
                    onValueChange = {
                        onPlayerChange(
                            player.copy(
                                playerCount = it.toIntOrNull() ?: 0
                            )
                        )
                    }
                )

            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "Klassisch",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary
            )

            // Row 2: Classic Stats
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                EditField(
                    label = "Fehler",
                    value = player.totalErrorClassic.toString(),
                    modifier = Modifier.weight(1f),
                    onValueChange = {
                        onPlayerChange(
                            player.copy(
                                totalErrorClassic = it.toIntOrNull() ?: 0
                            )
                        )
                    }
                )
                EditField(
                    label = "Sips (Total)",
                    value = player.sipCount.toString(),
                    modifier = Modifier.weight(1f),
                    onValueChange = {
                        onPlayerChange(
                            player.copy(
                                sipCount = it.toIntOrNull() ?: 0
                            )
                        )
                    }
                )
                EditField(
                    label = "Platz",
                    value = player.classicPosition.toString(),
                    modifier = Modifier.weight(1f),
                    onValueChange = {
                        onPlayerChange(
                            player.copy(
                                classicPosition = it.toIntOrNull() ?: 0
                            )
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "Ohne Exen",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary
            )

            // Row 3: Without Exercise Stats
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                EditField(
                    label = "Fehler",
                    value = player.totalErrorWithoutExer.toString(),
                    modifier = Modifier.weight(1f),
                    onValueChange = {
                        onPlayerChange(
                            player.copy(
                                totalErrorWithoutExer = it.toIntOrNull() ?: 0
                            )
                        )
                    }
                )
                EditField(
                    label = "Sips",
                    value = player.sipCountWithoutExer.toString(),
                    modifier = Modifier.weight(1f),
                    onValueChange = {
                        onPlayerChange(
                            player.copy(
                                sipCountWithoutExer = it.toIntOrNull() ?: 0
                            )
                        )
                    }
                )
                EditField(
                    label = "Platz",
                    value = player.withoutExerPosition.toString(),
                    modifier = Modifier.weight(1f),
                    onValueChange = {
                        onPlayerChange(
                            player.copy(
                                withoutExerPosition = it.toIntOrNull() ?: 0
                            )
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))


        }
    }
}

@Composable
fun EditField(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    isDecimal: Boolean = false,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
        modifier = modifier,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (isDecimal) KeyboardType.Decimal else KeyboardType.Number
        ),
        singleLine = true
    )
}