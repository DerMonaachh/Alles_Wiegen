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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import com.example.alleswiegen.utils.PermissionUtils
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.foundation.Image
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.alleswiegen.AvailableItems
import com.example.alleswiegen.Item
import com.example.alleswiegen.utils.PatchNotes
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.example.alleswiegen.GameUiState
import com.example.alleswiegen.GameViewModel
import com.example.alleswiegen.Screen

@Composable
fun OptionsScreen(viewModel: GameViewModel, uiState: GameUiState) {
    // State, um den aktuell ausgewählten Tab-Index zu speichern (Standard: 0 -> Bluetooth)
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Bluetooth", "Spiel", "Items", "Allgemeines")
    val context = LocalContext.current


    Column(modifier = Modifier.fillMaxSize()) {
        // Die Material 3 Scrollable Tab-Leiste
        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            edgePadding = 16.dp
        ) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                            maxLines = 1
                        )
                    }
                )
            }
        }

        // Container für den Inhalt des jeweils ausgewählten Tabs
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.TopStart
        ) {
            when (selectedTabIndex) {
                0 -> BluetoothOptions(viewModel, uiState, context)
                1 -> GameOptions(viewModel, uiState, context)
                2 -> ItemOverview(viewModel, uiState, context)
                3 -> GeneralOptions(viewModel, uiState)
            }
        }
        
        Button(
            onClick = { viewModel.navigateTo(Screen.SETUP) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),

            ) {
            Text("Zurück")
        }
    }
}

@Composable
fun ItemOverview(viewModel: GameViewModel, uiState: GameUiState, context: Context) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp)
    ) {


        items(uiState.getEffectiveItems()) { item ->
            ItemCard(item, viewModel, context)
        }


    }
}

