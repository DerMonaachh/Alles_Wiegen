package com.example.alleswiegen.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlin.collections.set
/*

@Composable
fun WeightInputField(
    playerName: String,
    currentWeight: String,
    liveWeight: String,
    isBluetoothConnected: Boolean,
    onWeightChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = playerName,
            modifier = Modifier
                .weight(1f)
                                .padding(8.dp),
            style = MaterialTheme.typography.bodyLarge
        )
        if (isBluetoothConnected) {
        IconButton(
            onClick = { onWeightChange(liveWeight) },
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Icon(Icons.Default.Scale, contentDescription = "Wert von Waage übernehmen")
        }
        }

        OutlinedTextField(
            value = currentWeight,
            onValueChange = onWeightChange,
            label = { Text("Gramm") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            modifier = Modifier.width(120.dp)
        )
    }
} */
@Composable
fun WeightInputField(
    playerName: String,
    currentWeight: String,
    liveWeight: String,
    isBluetoothConnected: Boolean,
    onWeightChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp) // Etwas Abstand zwischen den Zeilen
    ) {
        Text(
            text = playerName,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.titleMedium, // Etwas kräftiger als bodyLarge
            color = MaterialTheme.colorScheme.onSurface
        )

        OutlinedTextField(
            value = currentWeight,
            onValueChange = onWeightChange,
            modifier = Modifier.width(140.dp), // Etwas breiter für das Icon
            label = { Text("Gewicht") },
            suffix = { Text("g") }, // "g" steht fest im Feld
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number
            ),
            trailingIcon = {
                // Das Icon erscheint nur, wenn Bluetooth verbunden ist, IM Textfeld
                if (isBluetoothConnected) {
                    IconButton(
                        onClick = { onWeightChange(liveWeight) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Scale,
                            contentDescription = "Von Waage übernehmen",
                            tint = MaterialTheme.colorScheme.primary // Farblicher Akzent
                        )
                    }
                }
            },
            shape = MaterialTheme.shapes.medium // Abgerundete Ecken
        )
    }
}