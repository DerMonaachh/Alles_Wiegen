package com.example.alleswiegen.screens

import com.example.alleswiegen.GameMode
import android.content.res.Configuration
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Games
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.alleswiegen.GameUiState
import com.example.alleswiegen.GameViewModel
import com.example.alleswiegen.PlayerRank
import com.example.alleswiegen.PlayerScoreEntry
import com.example.alleswiegen.Screen
import com.example.alleswiegen.components.AdvancedPlayerChart
import com.example.alleswiegen.components.EventData
import com.example.alleswiegen.components.EventEditDialog
import com.example.alleswiegen.components.getFinalPlayerRanks
import com.example.alleswiegen.components.transformScoresToPlayerRanks
enum class Methoden(val displayName: String) {
    KLASSISCH("Klassische Wertung"),
    BAGEL("Bagel Quote"),
    EXXEN("Ohne Bagel"),
    DURSCHNITTLICHER_FEHLER("Durschnittlicher Fehler");


    // Hilfsfunktion, um eine Liste für das Dropdown zu bekommen
    companion object {
        fun getDisplayNames() = entries.map { it.displayName }
    }
}




@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Events(
    viewModel: GameViewModel,
    uiState: GameUiState,
    onNewEvent: () -> Unit = {}
) {

    BackHandler {
        viewModel.navigateTo(Screen.SETUP)
    }

    // UI State for dropdowns and toggles
    var expandedEvent by remember { mutableStateOf(false) }


    var expandedRating by remember { mutableStateOf(false) }
    var expandedGameMode by remember { mutableStateOf(false) }
    var selectedRatingMethod by rememberSaveable { mutableStateOf(Methoden.KLASSISCH.displayName) }
    var selectedGameMode by rememberSaveable { mutableStateOf(GameMode.Normal.displayName)}
    var showDialog by remember { mutableStateOf(false) }
    var showQrDialog by remember { mutableStateOf(false) }
    var editingEvent by remember { mutableStateOf<EventData?>(null) }
    val allEvents by viewModel.allEvents.collectAsState(initial = emptyList())
    var selectedEvent by rememberSaveable { mutableStateOf("") }
    val context = LocalContext.current
    var scannedText by remember { mutableStateOf<String?>(null) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val scannerOptions = com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions.Builder()
        .setBarcodeFormats(com.google.mlkit.vision.barcode.common.Barcode.FORMAT_QR_CODE) // Korrekte Referenz
        .enableAutoZoom()
        .build()

    val scanner = remember { com.google.mlkit.vision.codescanner.GmsBarcodeScanning.getClient(context, scannerOptions) }

    // 2. Logik für den Scanner-Aufruf
    fun startScanner() {
        scanner.startScan()
            .addOnSuccessListener { barcode ->
                val rawValue: String? = barcode.rawValue
                if (rawValue != null) {
                    // Hier wird noch nichts verarbeitet, nur gespeichert
                    scannedText = rawValue
                    showConfirmDialog = true
                }
            }
            .addOnFailureListener { e ->
                if (e is com.google.android.gms.common.api.ApiException && e.statusCode == 17) {
                    // Canceled by User
                } else {
                    Toast.makeText(context, "Fehler beim Scannen: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnCompleteListener {
                showQrDialog = false
            }
    }


    // Sobald allEvents geladen wurde, setzen wir den Default-Wert
    LaunchedEffect(allEvents) {
        if (selectedEvent.isEmpty() && allEvents.isNotEmpty()) {
            selectedEvent = allEvents.first().name
        }
    }

    val currentEvent = allEvents.find { it.name == selectedEvent }

    // Update Filter when Event changes
    LaunchedEffect(currentEvent) {
        viewModel.setTimeFilterwithDates(
            currentEvent?.startTimestamp ?: 0,
            currentEvent?.endTimestamp ?: Long.MAX_VALUE
        )
    }

    val graphList = transformScoresToPlayerRanks(viewModel, gameMode = selectedGameMode)//Berechent den Zeitlichen Verlauf aller Punkte
    val finalResult = getFinalPlayerRanks(graphList)//Nimmt jeweils nur den letzten WErt um den Endstand zu haben
    println(currentEvent)
    // State for Ranking vs Graph (0 = Ranking, 1 = Graph)
    var selectedViewIndex by rememberSaveable { mutableIntStateOf(0) }
    val viewOptions = listOf("Ranking", "Graph")
    if (showDialog) {
        EventEditDialog(
            initialEvent = editingEvent ?: EventData(),
            onDismiss = {
                showDialog = false
                editingEvent = null
            },
            onConfirm = { updatedEvent ->
                val adjustedEvent = updatedEvent.copy(
                    endDate = updatedEvent.endDate?.let { millis ->
                        // Adds 23 hours, 59 minutes, 59 seconds and 999ms
                        millis + (24 * 60 * 60 * 1000) - 1
                    }
                )
                viewModel.saveEventWithLong(adjustedEvent)

                showDialog = false
                editingEvent = null
            }
        )
    }
    if (isLandscape){
   
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                AdvancedPlayerChart(graphList,  selectedRatingMethod)}
   
        
    } else {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = { Text("Events & Statistik") },
                navigationIcon = {
                    IconButton(onClick = {viewModel.navigateTo(Screen.SETUP)}) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                },
                actions = {
                    IconButton(onClick =
                        {

                        if (selectedEvent!="All Time"){
                            val currentEventObject = allEvents.find { it.name == selectedEvent }

                            editingEvent = EventData(
                                name = selectedEvent,
                                // Convert back from your relative Int storage to Long milliseconds for the picker
                                startDate = currentEventObject?.startTimestamp,
                                endDate = currentEventObject?.endTimestamp
                            )



                            showDialog=true
                        }
                        else{
                            Toast.makeText(context, "Dieses Event kann nicht bearbeitet werden.", Toast.LENGTH_SHORT).show()
                        }


                    })
                     {
                        Icon(Icons.Default.Edit, contentDescription = "Event Bearbeiten")
                    }
                }
            )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- Dropdown Selectors ---
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Event Selection
                ExposedDropdownMenuBox(
                    expanded = expandedEvent,
                    onExpandedChange = { expandedEvent = !expandedEvent },

                ) {
                    OutlinedTextField(
                        value = selectedEvent,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Event") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedEvent) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedEvent,
                        onDismissRequest = { expandedEvent = false }) {
                        allEvents.forEach { event ->
                            DropdownMenuItem(
                                text = { Text(event.name) },
                                onClick = {
                                    selectedEvent = event.name
                                    expandedEvent = false
                                }
                            )
                        }
                    }
                }
                //GameMode Selection
                ExposedDropdownMenuBox(
                    expanded = expandedGameMode,
                    onExpandedChange = { expandedGameMode = !expandedGameMode },

                ) {
                    OutlinedTextField(
                        value = selectedGameMode,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("GameMode") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedGameMode) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedGameMode,
                        onDismissRequest = { expandedGameMode = false }) {
                        GameMode.getDisplayNames().forEach { mode ->
                            DropdownMenuItem(
                                text = { Text(mode) },
                                onClick = {
                                    selectedGameMode = mode
                                    expandedGameMode= false
                                }
                            )
                        }
                    }
                }
            }
                // Rating Method Selection
                ExposedDropdownMenuBox(
                    expanded = expandedRating,
                    onExpandedChange = { expandedRating = !expandedRating },

                ) {
                    OutlinedTextField(
                        value = selectedRatingMethod,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Bewertung") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRating) },
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                    )
                    ExposedDropdownMenu(
                        expanded = expandedRating,
                        onDismissRequest = { expandedRating = false }) {
                        Methoden.getDisplayNames().forEach { method ->
                            DropdownMenuItem(
                                text = { Text(method) },
                                onClick = {
                                    selectedRatingMethod = method
                                    expandedRating = false
                                }
                            )
                        }
                    }
                }


            // --- View Toggle (Ranking vs Graph) ---
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                viewOptions.forEachIndexed { index, label ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(
                            index = index,
                            count = viewOptions.size
                        ),
                        onClick = { selectedViewIndex = index },
                        selected = index == selectedViewIndex
                    ) {
                        Text(label)
                    }
                }
            }

            // --- Content Area ---
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = MaterialTheme.shapes.medium
            ) {
                if (selectedViewIndex == 0) {
                    // Ranking List
                    LazyColumn(
                        contentPadding = PaddingValues(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if ( selectedRatingMethod== Methoden.KLASSISCH.displayName){
                            items(finalResult.sortedByDescending { it.pointsByErrorClassic }) { score ->
                                RankingItem(score,selectedRatingMethod)
                            }
                        }
                        if ( selectedRatingMethod==Methoden.EXXEN.displayName){
                            items(finalResult.sortedByDescending { it.pointsByErrorNoExxer }) { score ->
                                RankingItem(score,selectedRatingMethod)
                            }
                        }
                        if ( selectedRatingMethod==Methoden.BAGEL.displayName){
                            items(finalResult.sortedByDescending { it.bagelRate }) { score ->
                                RankingItem(score,selectedRatingMethod)
                            }
                        }
                        if ( selectedRatingMethod==Methoden.DURSCHNITTLICHER_FEHLER.displayName){
                            items(finalResult.sortedBy { it.averageError }) { score ->
                                RankingItem(score,selectedRatingMethod)
                            }
                        }


                    }
                } else {
                    // Graph Placeholder
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        AdvancedPlayerChart(graphList,  selectedRatingMethod)
                    }
                }
            }

            // --- Footer Button ---
            Row() {Button(
                onClick = {
                    editingEvent = null // New event
                    showDialog = true
                },

                // ...
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Text("NEUES EVENT")
            }
                Spacer(modifier = Modifier.width(2.dp))
                Button(
                    onClick = {
                        showQrDialog=true
                    },

                    // ...
                ) {
                    Icon(Icons.Default.QrCode, contentDescription = null)

                }
                Spacer(modifier = Modifier.width(2.dp))
                Button(
                    onClick = {
                        viewModel.navigateTo(Screen.ALL_EVENTS)
                    },

                    // ...
                ) {
                    Icon(Icons.Default.Games, contentDescription = null)
                    Text("Alle Spiele")

                }
            }

        }
    }
}
    if (showQrDialog) {
        LaunchedEffect(Unit) {
            startScanner()
        }
    }
    if (showConfirmDialog && scannedText != null) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = {
                showConfirmDialog = false
                scannedText = null
            },
            title = { Text("QR-Code Inhalt prüfen") },
            text = {
                Column {
                    Text("Möchtest du folgende Daten importieren?")
                    Spacer(modifier = Modifier.height(8.dp))
                    // Zeigt den rohen Inhalt des QR-Codes an
                    Text(
                        text = scannedText!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Erst hier wird die ViewModel-Logik aufgerufen
                        val qrResult = viewModel.parseCsvFromQr(scannedText!!)

                        // qrResult (String vom ViewModel) als Toast anzeigen
                        Toast.makeText(context, qrResult, Toast.LENGTH_LONG).show()


                        showConfirmDialog = false
                        scannedText = null
                    }
                ) {
                    Text("Ja, Importieren")
                }
            },
            dismissButton = {
                androidx.compose.material3.TextButton(
                    onClick = {
                        showConfirmDialog = false
                        scannedText = null
                    }
                ) {
                    Text("Abbrechen")
                }
            }
        )
    }
}

@Composable
fun RankingItem(score: PlayerRank,method: String) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = score.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Spacer(modifier = Modifier.width(8.dp))
            val infoText = when (method){
                   Methoden.KLASSISCH.displayName -> "Klassische Punkte ${score.pointsByErrorClassic}"
                Methoden.EXXEN.displayName -> "Punkte ohne Fehler durch Bagel ${score.pointsByErrorNoExxer} "
                Methoden.BAGEL.displayName ->" ${"%.1f".format(score.bagelRate)} pro Spiel | Gesamt: ${score.bagels}"
                  Methoden.DURSCHNITTLICHER_FEHLER.displayName ->  "Fehler ${"%.1f".format(score.averageError)}g"
                else -> "Klassische Punkte ${score.pointsByErrorClassic}"
                }



            Text(
                text = infoText,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