@Composable
fun ItemCard(item: Item, viewModel: GameViewModel, context: Context) {
    var rarityInput by remember(item.rarity) { mutableStateOf(item.rarity.toString()) }
    var multiInput by remember(item.multiplikator) { mutableStateOf(item.multiplikator.toString()) }
    var wertInput by remember(item.wert) { mutableStateOf(item.wert.toString()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = item.imageRes),
                    contentDescription = item.name,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                
                Spacer(modifier = Modifier.size(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Seltenheit: ${"★".repeat(item.rarity.coerceIn(1, 5))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.buildBeschreibung(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = rarityInput,
                    onValueChange = { newVal ->
                        rarityInput = newVal
                        newVal.toIntOrNull()?.let { r ->
                            viewModel.updateItemProperty(context, item.id, r, item.multiplikator, item.wert)
                        }
                    },
                    label = { Text("Seltenheit") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = multiInput,
                    onValueChange = { newVal ->
                        multiInput = newVal
                        newVal.toDoubleOrNull()?.let { m ->
                            viewModel.updateItemProperty(context, item.id, item.rarity, m, item.wert)
                        }
                    },
                    label = { Text("Multi.") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = wertInput,
                    onValueChange = { newVal ->
                        wertInput = newVal
                        newVal.toIntOrNull()?.let { w ->
                            viewModel.updateItemProperty(context, item.id, item.rarity, item.multiplikator, w)
                        }
                    },
                    label = { Text("Wert") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }
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

    val scanPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.all { it }) {
            viewModel.startBluetoothScan(context)
        } else {
            Toast.makeText(context, "Bluetooth-Berechtigung verweigert.", Toast.LENGTH_SHORT).show()
        }
    }

    // Initial load of saved scales
    LaunchedEffect(Unit) {
        savedScales.putAll(viewModel.getSavedScales(context))
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopBluetoothScan()
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp)
    ) {


        // --- BLE SCANNER CARD ---
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "AKTIVE WAAGEN SUCHEN",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Text(
                        text = "Scannt nach eingeschalteten Bluetooth-Waagen in der Nähe.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                if (uiState.isScanningBluetooth) {
                                    viewModel.stopBluetoothScan()
                                } else {
                                    if (PermissionUtils.hasBluetoothPermissions(context)) {
                                        viewModel.startBluetoothScan(context)
                                    } else {
                                        scanPermissionLauncher.launch(PermissionUtils.getRequiredBluetoothPermissions())
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            if (uiState.isScanningBluetooth) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.size(8.dp))
                                Text("Scan Stoppen", style = MaterialTheme.typography.labelMedium)
                            } else {
                                Text("Nach Waagen scannen 🔍", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }

                    if (uiState.discoveredBluetoothDevices.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Gefundene Geräte (${uiState.discoveredBluetoothDevices.size}):",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        val sortedDevices = uiState.discoveredBluetoothDevices.sortedByDescending { it.isScaleCandidate }
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            sortedDevices.forEach { device ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            if (device.name.isNotBlank() && device.name != "Unbekanntes Gerät") {
                                                scaleName = device.name
                                            }
                                            viewModel.selectDiscoveredDevice(context, device)
                                            Toast.makeText(
                                                context,
                                                "Waage ${device.name} ausgewählt",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (device.isScaleCandidate)
                                            MaterialTheme.colorScheme.secondaryContainer
                                        else
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(12.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = device.name,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold
                                                )
                                                if (device.isScaleCandidate) {
                                                    Spacer(modifier = Modifier.size(6.dp))
                                                    Text(
                                                        text = "★ Waage erkannt",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.primary,
                                                        fontWeight = FontWeight.Bold
                                                    )
                                                }
                                            }
                                            Text(
                                                text = device.address,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Text(
                                            text = "${device.rssi} dBm",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    } else if (uiState.isScanningBluetooth) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Suche nach Geräten in der Nähe...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
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
                                modifier = Modifier.fillMaxWidth(0.8f)
                            ) {
                                if (savedScales.isEmpty()) {
                                    DropdownMenuItem(
                                        text = { Text("Keine gespeicherten Waagen") },
                                        onClick = { expanded = false }
                                    )
                                }
                                savedScales.forEach { (name, address) ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(
                                                        text = name,
                                                        style = MaterialTheme.typography.bodyMedium,
                                                        fontWeight = FontWeight.SemiBold
                                                    )
                                                    Text(
                                                        text = address,
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                                IconButton(
                                                    onClick = {
                                                        viewModel.deleteScale(context, name)
                                                        savedScales.remove(name)
                                                        Toast.makeText(
                                                            context,
                                                            "$name gelöscht",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "Löschen",
                                                        tint = MaterialTheme.colorScheme.error,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                }
                                            }
                                        },
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
        }

        item {
            OutlinedTextField(
                value = uiState.bluetoothAddress,
                onValueChange = { newAddr ->
                    viewModel.updateBluetoothAddress(context, newAddr)
                },
                label = { Text("Bluetooth MAC-Adresse") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = uiState.SERVICE_UUID,
                onValueChange = { viewModel.updateBluetoothServiceUuid(context, it) },
                label = { Text("SERVICE_UUID") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = uiState.CHARACTERISTIC_UUID,
                onValueChange = { viewModel.updatecharUudi(context, it) },
                label = { Text("CHARACTERISTIC_UUID") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun GameOptions(viewModel: GameViewModel, uiState: GameUiState,context: Context) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp)
    ) {

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
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

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                            .clickable { viewModel.toggleHidePlacement(!uiState.hidePlacement, context) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Platzierung Verbergen",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Gesamtfehler und Platzierung nicht anzeigen.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Switch(
                            checked = uiState.hidePlacement,
                            onCheckedChange = null,
                            thumbContent = if (uiState.hidePlacement) {
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

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(androidx.compose.foundation.shape.RoundedCornerShape(8.dp))
                            .clickable { viewModel.toggleSortByRand(!uiState.sortByRank, context) }
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

                        Switch(
                            checked = uiState.sortByRank,
                            onCheckedChange = null,
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
                            Text("Bagel Jingle abspielen", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Sound und Video bei einem Fehler von 0g (\uD83E\uDD6F) abspielen.",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Switch(
                            checked = uiState.showJingle,
                            onCheckedChange = null
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { viewModel.toggleUiTips(!uiState.showUiTips, context) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("UI-Tipps anzeigen", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Erklärungen und Hinweise im Spiel",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Switch(
                            checked = uiState.showUiTips,
                            onCheckedChange = null
                        )
                    }
                }
            }
        }

        item {
            PointsCalculationCard()
        }
    }
}

@Composable
fun PointsCalculationCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                androidx.compose.material3.Surface(
                    shape = androidx.compose.foundation.shape.CircleShape,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                    modifier = Modifier.size(36.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "PUNKTE-BERECHNUNG",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Formel & Abweichungspunkte pro Spiel",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(
                modifier = Modifier.padding(bottom = 12.dp),
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f)
            )

            // Formel Box
            androidx.compose.material3.Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "P = ( A / 2 - Pos ) + 1 + E",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Legende
            Text(
                text = "Legende:",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                LegendItem(symbol = "P", description = "Erzielte Gesamtpunkte pro Spiel")
                LegendItem(symbol = "A", description = "Anzahl aller Spieler im Spiel")
                LegendItem(symbol = "Pos", description = "Deine Platzierung (1 = 1. Platz, 2 = 2. Platz, ...)")
                LegendItem(symbol = "E", description = "Bonus-/Malus-Punkte basierend auf dem Fehler")
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
            Spacer(modifier = Modifier.height(12.dp))

            // Fehler-Punkte Tabelle
            Text(
                text = "Fehler-Punkte (E):",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                ErrorPointRow(range = "< 4,0 g", points = "+3 Punkte", isPositive = true)
                ErrorPointRow(range = "4,0 – 6,0 g", points = "+2 Punkte", isPositive = true)
                ErrorPointRow(range = "6,1 – 10,0 g", points = "+1 Punkt", isPositive = true)
                ErrorPointRow(range = "10,1 – 20,0 g", points = "0 Punkte")
                ErrorPointRow(range = "20,1 – 30,0 g", points = "-1 Punkt", isNegative = true)
                ErrorPointRow(range = "> 30,0 g", points = "-2 Punkte", isNegative = true)
            }
        }
    }
}

@Composable
private fun LegendItem(symbol: String, description: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        androidx.compose.material3.Surface(
            shape = RoundedCornerShape(4.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.padding(end = 8.dp)
        ) {
            Text(
                text = symbol,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ErrorPointRow(
    range: String,
    points: String,
    isPositive: Boolean = false,
    isNegative: Boolean = false
) {
    val pointColor = when {
        isPositive -> MaterialTheme.colorScheme.primary
        isNegative -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Durchschnittlicher Fehler $range",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = points,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = pointColor
        )
    }
}

@Composable
fun GeneralOptions(viewModel: GameViewModel, uiState: GameUiState) {
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showPatchNotesDialog by remember { mutableStateOf(false) }

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
            viewModel.updateJinglePath(it.toString(), context)
        }
    }

    if (showPatchNotesDialog) {
        AlertDialog(
            onDismissRequest = { showPatchNotesDialog = false },
            title = { Text("Patch Notes") },
            text = {
                Box(
                    modifier = Modifier
                        .height(400.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(text = PatchNotes.getPatchNotes())
                }
            },
            confirmButton = {
                TextButton(onClick = { showPatchNotesDialog = false }) {
                    Text("Schließen")
                }
            }
        )
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Wirklich löschen?") },
            text = { Text("Möchtest du wirklich alle Einträge in der Datenbank löschen? Diese Aktion kann nicht rückgängig gemacht werden.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearDatabasePlayerScore()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Löschen")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Abbrechen")
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 16.dp)
    ) {
        item {
            Text(
                text = "Allgemeines",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
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

                    // ... vibration toggle ...
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

                    Spacer(Modifier.height(8.dp))

                    // ... jingle picker ...
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { folderPickerLauncher.launch(null) }
                            .padding(vertical = 8.dp)
                    ) {
                        Text("Bagel Ordner", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = uiState.jingleLocation.ifEmpty { "Kein Ordner ausgewählt" },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)
                    Text(
                        text = "INFO",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "App Version 1.18" ,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Button(
                        onClick = { showPatchNotesDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Patch Notes und Dank ansehen")
                    }

                    Spacer(Modifier.height(8.dp))
                    // --- STARTUP MESSAGE TOGGLE ---
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { viewModel.toggleStartupMessage(!uiState.showStartupMessage, context) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Start-Hinweis anzeigen", style = MaterialTheme.typography.titleMedium)
                            Text(
                                "Verantwortungsvoll-Meldung beim App-Start",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Switch(
                            checked = uiState.showStartupMessage,
                            onCheckedChange = null
                        )
                    }

                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)
                    Text(
                        text = "ANLEITUNG & LINKS",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse("https://zygon-labs.github.io/alles-wiegen/"))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Spiel Infos, Anleitung & Datenschutz öffnen")
                    }

                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), thickness = 0.5.dp)
                    Text(
                        text = "DATENBANK-EINSTELLUNGEN",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Button(
                        onClick = { showDeleteDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Alle Einträge löschen.")
                    }
                }
            }
        }
    }
}
