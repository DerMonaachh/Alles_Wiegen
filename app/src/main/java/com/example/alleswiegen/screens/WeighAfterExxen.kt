package com.example.alleswiegen.screens

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.alleswiegen.GameUiState
import com.example.alleswiegen.GameViewModel
import com.example.alleswiegen.components.ScaleInfo
import com.example.alleswiegen.components.WeightInputField
import kotlin.collections.set
import kotlin.text.toDoubleOrNull

@Composable
fun WeighAfterExxenScreen(
    viewModel: GameViewModel,
    uiState: GameUiState
) {

    val weights = uiState.currentWeights
    val liveWeight = viewModel.liveWeight

    val player = uiState.players.first { it.name == uiState.exxer }

    val weightStr = weights[player.name] ?: ""
    val weightDouble = weightStr.toIntOrNull()
    val inputValid = weightDouble != null
    val liveWeightValue = liveWeight.toIntOrNull() ?: 0
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            "${uiState.exxer} Wiege dein neues Getränk. Du musst ${player.personalTarget} trinken um den Stand deines alten Getränks zu erreichen.",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(Modifier.width(8.dp))
        ScaleInfo(uiState, player, { newValue ->
            //weights[player.name] = newValue
            viewModel.updateWeight(newValue, player.name)

                                   }, liveWeight, context,liveWeightValue)

        Spacer(modifier = Modifier.height(24.dp))

        WeightInputField(
            playerName = player.name,
            currentWeight = weights[player.name] ?: "",
            liveWeight = liveWeight,
            isBluetoothConnected = uiState.isBluetoothConnected,
            onWeightChange = { newValue -> //weights[player.name] = newValue
            viewModel.updateWeight(newValue, player.name)
            }
        )
        if (inputValid){
            Spacer(modifier = Modifier.height(24.dp))
            Text("Dein Fehler wird ${player.upcomingError(weightDouble)} g sein.", style = MaterialTheme.typography.bodyMedium)
        }


        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {

                if (weightDouble!=0){
                    viewModel.updatePersonalTargetAfterExxen(weightDouble)
            }else{
                    Toast.makeText(context,"Gewicht ist leer", Toast.LENGTH_SHORT).show()
                }

            },
            enabled = inputValid,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Weiter")
        }
    }
}