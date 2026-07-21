package com.example.alleswiegen.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.alleswiegen.GameUiState
import com.example.alleswiegen.GameViewModel
import com.example.alleswiegen.Screen

@Composable
fun OptionsScreen(viewModel: GameViewModel, uiState: GameUiState) {
    // State, um den aktuell ausgewählten Tab-Index zu speichern (Standard: 0 -> Bluetooth)
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Bluetooth Optionen", "Spiel Optionen", "Allgemeines")
    val context = LocalContext.current


    Column(modifier = Modifier.fillMaxWidth()) {
        // Die Material 3 Tab-Leiste
        TabRow(selectedTabIndex = selectedTabIndex) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                )
            }
        }

        // Container für den Inhalt des jeweils ausgewählten Tabs
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.TopStart
        ) {
            when (selectedTabIndex) {
                0 -> BluetoothOptions(viewModel, uiState,context)
                1 -> GameOptions(viewModel, uiState,context)
                2 -> GeneralOptions(viewModel, uiState)
            }
        }
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { viewModel.navigateTo(Screen.SETUP) },
            modifier = Modifier.fillMaxWidth(),

            ) {
            Text("Zurück")
        }
    }
}

// ... other imports
@Composable
fun BluetoothOptions(viewModel: GameViewModel, uiState: GameUiState, context: Context) {
    // Local state for the Scale Name input and Dropdown control
    var scaleName by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val savedScales = remember { mutableStateMapOf<String, String>() }

    // Initial load of saved scales
    LaunchedEffect(Unit) {
        savedScales.putAll(viewModel.getSavedScales(context))
    }

    Column {
        Text(
            text = "Bluetooth Optionen",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                    alpha = 0.3f
                )
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // --- NEW: Waagen Name Field ---
                OutlinedTextField(
                    value = scaleName,
                    onValueChange = { scaleName = it },
                    label = { Text("Waagen Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(Modifier.height(8.dp))

                // --- NEW: Buttons Row ---
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            if (scaleName.isNotBlank() && uiState.bluetoothAddress.isNotBlank()) {
                                viewModel.saveScale(context, scaleName, uiState.bluetoothAddress)
                                savedScales[scaleName] =
                                    uiState.bluetoothAddress // Update local list
                            }
                            Toast.makeText(
                                context,
                                "${scaleName} mit Adresse ${uiState.bluetoothAddress} gespeichert.",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Speichern", style = MaterialTheme.typography.labelSmall)
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        Button(
                            onClick = { expanded = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Laden", style = MaterialTheme.typography.labelSmall)
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            modifier = Modifier.fillMaxWidth(0.5f)
                        ) {
                            if (savedScales.isEmpty()) {
                                DropdownMenuItem(
                                    text = { Text("Keine gespeicherten Waagen") },
                                    onClick = { expanded = false }
                                )
                            }
                            savedScales.forEach { (name, address) ->
                                DropdownMenuItem(
                                    text = { Text(name) },
                                    onClick = {
                                        scaleName = name
                                        viewModel.updateBluetoothAddress(context, address)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Existing Address Field
        OutlinedTextField(
            value = uiState.bluetoothAddress,
            onValueChange = { newAddr ->
                viewModel.updateBluetoothAddress(context, newAddr)
            },
            label = { Text("Bluetooth MAC-Adresse") },
            modifier = Modifier.fillMaxWidth()
        )

        // ... existing UUID fields and Rohdaten text ...
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = uiState.SERVICE_UUID,
            onValueChange = { viewModel.updateBluetoothServiceUuid(context, it) },
            label = { Text("SERVICE_UUID") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = uiState.CHARACTERISTIC_UUID,
            onValueChange = { viewModel.updatecharUudi(context, it) },
            label = { Text("CHARACTERISTIC_UUID") },
            modifier = Modifier.fillMaxWidth()
        )
        // ... raw data string ...
    }
}

@Composable
fun GameOptions(viewModel: GameViewModel, uiState: GameUiState,context: Context) {

    Column {
        Text(
            text = "Spiel Optionen",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(16.dp), // Etwas rundere Ecken für einen modernen Look
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp) // Einheitliches Padding für den Inhalt
            ) {
                // Sektions-Überschrift
                Text(
                    text = "SPIELOPTIONEN",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                )

                // Die Option als klickbare Zeile
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
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    thickness = 0.5.dp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
                )

                // Die Option als klickbare Zeile
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp)) // Clip für den Ripple-Effekt
                        .clickable { viewModel.toggleSortByRand(!uiState.sortByRank) } // Ganze Zeile klickbar
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Spieler nach Platzierung sortieren?",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Oder Reinfolge beibehalten.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Der Switch
                    Switch(
                        checked = uiState.sortByRank,
                        onCheckedChange = null, // Auf null, da clickable in der Row das übernimmt
                        thumbContent = if (uiState.sortByRank) {
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
                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)

                // --- JINGLE TOGGLE ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { viewModel.toggleJingle(!uiState.showJingle, context) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Jingle abspielen", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Sound bei erfolgreichem Wiegen",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Switch(
                        checked = uiState.showJingle,
                        onCheckedChange = null
                    )
                }
            }
        }
    }
}

@Composable
fun GeneralOptions(viewModel: GameViewModel, uiState: GameUiState) {
    val context = LocalContext.current

    // Launcher für den Ordner-Picker (Jingle Location)
    val folderPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let {
            // Berechtigungen dauerhaft anfordern, damit die App auch nach Neustart Zugriff hat
            context.contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            // Speicher den Pfad im ViewModel/UiState (als String)
            viewModel.updateJinglePath(it.toString(),context)
        }
    }

    Column {
        Text(
            text = "Allgemeines",
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "APP-EINSTELLUNGEN",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // --- VIBRATION TOGGLE ---
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { viewModel.toggleVibration(!uiState.vibrate, context) }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Vibrieren beim Scan", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Haptisches Feedback bei Erkennung",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Switch(
                        checked = uiState.vibrate,
                        onCheckedChange = null
                    )
                }



                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)

                // --- JINGLE ORDNER PICKER ---
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { folderPickerLauncher.launch(null) }
                        .padding(vertical = 8.dp)
                ) {
                    Text("Jingle Ordner", style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = uiState.jingleLocation.ifEmpty { "Kein Ordner ausgewählt" },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }



            }
        }

        Spacer(Modifier.height(16.dp))

        // --- Clear DB ---
        Button(onClick = { viewModel.clearDatabasePlayerScore() }) { Text("Alle Einträge löschen.")}

    }
}