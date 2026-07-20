package com.example.alleswiegen.components

import android.R
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.alleswiegen.GameUiState
import com.example.alleswiegen.GameViewModel
import com.example.alleswiegen.Player
import com.example.alleswiegen.screens.handleNfcAssignment

@Composable
fun InitialWeightTask(){
    Text(
        text = "Startgewichte",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold
    )
    Text(
        text = "Stelle ein volles Glas auf die Waage und weise es einem Spieler zu.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
fun InitialNFCCommunication(lastTag: String?){
    Surface(
        color = if (!lastTag.isNullOrEmpty()) MaterialTheme.colorScheme.primaryContainer
        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Nfc,
                contentDescription = null,
                tint = if (!lastTag.isNullOrEmpty()) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = if (!lastTag.isNullOrEmpty()) "NFC Tag bereit" else "Kein Tag gescannt",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                if (!lastTag.isNullOrEmpty()) {
                    Text(
                        text = "ID: $lastTag",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

@Composable
fun PlayerInitalWeightCard(hasWeight: Boolean,player: Player,hasNfc: Boolean,uiState: GameUiState,viewModel: GameViewModel,liveWeight: String,weights: Map<String, String>,context: android.content.Context,lastTag: String?){
    Surface(
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
    }
}