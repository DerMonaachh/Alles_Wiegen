package com.example.alleswiegen.screens

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.View
import android.view.ViewGroup
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.widget.FrameLayout
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.alleswiegen.GameUiState
import com.example.alleswiegen.GameViewModel
import com.example.alleswiegen.R
import com.example.alleswiegen.components.PlayerResultCard
import kotlin.collections.set
import kotlin.text.isNotBlank
import kotlin.text.toDoubleOrNull
import androidx.compose.material.icons.Icons

import androidx.compose.material.icons.filled.WineBar

import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.alleswiegen.components.HapticHelper

import com.example.alleswiegen.components.NfcScannerEffect
import androidx.core.net.toUri

import com.example.alleswiegen.components.NFCandBluetoothUI
import com.example.alleswiegen.components.WeiterOderEnde

import kotlin.text.isNotEmpty
import kotlin.text.lowercase


@Composable
fun PlayerWeightingScreen(viewModel: GameViewModel, uiState: GameUiState) {

    val currentWeights = uiState.currentWeights
    val liveWeight = viewModel.liveWeight
    var showExitDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val hapticHelper = remember { HapticHelper(context) }
    var showCelebrationVideo by remember { mutableStateOf(false) }
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    var lastErrorInfo by remember { mutableStateOf("") }
    // VALIDIERUNGSLOGIK
    val allInputsValid = uiState.players.all { player ->
        val weightStr = currentWeights[player.name] ?: ""
        val weightDouble = weightStr.toIntOrNull()
        // Check: Wert vorhanden && ist eine Zahl && Wert < vorheriges Gewicht
        weightDouble != null && weightDouble < player.currentWeight
    }
    if (uiState.nfcIsUsed){
        NfcScannerEffect { scannedId ->
            viewModel.onNfcTagDetected(scannedId)
            if (uiState.vibrate){
                hapticHelper.vibrate(50) // Kurz vibrieren
            }



        }
        // Auto-Zuweisung Logik:
        if (uiState.isBluetoothConnected && uiState.isWeightStable && liveWeight.isNotEmpty()) {
            val weightValue = liveWeight.toIntOrNull() ?: 0
            if (weightValue > 10 && !viewModel.lastScannedTagId.isNullOrEmpty() ) {
                val detectedPlayer = uiState.players.find { it.nfcId == viewModel.lastScannedTagId}
                if (detectedPlayer != null) {
                    // Wert eintragen
                    //currentWeights[detectedPlayer.name] = liveWeight
                    viewModel.updateWeight(liveWeight, detectedPlayer.name)

                    // Feedback
                    if (uiState.vibrate){
                        hapticHelper.vibrate(50) // Kurz vibrieren
                    }
                    lastErrorInfo="${detectedPlayer.name}s letzter Fehler: ${detectedPlayer.upcomingError(liveWeight.toInt())} g"
                    if (detectedPlayer.upcomingError(liveWeight.toIntOrNull() ?: 0) == 0 && uiState.showJingle) {
                        showCelebrationVideo = true
                    }
                    Toast.makeText(
                        context,
                        "${liveWeight} g für ${detectedPlayer.name } eingetragen.",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Kurz warten und dann ID zurücksetzen, damit man denselben Tag erneut scannen kann
                    viewModel.onNfcTagDetected("")
                }
            }
        }
    }
    if (isLandscape){
        Box(modifier = Modifier.fillMaxSize()) {
            Row(modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)) {
                Column(modifier = Modifier.weight(0.3f)) {
                    val detectedPlayer = uiState.players.find { it.nfcId == viewModel.lastScannedTagId }
                    if (uiState.isBluetoothConnected) {
                    // --- UI: KOMBINIERTE SCANNER CARD ---
                        NFCandBluetoothUI(uiState, viewModel, liveWeight, detectedPlayer)
                    }
                    if (lastErrorInfo.isNotBlank()) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.WineBar,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = lastErrorInfo,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                    WeiterOderEnde(viewModel, currentWeights, allInputsValid, showExitDialog, onExit = { showExitDialog = true })

                }
                Spacer(modifier = Modifier.width(8.dp))
                val effPlayers=if (uiState.sortByRank){      uiState.players.sortedBy { it.position }  }else  { uiState.players }
                LazyVerticalGrid(columns = androidx.compose.foundation.lazy.grid.GridCells.Fixed(2), modifier = Modifier.weight(0.7f),horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    items(effPlayers) { player ->
                        val weightStr = currentWeights[player.name] ?: ""
                        val weightDouble = weightStr.toIntOrNull() ?: 0
                        val isError = weightStr.isNotBlank() && weightDouble >= player.currentWeight

                        Box(modifier = Modifier.padding(vertical = 4.dp)) {
                            PlayerResultCard(
                                player = player,
                                liveWeight = liveWeight,
                                round = uiState.currentRound,
                                enteredValue = weightStr,
                                isError = isError, // Reiche den Fehlerstatus an die Card weiter
                                onCaptured = { value ->
                                    //currentWeights[player.name] = value
                                    viewModel.updateWeight(value, player.name)
                                    viewModel.onNfcTagDetected("")
                                    lastErrorInfo="${player.name}s letzter Fehler: ${player.upcomingError(value.toInt())} g"
                                    if (player.upcomingError(value.toIntOrNull() ?: 0) == 0 && uiState.showJingle) {
                                        showCelebrationVideo = true
                                    }
                                },
                                // Wir fügen ein Trailing-Content Slot in die Card ein oder legen das TextField darüber
                                inputField = {
                                    OutlinedTextField(
                                        value = weightStr,
                                        onValueChange = {
                                            //currentWeights[player.name] = it
                                            viewModel.updateWeight(it, player.name)
                                            viewModel.onNfcTagDetected("")
                                            val newValue = it.toIntOrNull() ?: -1
                                            lastErrorInfo="${player.name}s letzter Fehler: ${player.upcomingError(newValue)} g"
                                            if (player.upcomingError(newValue) == 0&& uiState.showJingle) {
                                                showCelebrationVideo = true
                                            }
                                        },
                                        label = { Text("Aktuell") },
                                        suffix = { Text("g") },
                                        isError = isError,
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.width(110.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        singleLine = true
                                    )
                                },landscape = true

                            )
                        }


                    }
                }

            }


        }
    }
    else{

    Box(modifier = Modifier.fillMaxSize()) {
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        val detectedPlayer = uiState.players.find { it.nfcId == viewModel.lastScannedTagId }
        if (uiState.isBluetoothConnected) {
// --- UI: KOMBINIERTE SCANNER CARD ---
            NFCandBluetoothUI(uiState, viewModel, liveWeight, detectedPlayer)
        }
        if (lastErrorInfo.isNotBlank()) {
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.WineBar,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = lastErrorInfo,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }




        // Anzeige eines Warnhinweises, falls Werte ungültig sind (optional aber hilfreich)

        if (!allInputsValid && currentWeights.isNotEmpty()) {
            Text(
                text = "Hinweis: Alle Gewichte müssen kleiner sein als der Startwert!",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        val effPlayers=if (uiState.sortByRank){      uiState.players.sortedBy { it.position }  }else  { uiState.players }
        LazyColumn(modifier = Modifier.weight(1f)) {
            items(effPlayers) { player ->

                    val weightStr = currentWeights[player.name] ?: ""
                    val weightDouble = weightStr.toIntOrNull() ?: 0
                    val isError = weightStr.isNotBlank() && weightDouble >= player.currentWeight

                    Box(modifier = Modifier.padding(vertical = 8.dp)) {
                        PlayerResultCard(
                            player = player,
                            liveWeight = liveWeight,
                            round = uiState.currentRound,
                            enteredValue = weightStr,
                            isError = isError, // Reiche den Fehlerstatus an die Card weiter
                            onCaptured = { value ->
                                //currentWeights[player.name] = value
                                viewModel.updateWeight(value, player.name)
                                viewModel.onNfcTagDetected("")
                                lastErrorInfo="${player.name}s letzter Fehler: ${player.upcomingError(value.toInt())} g"
                                if (player.upcomingError(value.toIntOrNull() ?: 0) == 0 && uiState.showJingle) {
                                    showCelebrationVideo = true
                                }
                            },
                            // Wir fügen ein Trailing-Content Slot in die Card ein oder legen das TextField darüber
                            inputField = {
                                OutlinedTextField(
                                    value = weightStr,
                                    onValueChange = {
                                        //currentWeights[player.name] = it
                                        viewModel.updateWeight(it, player.name)

                                        viewModel.onNfcTagDetected("")
                                        val newValue = it.toIntOrNull() ?: -1
                                        lastErrorInfo="${player.name}s letzter Fehler: ${player.upcomingError(newValue)} g"
                                        if (player.upcomingError(newValue) == 0&& uiState.showJingle) {
                                            showCelebrationVideo = true
                                        }
                                    },
                                    label = { Text("Aktuell") },
                                    suffix = { Text("g") },
                                    isError = isError,
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.width(110.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    singleLine = true
                                )
                            }
                        )
                    }
                }

        }

        WeiterOderEnde(viewModel, currentWeights, allInputsValid, showExitDialog, onExit = { showExitDialog = true })
    }
    }
    }
        // DAS VIDEO-OVERLAY
        if (showCelebrationVideo) {
            val context = LocalContext.current
            val audioUri = remember {
                Uri.parse("android.resource://${context.packageName}/${R.raw.bagel_audio}")
            }
            // Video-Auswahl Logik (bleibt gleich)
            val videoUri = remember {
                val folderPath = uiState.jingleLocation
                var selectedUri: Uri? = null

                if (folderPath.isNotBlank()) {
                    try {
                        val treeUri = folderPath.toUri()
                        // DocumentFile statt java.io.File verwenden
                        val pickedDir = androidx.documentfile.provider.DocumentFile.fromTreeUri(context, treeUri)

                        if (pickedDir != null && pickedDir.isDirectory) {
                            // Alle Dateien im Ordner auflisten
                            val files = pickedDir.listFiles()

                            val videoFiles = files.filter { file ->
                                val name = file.name?.lowercase() ?: ""
                                file.isFile && (
                                        name.endsWith(".mp4") ||
                                                name.endsWith(".mkv") ||
                                                name.endsWith(".3gp") ||
                                                name.endsWith(".mov")
                                        )
                            }

                            if (videoFiles.isNotEmpty()) {
                                selectedUri = videoFiles.random().uri
                            }
                        }
                    } catch (e: Exception) {
                        println("Fehler beim Laden des Verzeichnisses: ${e.message}")
                        e.printStackTrace()
                    }
                }
                selectedUri ?: "android.resource://${context.packageName}/${R.raw.bagel}".toUri()
            }

            // WICHTIG: Wir packen den Screen in ein Dialog-Fenster
            Dialog(
                onDismissRequest = { showCelebrationVideo = false },
                properties = DialogProperties(
                    usePlatformDefaultWidth = false, // Erlaubt echtes Vollbild
                    dismissOnBackPress = true
                )
            ) {
                // Der BagelScreen füllt nun das gesamte Fenster inkl. Titelzeile aus
                BagelScreen(videoUri = videoUri, audioUri = audioUri) {
                    showCelebrationVideo = false
                }
            }
        }


    // Der Bestätigungs-Dialog
    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Spiel beenden?") },
            text = { Text("Möchtest du die Runde wirklich beenden und zum Ranking gehen?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExitDialog = false
                        val weightMap = currentWeights.mapValues { it.value.toIntOrNull() ?: 0 }
                        viewModel.enterPlayerWeights(weightMap, true)
                    }
                ) {
                    Text("Beenden", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("Abbrechen")
                }
            }
        )
    }
}

@OptIn(UnstableApi::class)
@Composable
fun BagelScreen(videoUri: Uri, audioUri: Uri, onVideoFinished: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? Activity
    val audioManager = remember { context.getSystemService(Context.AUDIO_SERVICE) as AudioManager }

    // Fokus-Anfrage Definition (für Android 8.0+)
    val audioFocusRequest = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Hier explizit android.media.AudioAttributes verwenden!
            val attrs = android.media.AudioAttributes.Builder()
                .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MOVIE)
                .build()

            AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(attrs)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener { /* Optional */ }
                .build()
        } else null
    }

    val exoPlayer = remember(videoUri, audioUri) {
        ExoPlayer.Builder(context).build().apply {
            // Playlist erstellen: Erst Audio, dann Video
            setMediaItems(listOf(MediaItem.fromUri(audioUri), MediaItem.fromUri(videoUri)))
            prepare()
            playWhenReady = true
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    if (state == Player.STATE_ENDED) onVideoFinished()
                }

                override fun onPlayerError(error: PlaybackException) {
                    onVideoFinished()
                }
            })
        }
    }

    DisposableEffect(Unit) {
        // --- AUDIO FOKUS ANFORDERN (Pausiert Spotify) ---
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let { audioManager.requestAudioFocus(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                null,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN
            )
        }

        // System-Bars verstecken (Vollbild)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity?.window?.let { window ->
                val controller = window.insetsController
                controller?.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller?.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            activity?.window?.decorView?.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    )
        }

        onDispose {
            // --- AUDIO FOKUS FREIGEBEN (Spotify spielt weiter) ---
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
            } else {
                @Suppress("DEPRECATION")
                audioManager.abandonAudioFocus(null)
            }

            // Bars wieder einblenden
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                activity?.window?.insetsController?.show(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
            } else {
                @Suppress("DEPRECATION")
                activity?.window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_VISIBLE
            }
            exoPlayer.release()
        }
    }

    // Box & AndroidView bleibt gleich wie in deinem Code...
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}
/*
@OptIn(UnstableApi::class)
@Composable
fun BagelScreen(videoUri: Uri,audioUri: Uri, onVideoFinished: () -> Unit) {
    val context = LocalContext.current
    val activity = context as? Activity


    val exoPlayer = remember(videoUri) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItems(listOf(MediaItem.fromUri(audioUri), MediaItem.fromUri(videoUri)))
            prepare()
            playWhenReady = true
            addListener(object : Player.Listener {
                override fun onPlaybackStateChanged(state: Int) {
                    if (state == Player.STATE_ENDED) onVideoFinished()
                }

                override fun onPlayerError(error: PlaybackException) {
                    onVideoFinished() // Bei Fehler schließen
                }
            })
        }
    }

    // System-Leisten verstecken (Vollbild)
    DisposableEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity?.window?.let { window ->
                val controller = window.insetsController
                controller?.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller?.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            activity?.window?.decorView?.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_FULLSCREEN
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    )
        }

        onDispose {
            // Leisten wieder einblenden beim Schließen
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                activity?.window?.insetsController?.show(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
            } else {
                @Suppress("DEPRECATION")
                activity?.window?.decorView?.systemUiVisibility =
                    View.SYSTEM_UI_FLAG_VISIBLE
            }
            exoPlayer.release()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        AndroidView(
            factory = { ctx ->
                PlayerView(ctx).apply {
                    player = exoPlayer
                    useController = false // Keine Play-Buttons etc. einblenden
                    resizeMode =
                        AspectRatioFrameLayout.RESIZE_MODE_ZOOM // Füllt den ganzen Bildschirm
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}*/