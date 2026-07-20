package com.example.alleswiegen.components

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.alleswiegen.GameUiState
import androidx.compose.runtime.mutableStateMapOf
import com.example.alleswiegen.Player
import kotlin.collections.set

@Composable
fun ScaleInfo(uiState: GameUiState, player: Player,   onWeightChange: (String) -> Unit, liveWeight: String, context: Context,liveWeightValue:Int){
    if (uiState.isBluetoothConnected) {
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

            if (uiState.isWeightStable && liveWeightValue > 10) {
                onWeightChange( liveWeight)
                Toast.makeText(
                    context,
                    "${liveWeight} g für ${player.name} eingetragen.",
                    Toast.LENGTH_SHORT
                ).show()


            }

        }
    }
}