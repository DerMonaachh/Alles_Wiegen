package com.example.alleswiegen.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Nfc
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.alleswiegen.AvailableItems
import com.example.alleswiegen.GameUiState
import com.example.alleswiegen.GameViewModel
import com.example.alleswiegen.Player
@Composable
fun WeiterOderEnde(viewModel: GameViewModel,currentWeights: Map<String, String>, allInputsValid: Boolean, showExitDialog: Boolean, onExit: () -> Unit, context: android.content.Context){
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
            onClick = {
                val weightMap = currentWeights.mapValues { it.value.toIntOrNull() ?: 0 }
                viewModel.onNfcTagDetected("")
                viewModel.enterPlayerWeights(weightMap, false, context)
            },
            modifier = Modifier.weight(1f),
            enabled = allInputsValid // HIER AKTIVIERT
        ) {
            Text("Noch eine Runde")
        }

        Button(
            onClick = { onExit()
                viewModel.onNfcTagDetected("")},
            modifier = Modifier.weight(1f),
            enabled = allInputsValid, // HIER AKTIVIERT
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                disabledContainerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
            )
        ) {
            Text("Spiel Beenden")
        }
    }
}
@Composable
fun NFCandBluetoothUI(uiState: GameUiState, viewModel: GameViewModel, liveWeight: String, detectedPlayer: Player?){
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        tonalElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Linke Seite: Gewicht
                Column {
                    Row(verticalAlignment = Alignment.Top) {
                        Text(
                            "LIVE-GEWICHT",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.width(8.dp))
                        if (uiState.isWeightStable) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = Color(0xFF4CAF50),
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                    }


                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = if (liveWeight.isEmpty()) "0.0" else liveWeight,
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Black)
                        )
                        Text(
                            " g",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(bottom = 4.dp, start = 2.dp)
                        )
                    }
                }

                // Rechte Seite: NFC Status
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "NFC SCANNER",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (uiState.nfcIsUsed) MaterialTheme.colorScheme.secondary else Color.Gray
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {

                        Icon(
                            imageVector = if (uiState.nfcIsUsed) Icons.Default.Nfc else Icons.Default.Nfc, // Nutze Nfc Icon
                            contentDescription = null,
                            tint = if (!viewModel.lastScannedTagId.isNullOrEmpty())
                                MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            // Animierte Anzeige für erkannten Spieler
            AnimatedVisibility(
                visible = !viewModel.lastScannedTagId.isNullOrEmpty(),
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Surface(
                    color = if (detectedPlayer != null) Color(0xFF4CAF50).copy(alpha = 0.1f)
                    else MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .padding(top = 12.dp)
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (detectedPlayer != null) Icons.Default.Person else Icons.Default.Warning,
                            contentDescription = null,
                            tint = if (detectedPlayer != null) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = if (detectedPlayer != null)
                                "Erkannt: ${detectedPlayer.name}"
                            else "Unbekannter Chip (${viewModel.lastScannedTagId})",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = if (detectedPlayer != null) Color(0xFF2E7D32) else MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InnerResultCadd(releventError: Int, releventAbsolutError: Int, player: Player, liveWeight: String, onCaptured: (String) -> Unit, round: Int, landscape: Boolean, hidePlacement: Boolean = false, inputField: @Composable () -> Unit){

    Column() {
        val textColor=if (releventError>=0) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error
        val headerText=if (round > 1){
            val positionStr = if (hidePlacement) "" else "${player.position}. "
            if (player.nextErrorMultiplier!=1.0){
                "$positionStr${player.name} (${player.personalTarget}g) - Err Multi ${player.nextErrorMultiplier}"
            }
            else{
                "$positionStr${player.name} (${player.personalTarget}g)"
            }




        }
        else{
            "${player.name} (${player.personalTarget}g)"
        }
        if (player.itemHit!=0){
            Text(AvailableItems.items.first { it.id == player.itemHit }.name)
        }
        Row() {

                Text(

                    text = headerText,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )



        }

        if (!landscape){
            Text(
                text = "Letzte Messung: ${player.currentWeight} g",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

        }
        val errorText = if (hidePlacement) "Fehler $releventError" else "Fehler $releventError / $releventAbsolutError"
        Text(
            text = errorText,
            style = MaterialTheme.typography.bodyMedium,
            color = textColor,
            fontWeight = FontWeight.Bold
        )


    }

    // Mitte: Der "Capture" Button (Waage)
    Row(modifier = Modifier
        .padding(2.dp)
        .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically) {IconButton(
        onClick = { onCaptured(liveWeight) },
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
        ),
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        Icon(Icons.Default.Scale, contentDescription = "Wert von Waage übernehmen")
    }

        // Rechts: Das Eingabefeld (wird von außen reingereicht)
        inputField() }

}

@Composable
fun ErrorInfoCard(modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = "Fehler <0 zu wenig getrunken",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Fehler >0 zu viel getrunken",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFF4CAF50),
                fontWeight = FontWeight.Bold
            )
        }
    }
}