package com.example.alleswiegen.screens

import android.annotation.SuppressLint
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.copy
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.isEmpty
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

import com.example.alleswiegen.GameUiState
import com.example.alleswiegen.GameViewModel
import com.example.alleswiegen.PlayerScoreEntry
import com.example.alleswiegen.Screen
import com.example.alleswiegen.components.EditGameScreen
import com.example.alleswiegen.components.QrCodeDialog
import java.text.SimpleDateFormat
import kotlin.text.format
import kotlin.text.groupBy
import kotlin.text.toList
import java.io.OutputStreamWriter
import java.io.BufferedWriter
import java.io.BufferedReader
import java.io.InputStreamReader
import android.widget.Toast
import androidx.activity.compose.BackHandler

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllEvents(
    viewModel: GameViewModel,
    uiState: GameUiState,
){
    BackHandler {
        viewModel.navigateTo(Screen.EVENT)
    }
    // Collecting all scores from the repository
    val allScores by viewModel.allScores.collectAsState()
    var showEditWindow by remember { mutableStateOf<Long?>(null) }
    var showQRWindow by remember { mutableStateOf<Long?>(null) }
    var gameToDelete by remember { mutableStateOf<Long?>(null) }
    val context = LocalContext.current

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        uri?.let {
            try {
                context.contentResolver.openOutputStream(it)?.use { outputStream ->
                    BufferedWriter(OutputStreamWriter(outputStream)).use { writer ->
                        writer.write(viewModel.generateCsvForAllGames())
                    }
                }
                Toast.makeText(context, "Export erfolgreich", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Export fehlgeschlagen", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            try {
                context.contentResolver.openInputStream(it)?.use { inputStream ->
                    val content = BufferedReader(InputStreamReader(inputStream)).readText()
                    val message = viewModel.importCsvData(content)
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(context, "Import fehlgeschlagen", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Grouping scores by timestamp to represent unique games
    // We sort keys descending to show the newest games first
    val groupedGames = remember(allScores) {
        allScores.groupBy { it.timestamp }.toSortedMap(compareByDescending { it })
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Spiel-Historie") },
                windowInsets = WindowInsets(0, 0, 0, 0),
                navigationIcon = {
                    IconButton(onClick = {viewModel.navigateTo(Screen.EVENT)}) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Zurück")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        importLauncher.launch(arrayOf("text/csv", "text/comma-separated-values", "application/csv"))
                    }) {
                        Icon(Icons.Default.Upload, contentDescription = "Spiele importieren")
                    }
                    IconButton(onClick = {
                        exportLauncher.launch("alles_wiegen_export.csv")
                    }) {
                        Icon(Icons.Default.Download, contentDescription = "Alle Spiele exportieren")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        if (groupedGames.isEmpty()) {
            Box(Modifier
                .fillMaxSize()
                .padding(padding), contentAlignment = Alignment.Center) {
                Text("Keine Spiele gefunden", style = MaterialTheme.typography.bodyLarge)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(groupedGames.keys.toList()) { timestamp ->
                    val playersInGame = groupedGames[timestamp] ?: emptyList()
                    GameHistoryCard(
                        timestamp = timestamp,
                        players = playersInGame,
                        onClick = { showEditWindow = timestamp },
                        onLongClick = {showQRWindow=timestamp},
                        onDeleteClick = { gameToDelete = timestamp }
                    )
                }
            }
        }
    }
    if (gameToDelete != null) {
        AlertDialog(
            onDismissRequest = { gameToDelete = null },
            title = { Text("Spiel löschen") },
            text = { Text("Möchtest du dieses Spiel wirklich löschen?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteGame(gameToDelete!!)
                    gameToDelete = null
                }) {
                    Text("Löschen", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { gameToDelete = null }) {
                    Text("Abbrechen")
                }
            }
        )
    }
    if (showEditWindow != null) {
        val selectedTimestamp = showEditWindow!!
        val playersInGame = groupedGames[selectedTimestamp] ?: emptyList()

        // You can use a Basic Dialog or a Fullscreen Surface
        Dialog(
            onDismissRequest = { showEditWindow = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                EditGameScreen(
                    timestamp = selectedTimestamp,
                    players = playersInGame,
                    onDismiss = { showEditWindow = null },
                    viewModel = viewModel
                )
            }
        }
    }
    if (showQRWindow != null) {
        val selectedTimestamp = showQRWindow!!
        val playersInGame = groupedGames[selectedTimestamp] ?: emptyList()
        QrCodeDialog(csvData = viewModel.generateCsvForQrFromDB(playersInGame), onDismiss = { showQRWindow = null })
    }

}

@SuppressLint("SimpleDateFormat")
@Composable
fun GameHistoryCard(
    timestamp: Long,
    players: List<PlayerScoreEntry>,
    onClick: () -> Unit,
    onLongClick: () -> Unit = {},
    onDeleteClick: () -> Unit = {}
) {
    // Format the date/time
    val formattedDate = remember(timestamp) {
        val sdf = SimpleDateFormat("dd. MMM yyyy, HH:mm")
        sdf.format(timestamp)

    }

    Card(
        modifier = Modifier
            .fillMaxWidth().combinedClickable(onClick = onClick,
                onLongClick = onLongClick)
           ,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Event,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(

                        text = formattedDate,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Groups,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "${players.size} Spieler: ${players.joinToString { it.name }}",
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }

            Icon(
                Icons.Default.ChevronRight,
                contentDescription = "Details",
                tint = MaterialTheme.colorScheme.outline
            )

            IconButton(onClick = onDeleteClick) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Löschen",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                )
            }
        }
    }
}

