package com.example.alleswiegen.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.alleswiegen.GameUiState
import com.example.alleswiegen.GameViewModel
import com.example.alleswiegen.Player

@Composable
fun ItemHandling(viewModel: GameViewModel, uiState: GameUiState) {
    var selectedPlayerName by remember { mutableStateOf<String?>(null) }
    var targetMode by remember { mutableStateOf<Boolean>(false) }

    val selectedPlayer = uiState.players.find { it.name == selectedPlayerName }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Info Bereich
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = if (selectedPlayer != null) "Item Info" else "Information",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = selectedPlayer?.items?.firstOrNull()?.buildBeschreibung() ?: uiState.info,
                    style = MaterialTheme.typography.bodyLarge,
                    minLines = 2
                )
                if (targetMode){
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Wähle dein Opfer!",
                        style = MaterialTheme.typography.bodyLarge,

                        )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val usedItem = selectedPlayer?.items?.firstOrNull()

                            if (usedItem?.target == true) {
                                targetMode = true
                            } else {
                                selectedPlayerName?.let { viewModel.handleItem(it) }
                                selectedPlayerName = null
                                targetMode = false
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = selectedPlayer != null && !targetMode
                    ) {
                        Text("Item einsetzen")
                    }
                    Button(
                        onClick = {
                            selectedPlayerName = null
                            targetMode = false
                        },
                        modifier = Modifier.weight(1f),
                        enabled = selectedPlayerName != null,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) {
                        Text("Abbrechen")
                    }
                }
            }
        }

        // Spieler Liste
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            items(uiState.players) { player ->
                PlayerItemCard(
                    player = player,
                    isSelected = selectedPlayerName == player.name,
                    isTargetMode = targetMode,
                    hidePlacement = uiState.hidePlacement,
                    onClick = {
                        if (targetMode) {
                            selectedPlayerName?.let { trigger ->
                                viewModel.handleItem(trigger, player.name)
                            }
                            selectedPlayerName = null
                            targetMode = false
                        } else {
                            if (player.items.isNotEmpty()) {
                                selectedPlayerName = if (selectedPlayerName == player.name) null else player.name
                            }
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { viewModel.GoToNextTarget() },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Weiter", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PlayerItemCard(
    player: Player,
    isSelected: Boolean,
    isTargetMode: Boolean,
    hidePlacement: Boolean = false,
    onClick: () -> Unit
) {
    val hasItem = player.items.isNotEmpty()
    val isClickable = hasItem || isTargetMode
    
    val containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (isClickable) 1f else 0.5f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(enabled = isClickable) { onClick() },
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 6.dp else 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Item Bild
            if (hasItem) {
                val item = player.items.first()
                Image(
                    painter = painterResource(id = item.imageRes),
                    contentDescription = item.name,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
            } else {
                Spacer(modifier = Modifier.size(48.dp))
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                val headerText = if (hidePlacement) {
                    player.name
                } else {
                    "${player.position}. ${player.name} Fehler: ${player.totalError}"
                }
                Text(
                    text = headerText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                if (hasItem) {//TODO multiple Items
                    Text(
                        text = player.items.first().name,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "Kein Item",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            if (isSelected) {
                Text(
                    text = "Ausgewählt",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
