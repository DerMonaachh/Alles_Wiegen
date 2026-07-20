package com.example.alleswiegen.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.alleswiegen.GameUiState
import com.example.alleswiegen.GameViewModel


@Composable
fun SetTargetScreen(viewModel: GameViewModel, uiState: GameUiState) {
    var targetInput by remember { mutableStateOf("") }
    val leader = viewModel.getLeaderForNextRound()
    val focusManager = LocalFocusManager.current

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        leader?.let {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("${it.name}", style = MaterialTheme.typography.titleMedium)
                    Text("Du musst den Zielwert bestimmen.")
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = targetInput,
            onValueChange = { targetInput = it },
            label = { Text("Zielwert für diese Runde (g/ml)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth(),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus() // Schließt die Tastatur

                }
            ),

        )

        HorizontalDivider(Modifier.padding(vertical = 16.dp))


        Button(
            onClick = {
                val target = targetInput.toIntOrNull() ?: 0
                viewModel.enterNewTargets(target)
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = targetInput.isNotBlank()
        ) {
            Text("Trinken!")
        }
    }
}
