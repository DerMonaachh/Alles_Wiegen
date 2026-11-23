package com.example.alleswiegen

import android.R
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.content.Context

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlin.math.abs
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.isEmpty
import androidx.core.content.edit

import androidx.lifecycle.viewModelScope
import com.example.alleswiegen.components.EventData
import com.example.alleswiegen.server.LocalWebServer
import com.example.alleswiegen.utils.NetworkUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.collections.map

class GameViewModel (private val repository: PlayerScoreRepository) : ViewModel() {


    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()
    private val timeRange = MutableStateFlow(
        (System.currentTimeMillis() - (30 * 24 * 60 * 60*1000 )) to System.currentTimeMillis()  )



    val currenWeights = mutableMapOf<String, String>()
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val filteredScores: StateFlow<List<PlayerScoreEntry>> = timeRange
        .flatMapLatest { range ->
            repository.getScoresForRange(range.first, range.second)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    val allScores: StateFlow<List<PlayerScoreEntry>> = repository.allScores
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    // Inside GameViewModel
    val allEvents: StateFlow<List<EventEntry>> = repository.getAllEvents().map { list ->
        val hasAllTime = list.any { it.name == "All Time" }
        if (!hasAllTime) {
            listOf(
                EventEntry(
                    id = 0,
                    name = "All Time",
                    startTimestamp = 0,
                    endTimestamp = Long.MAX_VALUE
                )
            ) + list
        } else {
            list
        }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun clearDatabasePlayerScore() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }

    fun saveEventWithLong(entry: EventData) {
        if (entry.startDate != null && entry.endDate != null) {
            // Your existing timestamp conversion
            val startDate = entry.startDate
            val endDate = entry.endDate

            // Upsert Logic: Check if an event with this name already exists
            val existingEvent = allEvents.value.find { it.name == entry.name }

            val eventEntry = EventEntry(
                // If it exists, use its ID to update it. If not, use 0 to insert new.
                id = existingEvent?.id ?: 0,
                name = entry.name,
                startTimestamp = startDate,
                endTimestamp = endDate
            )

            viewModelScope.launch {
                repository.upsertEvent(eventEntry)
            }
        }
    }


    fun saveGameResult(entry: PlayerScoreEntry) {
        viewModelScope.launch {
            repository.insert(entry)
        }
    }

    private var bluetoothService: BluetoothService? = null
    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private val webServer = LocalWebServer()

    var liveWeight by mutableStateOf("0.0")
        private set
    var rawDataString by mutableStateOf("")
        private set
    var lastScannedTagId by mutableStateOf<String?>(null)
    fun onNfcTagDetected(tagId: String) {
        lastScannedTagId = tagId
        _uiState.update { it.copy(nfcIsUsed = true) }
    }
    fun setTimeFilter(daysAgo: Int) {
        val start = System.currentTimeMillis() - (daysAgo * 24 * 60 * 60 * 1000)
        val end = System.currentTimeMillis()  + (4 * 60 * 60 * 1000)
        timeRange.value = start to end
    }
    fun setTimeFilterwithDates(start: Long, end: Long) {
        //val start = ((startL / 1000) - 1704067200L).toInt()
       // val end = ((endL / 1000) - 1704067200L).toInt() + (4 * 60 * 60 * 1000)
        timeRange.value = start to end
    }
    fun setNfcUsed(used: Boolean) {
        _uiState.update { currentState ->
            currentState.copy(nfcIsUsed = used)
        }
    }
    fun assignTagToPlayer(playerName: String, tagId: String) {
        _uiState.update { currentState ->
            val updatedPlayers = currentState.players.map { player ->
                if (player.name == playerName) {
                    player.copy(nfcId = tagId)
                } else {
                    player
                }
            }
            currentState.copy(players = updatedPlayers)
        }
    }
    init {
        viewModelScope.launch {
            // Wir nehmen den ersten emittierten Wert der Liste
            val currentEvents = repository.getAllEvents().first()
            if (currentEvents.isEmpty()) {
                repository.upsertEvent(
                    EventEntry(
                        id = 0, // Room generiert die ID oder nutzt 0 als neuen Eintrag
                        name = "All Time",
                        startTimestamp = 0,
                        endTimestamp = Long.MAX_VALUE
                    )
                )
            }
        }
        viewModelScope.launch {
            uiState.collect { state ->
                if (webServer.isRunning) {
                    webServer.broadcastJson(buildLiveGameJson(state))
                }
            }
        }
    }
    fun setupBluetooth(context: Context) {
        if (bluetoothService == null) {
            bluetoothService = BluetoothService(context, bluetoothAdapter)
        }
    }

    fun toggleExenByNull(enabled: Boolean) {
        _uiState.update { it.copy(exenByNull = enabled) }
    }
    fun toggleSortByRand(enabled: Boolean, context: Context? = null) {
        _uiState.update { it.copy(sortByRank = enabled) }
        context?.let { ctx ->
            val prefs = ctx.getSharedPreferences("AllesWiegenPrefs", android.content.Context.MODE_PRIVATE)
            prefs.edit {
                putString("sort_by_rank", enabled.toString())
            }
        }
    }
    fun toggleHidePlacement(enabled: Boolean, context: Context? = null) {
        _uiState.update { it.copy(hidePlacement = enabled) }
        context?.let { ctx ->
            val prefs = ctx.getSharedPreferences("AllesWiegenPrefs", android.content.Context.MODE_PRIVATE)
            prefs.edit {
                putString("hide_placement", enabled.toString())
            }
        }
    }
    fun toggleJingle(enabled: Boolean,context: Context) {
        _uiState.update { it.copy(showJingle =  enabled) }
        val prefs = context.getSharedPreferences("AllesWiegenPrefs", android.content.Context.MODE_PRIVATE)
        prefs.edit {
            putString("jingle", enabled.toString())
        }
    }
    fun toggleVibration(enabled: Boolean,context: Context) {
        _uiState.update { it.copy(vibrate = enabled) }
        val prefs = context.getSharedPreferences("AllesWiegenPrefs", android.content.Context.MODE_PRIVATE)
        prefs.edit {
            putString("vibration", enabled.toString())
        }
    }
    fun updateJinglePath(urlLocation:String,context: Context){
        _uiState.update { it.copy(jingleLocation = urlLocation) }
        val prefs = context.getSharedPreferences("AllesWiegenPrefs", android.content.Context.MODE_PRIVATE)
        prefs.edit {
            putString("jingleLocation", urlLocation)
        }
    }





    fun updateLiveWeight(data: String) {
        val cleaned = data.filter { it.isDigit() || it == '.' || it == '-' }
        if (cleaned.isNotEmpty()) {
            liveWeight = cleaned
            // Wenn ein Spieler gerade aktiv gewogen wird, aktualisiere dessen Wert im UI State
            _uiState.value.activeBluetoothPlayer?.let { playerName ->
                updatePlayerWeightInput(playerName, cleaned)
            }
        }
    }

    fun updatePlayerWeightInput(playerName: String, weight: String) {
        _uiState.update { currentState ->
            val newWeights = currentState.currentWeights.toMutableMap()
            newWeights[playerName] = weight
            currentState.copy(currentWeights = newWeights)
        }
    }

    fun setActiveBluetoothPlayer(playerName: String?) {
        _uiState.update { it.copy(activeBluetoothPlayer = playerName) }
    }


    @SuppressLint("MissingPermission")
    fun connectBluetooth() {
        bluetoothService?.connect(
            address = _uiState.value.bluetoothAddress,
            // Hier greifen wir jetzt alle drei Parameter ab: weight, stable, hex
            onWeightReceived = { weight, stable, hex ->
                rawDataString = hex // Rohdaten für das UI speichern

                // 1. Das Live-Gewicht updaten
                updateLiveWeight(weight.toString())

                // 2. Den Stabilitätsstatus im UI State speichern
                _uiState.update { it.copy(isWeightStable = stable) }
            },
            onStatusChanged = { isConnected ->
                _uiState.update { it.copy(isBluetoothConnected = isConnected) }
            },
            serviceUuid = _uiState.value.SERVICE_UUID,
            characteristicUuid = _uiState.value.CHARACTERISTIC_UUID
        )
    }

    fun startBluetoothScan(context: Context) {
        setupBluetooth(context)
        _uiState.update {
            it.copy(
                isScanningBluetooth = true,
                discoveredBluetoothDevices = emptyList()
            )
        }
        bluetoothService?.startScan(
            onDeviceDiscovered = { device ->
                _uiState.update { current ->
                    if (current.discoveredBluetoothDevices.none { it.address == device.address }) {
                        current.copy(
                            discoveredBluetoothDevices = current.discoveredBluetoothDevices + device
                        )
                    } else {
                        current
                    }
                }
            },
            onScanFinished = {
                _uiState.update { it.copy(isScanningBluetooth = false, discoveredBluetoothDevices = emptyList()) }
            }
        )
    }

    fun stopBluetoothScan() {
        bluetoothService?.stopScan()
        _uiState.update { it.copy(isScanningBluetooth = false, discoveredBluetoothDevices = emptyList()) }
    }

    fun selectDiscoveredDevice(context: Context, device: DiscoveredBluetoothDevice) {
        stopBluetoothScan()
        updateBluetoothAddress(context, device.address)
        if (device.name.isNotBlank() && device.name != "Unbekanntes Gerät") {
            saveScale(context, device.name, device.address)
        }
    }

    override fun onCleared() {
        super.onCleared()
        bluetoothService?.stopScan()
        bluetoothService?.stop()
        webServer.stop()
    }

    fun startServer(context: Context) {
        if (!NetworkUtils.isWifiConnected(context)) {
            _uiState.update { it.copy(showWifiErrorDialog = true) }
            return
        }

        val ip = NetworkUtils.getWifiIpAddress(context) ?: "127.0.0.1"
        val port = 8080
        val url = "http://$ip:$port"

        webServer.start(context, port) {
            broadcastLiveGameJson()
        }

        _uiState.update {
            it.copy(
                isServerRunning = true,
                serverIpUrl = url,
                showServerQrDialog = true,
                showWifiErrorDialog = false
            )
        }
        broadcastLiveGameJson()
    }

    fun stopServer() {
        webServer.stop()
        _uiState.update {
            it.copy(
                isServerRunning = false,
                serverIpUrl = "",
                showServerQrDialog = false
            )
        }
    }

    fun dismissWifiErrorDialog() {
        _uiState.update { it.copy(showWifiErrorDialog = false) }
    }

    fun dismissServerQrDialog() {
        _uiState.update { it.copy(showServerQrDialog = false) }
    }

    fun showServerQrDialog() {
        _uiState.update { it.copy(showServerQrDialog = true) }
    }

    fun broadcastLiveGameJson() {
        if (webServer.isRunning) {
            webServer.broadcastJson(buildLiveGameJson())
        }
    }

    fun buildLiveGameJson(state: GameUiState = uiState.value): String {
        val spielBeendet = state.gameEnds || state.currentScreen == Screen.RANKING

        data class TempPlayerStats(
            val id: Int,
            val name: String,
            val totalDrunk: Int,
            val personalTarget: Int,
            val totalError: Int,
            val lastError: Int,
            val storedPosition: Int
        )

        val liveStatsList = state.players.mapIndexed { index, player ->
            val weightStr = state.currentWeights[player.name]
            val enteredWeight = weightStr?.toIntOrNull()

            val displayDrunk: Int
            val displayAbsError: Int
            val displayLastError: Int

            if (enteredWeight != null && enteredWeight >= 0) {
                val drunkThisRound = (player.currentWeight - enteredWeight).coerceAtLeast(0)
                val upcomingLastError = drunkThisRound - player.personalTarget
                val rawError = abs(upcomingLastError)
                val errorThisRound = (rawError * player.nextErrorMultiplier).toInt()

                displayDrunk = player.totalDrunk + drunkThisRound
                displayLastError = upcomingLastError
                displayAbsError = player.totalError + errorThisRound
            } else {
                displayDrunk = player.totalDrunk
                displayLastError = player.lastError
                displayAbsError = player.totalError
            }

            TempPlayerStats(
                id = index + 1,
                name = player.name,
                totalDrunk = displayDrunk,
                personalTarget = player.personalTarget,
                totalError = displayAbsError,
                lastError = displayLastError,
                storedPosition = if (player.position > 0) player.position else (index + 1)
            )
        }

        val hasAnyEnteredWeight = state.currentWeights.values.any { it.toIntOrNull() != null }
        val sortedStats = if (hasAnyEnteredWeight) {
            liveStatsList.sortedBy { it.totalError }
        } else {
            liveStatsList.sortedBy { it.storedPosition }
        }

        val playersJson = sortedStats.mapIndexed { rankIndex, stats ->
            val pos = if (hasAnyEnteredWeight) (rankIndex + 1) else stats.storedPosition
            """
            {
              "id": ${stats.id},
              "name": "${escapeJson(stats.name)}",
              "position": $pos,
              "gesamtGetrunken": "${stats.totalDrunk} g",
              "persoenlichesZiel": "${stats.personalTarget} g",
              "absoluterFehler": "${stats.totalError} g",
              "letzterFehler": "${stats.lastError} g"
            }
            """.trimIndent()
        }.joinToString(separator = ",\n")

        return """
        {
          "spielBeendet": $spielBeendet,
          "spieler": [
            $playersJson
          ]
        }
        """.trimIndent()
    }

    private fun escapeJson(text: String): String {
        return text.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\b", "\\b")
            .replace("\u000C", "\\f")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }

    fun addPlayer(name: String) {
        if (name.isBlank() || _uiState.value.players.any { it.name == name }) return
        _uiState.update { currentState ->
            currentState.copy(
                players = currentState.players + Player(name = name)
            )
        }
    }

    fun removePlayer(name: String) {
        _uiState.update { currentState ->
            currentState.copy(
                players = currentState.players.filter { it.name != name }
            )
        }
    }

    fun navigateTo(nextScreen: Screen) {
        clearWeights()
        _uiState.update { currentState ->
            currentState.copy(
                previousScreen = currentState.currentScreen, // Den alten Screen merken
                currentScreen = nextScreen
            )
        }
    }

    fun startInitialWeighing(weights: Map<String, Int>) {
        _uiState.update { currentState ->
            val updatedPlayers = currentState.players.map { player ->
                val initialWeight = weights[player.name] ?: 0
                player.copy(currentWeight = initialWeight)
            }
            clearWeights()
            currentState.copy(
                players = updatedPlayers,
                currentScreen = Screen.SET_TARGET,
                        previousScreen = currentState.currentScreen,
            )
        }
    }

    fun enterNewTargets(targetAmount: Int) {
        val mostDrunkPlayer=getLeaderForNextRound()
        val mostDrunk= mostDrunkPlayer.totalDrunk
        _uiState.update { currentState ->
            val newTotalRequest = mostDrunk+targetAmount
            val updatedPlayers = currentState.players.map { player ->
                player.copy(

                    personalTarget = newTotalRequest-player.totalDrunk
                )
            }
            println("7777777777777777777777777777${currentState.previousScreen }")
            val infoUpdate = if (currentState.previousScreen != Screen.WEIGH_AFTER_EXXEN){
                generateInfoText(updatedPlayers,targetAmount,currentState=currentState).first
            }
            else
            {
                generateInfoText(updatedPlayers,targetAmount,true,currentState=currentState).first
            }


            clearWeights()
            currentState.copy(
                players = updatedPlayers,
                currentRound = currentState.currentRound + 1,
                previousScreen = currentState.currentScreen,
                currentScreen = Screen.PlAYER_WEIGHING,
                requested = targetAmount,
                info = infoUpdate,

            )
        }
    }
    fun stroePlayerScoreAtGameEnd(playersToStore: List<Player>){
        val time : Long = System.currentTimeMillis()
        _uiState.update { it.copy(lastStoredGameTime = time) }
        playersToStore.forEach { player ->
            println("Store Data for ${player.name} at time ${time}")
            val score= PlayerScoreEntry(name=player.name,
                totalErrorClassic = player.totalError,
                totalErrorWithoutExer = player.regularError(),
                playerCount = playersToStore.size,
                averageError = player.averageError(),
                classicPosition = playersToStore.getRankOf(player),
                withoutExerPosition = playersToStore.getRankOf(player, byRegularError = true),
                sipCount = player.sipsErrors.size,
                sipCountWithoutExer = player.reuglarCount(),
                timestamp = time,
                bagelCount = player.getBagelCount(),
                gameMode = _uiState.value.gameMode.toInt())



            saveGameResult(score)
            println("Stored Data")

        }

        println("Stored Data fnished")


    }
    @SuppressLint("DefaultLocale")
    fun generateCsvForQr(): String {
        val time = uiState.value.lastStoredGameTime
        val sb = StringBuilder()

        // Optional: Add Header (removes ambiguity but takes up space)
        sb.append("time, ${time}\n")
        sb.append("Name,ErrClas,RegEx,AvgErr,Rank,ExRank,Sips,SipswoEx,Bagels,gameMode\n")

        uiState.value.players.forEach { player ->
            val data = listOf(
                player.name,
                player.totalError,
                player.regularError(),
                String.format(java.util.Locale.US, "%.2f", player.averageError()), // Formatting to 2 decimals to save space
                uiState.value.players.getRankOf(player),
                uiState.value.players.getRankOf(player, byRegularError = true),
                player.sipsErrors.size,
                player.reuglarCount(),
                player.getBagelCount(),
                _uiState.value.gameMode.toInt()
            )
            sb.append(data.joinToString(separator = ","))
            sb.append("\n") // New line for next player
        }
        return sb.toString()
    }
    @SuppressLint("DefaultLocale")
    fun generateCsvForQrFromDB(players:List<PlayerScoreEntry>): String {
        if (players.isEmpty()) {return ""}
        else {
            val sb = StringBuilder()
            val time =players[0].timestamp



        // Optional: Add Header (removes ambiguity but takes up space)
        sb.append("time, ${time}\n")
        sb.append("Name,ErrClas,RegEx,AvgErr,Rank,ExRank,Sips,SipswoEx,Bagels,GameMode\n")

        players.forEach { player ->
            val data = listOf(
                player.name,
                player.totalErrorClassic,
                player.totalErrorWithoutExer,
                String.format(java.util.Locale.US, "%.2f", player.averageError), // Formatting to 2 decimals to save space
                player.classicPosition,
                player.withoutExerPosition,
                player.sipCount,
                player.sipCountWithoutExer,
                player.bagelCount,
                _uiState.value.gameMode.toInt()
            )
            sb.append(data.joinToString(separator = ","))
            sb.append("\n") // New line for next player
        }
        return sb.toString()
        }
    }

    @SuppressLint("DefaultLocale")
    fun generateCsvForAllGames(): String {
        val scores = allScores.value
        if (scores.isEmpty()) return ""

        val grouped = scores.groupBy { it.timestamp }.toSortedMap(compareByDescending { it })
        val sb = StringBuilder()

        grouped.forEach { (_, players) ->
            sb.append(generateCsvForQrFromDB(players))
            sb.append("\n") // Optional: extra newline between games
        }
        return sb.toString()
    }

    fun importCsvData(csvData: String): String {
        if (csvData.isBlank()) return "Datei ist leer"

        // We split by the "time," marker to separate multiple games
        val gameBlocks = csvData.split("time,")
            .filter { it.isNotBlank() }
            .map { "time," + it.trim() }

        var importedCount = 0
        var duplicateCount = 0
        val sessionTimestamps = mutableSetOf<Long>()

        gameBlocks.forEach { block ->
            val lines = block.lines()
            if (lines.size >= 3) {
                val timeRow = lines[0].split(",")
                val timestamp = timeRow.getOrNull(1)?.trim()?.toLongOrNull() ?: 0L

                // Check if we already processed this timestamp in THIS session
                // to handle duplicates within the same CSV file
                if (sessionTimestamps.contains(timestamp)) {
                    duplicateCount++
                } else {
                    val result = parseCsvFromQr(block)
                    when {
                        result.startsWith("Hat geklappt") -> {
                            importedCount++
                            sessionTimestamps.add(timestamp)
                        }
                        result.startsWith("Duplikat") -> {
                            duplicateCount++
                        }
                    }
                }
            }
        }

        return if (importedCount == 0 && duplicateCount == 0) {
            "Keine validen Spieldaten gefunden"
        } else {
            "Importiert: $importedCount Spiele, Übersprungen: $duplicateCount Duplikate"
        }
    }


    fun parseCsvFromQr(csvData: String): String {
        val lines = csvData.trim().lines()
        if (lines.size < 3) return "Zu wenig Reihen" // Needs at least Time, Header, and 1 Player

        // 1. Extract the shared timestamp from the first line (e.g., "time, 12345678")
        val timeRow = lines[0].split(",")
        val extractedTime = timeRow.getOrNull(1)?.trim()?.toLongOrNull() ?: 0

        // Duplicate check: Verify if this game already exists in the database
        if (allScores.value.any { it.timestamp == extractedTime }) {
            return "Duplikat: Spiel mit Zeitstempel $extractedTime existiert bereits"
        }

        var anyIssue=""
        // 2. Map the player lines (skipping line 0 (time) and line 1 (header))
        val playerLines = lines.drop(2).filter { it.isNotBlank() }
        val players = playerLines.map { line ->
            val parts = line.split(",")
            if (parts.size >= 10) {
                try {
                    PlayerScoreEntry(
                        name = parts[0],
                        totalErrorClassic = parts[1].toInt(),
                        totalErrorWithoutExer = parts[2].toInt(),
                        averageError = parts[3].toDouble(),
                        classicPosition = parts[4].toInt(),
                        withoutExerPosition = parts[5].toInt(),
                        sipCount = parts[6].toInt(),
                        sipCountWithoutExer = parts[7].toInt(),
                        bagelCount = parts[8].toInt(),
                        timestamp = extractedTime, // Use the time from the top
                        playerCount = playerLines.size, // Total players is total player lines
                        gameMode = parts[9].toInt()

                    )
                } catch (e: Exception) {
                    return "$e"
                }
            } else {
                return "Zeile zu klein"
            }
        }

        if (anyIssue.isNotEmpty()){
            return anyIssue
        }
        players.forEach {player -> saveGameResult(player) }
        return "Hat geklappt ${players.size} Spieler"
    }
    fun reassignInfo(text:String){

        _uiState.update { currentState ->
            currentState.copy(
                info = text
            )
        }
    }
    fun enterPlayerWeights(actualWeights: Map<String, Int>, ende: Boolean) {
        var playersToSave: List<Player>? = null
        var shouldGetItems = false
        _uiState.update { currentState ->
            val updatedPlayers = currentState.players.map { player ->
                val oldWeight = player.currentWeight
                val newWeight = actualWeights[player.name] ?: oldWeight
                val drunkAmount = oldWeight - newWeight
                val rawError = abs(drunkAmount - player.personalTarget)
                val error = (rawError * player.nextErrorMultiplier).toInt()
                //print("${player.name} ${error}")

                player.copy(
                    totalDrunk = player.totalDrunk + drunkAmount,
                    totalError = player.totalError + error,
                    currentWeight = newWeight,
                    lastError = drunkAmount - player.personalTarget,
                    sipsErrors = player.sipsErrors + SipError(error, false),
                    nextErrorMultiplier = 1.0 ,// Reset after use
                    itemHit = 0
                )
            }

            // Wir rufen makeInfo mit den neuen Daten auf, BEVOR wir den State kopieren
            val infoUpdate = generateInfoText(updatedPlayers, currentState = currentState)

            val nextScreen = when {
                infoUpdate.second > 0 -> Screen.PICK_EXXER // Wenn jemand exxen muss
                ende -> Screen.RANKING                    // Wenn Spielende gedrückt wurde
                currentState.gameMode == GameMode.Items -> Screen.ITEM_HANDLING
                else -> Screen.SHOW_BEST_AND_WORST                // Normaler Spielverlauf
            }


            // Jetzt sortieren wir auch direkt hier für das Ranking
            val playersSortedByError = updatedPlayers.sortedBy { it.totalError }
            val rankedPlayers = updatedPlayers.map { player ->
                val rank = playersSortedByError.indexOfFirst { it.name == player.name } + 1
                player.copy(position = rank)
            }

            if (nextScreen == Screen.RANKING) {
                playersToSave = rankedPlayers
            }
            if (nextScreen == Screen.ITEM_HANDLING) {
                shouldGetItems = true
            }

            currentState.copy(
                players = rankedPlayers,
                currentScreen = nextScreen,
                previousScreen = currentState.currentScreen,
                info = infoUpdate.first,
                playerNeedToEx = infoUpdate.second,
                gameEnds = ende
            )
        }
        if (shouldGetItems) getItems()
        playersToSave?.let {
            stroePlayerScoreAtGameEnd(it)
        }
    }

    // Hilfsfunktion, die die Logik berechnet ohne den State direkt zu lesen
    private fun generateInfoText(currentPlayers: List<Player>,targetAmount: Int =0,short: Boolean=false,currentState: GameUiState): Pair<String, Int> {
        val sb = StringBuilder()
        var exxer = 0
        //sb.append("Ziel: ${targetAmount}\n")
        if (currentState.currentRound>0 &&  !short){
            // 1. Schluckspechte (Höchster Fehler)
            val maxVal = currentPlayers.maxOfOrNull { abs(it.lastError) } ?: 0
            val badGuys = currentPlayers.filter { abs(it.lastError) == maxVal }
            val perfectOnes = currentPlayers.filter { it.lastError == 0 }

            if  (!(perfectOnes.isNotEmpty() and currentState.exenByNull)){
                if (badGuys.isNotEmpty()) {
                    val names = badGuys.joinToString(" und ") { it.name }
                    val verb = if (badGuys.size > 1) "müssen" else "muss"
                    sb.append("$names $verb einen Strafschluck trinken. \n")
                }
            }

            val almostGood=currentPlayers.filter { abs(it.lastError) == 1 }
            // 2. Perfekte / Pussys


            if (perfectOnes.isNotEmpty()) {
                if (currentState.exenByNull) {
                    val names = perfectOnes.joinToString(" und ") { it.name }
                    val verb = if (perfectOnes.size > 1) "dürfen" else "darf einen"
                    sb.append("$names $verb Kandidaten für Bagel auswählen!\n")

                    exxer = perfectOnes.size
                } else {
                    sb.append("Alle trinken außer: ${perfectOnes.joinToString(", ") { it.name }}!")
                }
            } else {
                val minVal = currentPlayers.minOfOrNull { abs(it.lastError) } ?: 0
                val goodGuys = currentPlayers.filter { abs(it.lastError )== minVal }
                if (goodGuys.isNotEmpty()) {
                    val names = goodGuys.joinToString(" und ") { it.name }
                    if (minVal==1){
                        sb.append("Alle trinken außer: ${almostGood.joinToString(", ") { it.name }}!")
                    }else{
                        val verb =
                            if (goodGuys.size > 1) "dürfen einen Schluck verteilen." else "darf einen Schluck verteilen."
                        sb.append("$names $verb")
                    }
                    }

            }

        }

        return Pair(sb.toString(), exxer)
    }
    fun pickExxer(name: String) {
        clearWeights()
        _uiState.update { it.copy(exxer = name, currentScreen = Screen.WEIGH_BEFORE_EXXEN) }
    }
    fun GoToNextTarget(){
        _uiState.update { currentState ->
        currentState.copy(

            currentScreen = Screen.SET_TARGET,
            previousScreen = currentState.currentScreen,


        )}
    }
    fun updatePlayerScore(score: PlayerScoreEntry) {
        viewModelScope.launch {
            repository.update(score)
        }
    }

    fun deleteGame(timestamp: Long) {
        viewModelScope.launch {
            repository.deleteGame(timestamp)
        }
    }

    // Inside GameViewModel.kt
// Function to save the current name and address to SharedPreferences
    fun saveScale(context: Context, name: String, address: String) {
        val sharedPref = context.getSharedPreferences("SavedScales", Context.MODE_PRIVATE)
        sharedPref.edit {
            putString(name, address)
        }
    }

    fun deleteScale(context: Context, name: String) {
        val sharedPref = context.getSharedPreferences("SavedScales", Context.MODE_PRIVATE)
        sharedPref.edit {
            remove(name)
        }
    }

    fun getSavedScales(context: Context): Map<String, String> {
        val sharedPref = context.getSharedPreferences("SavedScales", Context.MODE_PRIVATE)
        val allEntries = sharedPref.all ?: return emptyMap()

        // Filter only those entries where the value is actually a String
        return allEntries.filterValues { it is String } as Map<String, String>
    }

    fun getItems() {
        _uiState.update { currentState ->
            val allowedItems = AvailableItems.items.filter { it.id in currentState.erlaubteItems }
            if (allowedItems.isEmpty()) return@update currentState

            val itemsCount = currentState.itemsProRunde
            if (itemsCount <= 0) return@update currentState

            val players = currentState.players
            if (players.isEmpty()) return@update currentState

            // 1. Determine priority players based on distro mode
            val sortedByRank = players.sortedBy { it.position }
            val sortedByLastError=players.sortedBy { it.lastError }
            val prioritizedPlayers = mutableListOf<Player>()

            when (currentState.itemVerteilung) {
                ItemDistro.letzer.displayName -> {
                    prioritizedPlayers.add(sortedByRank.last())
                }
                ItemDistro.erster.displayName -> {
                    prioritizedPlayers.add(sortedByRank.first())
                }
                ItemDistro.runden_erster.displayName -> {
                    prioritizedPlayers.add(sortedByLastError.first())
                }
                ItemDistro.runden_letzter.displayName -> {
                    prioritizedPlayers.add(sortedByLastError.last())
                }
                // gleichmasig / default: no priority
            }

            // 2. Select players to receive items (max 1 per player, up to itemsProRunde)
            val selectedPlayers = mutableSetOf<String>()
            val playersToReceive = mutableListOf<Player>()

            // Add prioritized player first
            prioritizedPlayers.forEach { p ->
                if (selectedPlayers.size < itemsCount && selectedPlayers.add(p.name)) {
                    playersToReceive.add(p)
                }
            }

            // Fill remaining slots randomly
            val remainingPlayers = players.filter { it.name !in selectedPlayers }.shuffled()
            for (p in remainingPlayers) {
                if (selectedPlayers.size < itemsCount) {
                    selectedPlayers.add(p.name)
                    playersToReceive.add(p)
                } else {
                    break
                }
            }

            // 3. Assign items to selected players based on rarity
            // rarity 10 is common, rarity 60 is rare. Weight = 1.0 / rarity
            val weightedItems = allowedItems.map { it to (it.rarity.coerceAtLeast(1)) }
            val totalWeight = weightedItems.sumOf { it.second }

            val updatedPlayers = players.map { player ->
                if (player.name in selectedPlayers) {
                    var random = Math.random() * totalWeight
                    var selectedItem = allowedItems.first()
                    for ((item, weight) in weightedItems) {
                        random -= weight
                        if (random <= 0) {
                            selectedItem = item
                            if (player.position==1 && item.id==2){//Blue shell
                                selectedItem= AvailableItems.items[6]
                            }
                            break
                        }
                    }
                    player.copy(items = player.items + selectedItem)
                } else {
                    player
                }
            }

            currentState.copy(
                players = updatedPlayers,
                info = "Klick einen Spieler mit ausführbarem Item an um das Item zu nutzen."
            )
        }
    }
    fun handleItem(triggerName: String, victimName: String? = null) {
        var extraInfo = ""
        _uiState.update { currentState ->
            val trigger = currentState.players.find { it.name == triggerName } ?: return@update currentState
            val usedItem = trigger.items.firstOrNull() ?: return@update currentState
            val victim = if (victimName != null) currentState.players.find { it.name == victimName } else null

            val updatedPlayers = currentState.players.map { player ->
                var updatedPlayer = player
                val leader = currentState.players.find { it.position == 1 }

                // 1. Apply item effect to victim (Blue Shell case)
                when (usedItem.id) {
                    1 -> { // Wein Panzer
                        if (victim != null && player.name == victim.name) {
                            updatedPlayer = updatedPlayer.copy(
                                totalError = updatedPlayer.totalError + usedItem.wert,
                            )
                        }
                    }
                    2 -> { // Blue Shell
                        if (leader != null && player.name == leader.name) {
                            updatedPlayer = updatedPlayer.copy(
                                totalError = updatedPlayer.totalError + usedItem.wert,
                                nextErrorMultiplier = usedItem.multiplikator,
                                itemHit = 2
                            )
                            extraInfo = "${leader.name} wurde getroffen."
                        }
                    }
                    3 -> { // Pilz
                        if (player.name == trigger.name) {
                            val maxErrorEntry = updatedPlayer.sipsErrors.maxByOrNull { it.amount }
                            if (maxErrorEntry != null) {
                                val maxErrorValue = maxErrorEntry.amount
                                var found = false
                                val newSipsErrors = updatedPlayer.sipsErrors.map {
                                    if (!found && it == maxErrorEntry) {
                                        found = true
                                        it.copy(amount = 0)
                                    } else {
                                        it
                                    }
                                }
                                updatedPlayer = updatedPlayer.copy(
                                    totalError = updatedPlayer.totalError - maxErrorValue,
                                    sipsErrors = newSipsErrors
                                )
                                extraInfo = "Dein Fehler wurde um ${maxErrorValue} reduziert."
                            }
                        }
                    }
                    4 -> { // Blitz
                        if (trigger.name != player.name) {
                            updatedPlayer = updatedPlayer.copy(
                                totalError = updatedPlayer.totalError + usedItem.wert,
                            )
                        }
                    }
                    5 -> { // Blitz (Blume)
                        if (trigger.name == player.name) {
                            updatedPlayer = updatedPlayer.copy(
                                itemHit = 5
                            )
                        }
                    }
                    6 -> { // Pfeffi Panzer
                        if (victim?.name == player.name) {
                            extraInfo = "${victim.name} muss einen Strafschluck trinken."
                        }
                    }
                    7 -> { // Tripple Pfeffi Panzer
                        if (trigger.name != player.name) {
                            // Note: extraInfo accumulation logic was slightly broken in original, fixing it:
                            if (extraInfo.isEmpty()) extraInfo = "Müssen einen Strafschluck trinken: "
                            extraInfo += "${player.name} "
                        }
                    }
                }

                // 2. Remove item from trigger
                if (player.name == trigger.name) {
                    updatedPlayer = updatedPlayer.copy(
                        items = updatedPlayer.items.drop(1)
                    )
                }

                updatedPlayer
            }

            currentState.copy(
                players = updatedPlayers,
                info = "${trigger.name} hat ${usedItem.name} eingesetzt!\n${extraInfo}"
            )
        }
    }
    fun updatePersonalTargetAfterExxen(weight: Int?) {
        if (weight != null) {
            var playersToSave: List<Player>? = null
            var shouldGetItems = false
            _uiState.update { currentState ->
                // Wir erstellen eine neue Liste, in der nur der 'exxer' aktualisiert wird
                val updatedPlayers = currentState.players.map { player ->
                    if (player.name == currentState.exxer) {
                        val drunkAmount = player.currentWeight - weight
                        val error = abs(drunkAmount - player.personalTarget)

                        // Hier wird der spezifische Spieler aktualisiert






                        player.copy(
                            currentWeight = weight,

                            extraDrunkAccount = player.extraDrunkAccount + player.totalDrunk,
                            totalError = player.totalError + error,
                            sipsErrors = player.sipsErrors + SipError(error, true),
                            totalDrunk = drunkAmount,

                            lastError = drunkAmount - player.personalTarget


                        )
                    } else {
                        // Alle anderen Spieler bleiben unverändert
                        player
                    }
                }
                val nextScreen = when {
                    currentState.playerNeedToEx > 1 -> Screen.PICK_EXXER // Wenn jemand exxen muss
                    currentState.gameEnds -> Screen.RANKING                    // Wenn Spielende gedrückt wurde
                    currentState.gameMode == GameMode.Items -> Screen.ITEM_HANDLING
                    else -> Screen.SET_TARGET             // Normaler Spielverlauf
                }

                if (nextScreen == Screen.RANKING) {
                    playersToSave = updatedPlayers
                }
                if (nextScreen == Screen.ITEM_HANDLING) {
                    shouldGetItems = true
                }
                currentState.copy(
                    players = updatedPlayers,
                    currentScreen = nextScreen,
                    previousScreen = currentState.currentScreen,
                    playerNeedToEx = currentState.playerNeedToEx - 1,


                    )
            }
            clearWeights()
            if (shouldGetItems) getItems()
            playersToSave?.let {
                stroePlayerScoreAtGameEnd(it)
            }
        }
    }

    fun updatePersonalTargetBeforeExxen(weight: Int?) {
        if (weight != null) {
            _uiState.update { currentState ->
                // Wir erstellen eine neue Liste, in der nur der 'exxer' aktualisiert wird
                val updatedPlayers = currentState.players.map { player ->
                    if (player.name == currentState.exxer) {
                        // Hier wird der spezifische Spieler aktualisiert
                        //val newTarget = weight-player.currentWeight
                        val newTarget = player.totalDrunk
                        weight
                        player.copy(
                            currentWeight = weight,

                            personalTarget = newTarget

                        )
                    } else {
                        // Alle anderen Spieler bleiben unverändert
                        player
                    }
                }
                currentState.copy(
                    players = updatedPlayers,
                    currentScreen = Screen.WEIGH_AFTER_EXXEN, // Nächster Schritt im Prozess
                    previousScreen = currentState.currentScreen,
                )
            }
            clearWeights()
        }

    }

    fun getAllPlayersWithHighestLastError(): List<Player> {
        val players = _uiState.value.players
        if (players.isEmpty()) return emptyList()
        val maxVal = players.maxOf { it.lastError }
        return players.filter { it.lastError == maxVal }
    }

    // Liefert alle Spieler mit dem niedrigsten lastError
    fun getAllPlayersWithLowestLastError(): List<Player> {
        val players = _uiState.value.players
        if (players.isEmpty()) return emptyList()
        val minVal = players.minOf { it.lastError }
        return players.filter { it.lastError == minVal }
    }
    fun getLeaderForNextRound(): Player {
        return _uiState.value.players.maxBy { it.totalDrunk }
    }


    fun getPlayersWithPerfectScore(): List<Player> {
        return _uiState.value.players.filter { it.lastError == 0 }
    }
    fun updateRankByTotalError() {
        _uiState.update { currentState ->
            val sortedPlayers = currentState.players
                .sortedBy { it.totalError } // Kleinerer Fehler = besserer Platz
                .mapIndexed { index, player ->
                    player.copy(position = index + 1)
                }
            currentState.copy(players = sortedPlayers)
        }
    }
    fun clearWeights(){
        _uiState.update { currentState ->
            currentState.copy(currentWeights = emptyMap())
        }
    }
    fun updateWeight(newWeight: String, playerName: String) {
        _uiState.update { currentState ->
            val updatedWeights = currentState.currentWeights.toMutableMap()
            updatedWeights[playerName] = newWeight
            currentState.copy(currentWeights = updatedWeights)
        }
    }
    private var settingsLoaded = false

    fun loadSavedSettings(context: android.content.Context) {
        if (settingsLoaded) return
        settingsLoaded = true
        val prefs = context.getSharedPreferences("AllesWiegenPrefs", android.content.Context.MODE_PRIVATE)

        // 1. Vorherige Spieler laden (dein bestehender Code)
        val savedPlayers = prefs.getString("previous_players", "") ?: ""
        val players = if (savedPlayers.isEmpty()) listOf("Spieler 1", "Spieler 2") else savedPlayers.split(",")

        // 2. Bluetooth Adresse laden (NEU)
        val savedAddress = prefs.getString("bluetooth_address", "AA:BB:CC:DD:EE:FF") ?: "AA:BB:CC:DD:EE:FF"
        val savedService = prefs.getString("service_uuid", "fff0") ?: "ffff0"
        val savedCharacteristic = prefs.getString("bluetooth_characteristic", "fff1") ?: "fff1"
        val vibrate = (prefs.getString("vibration", "true") ?: "true").toBoolean()
        val jingle = (prefs.getString("jingle", "true") ?: "true").toBoolean()
        val jingleLocation = prefs.getString("jingleLocation", "") ?: ""
        val showStartupMessage = (prefs.getString("show_startup_message", "true") ?: "true").toBoolean()
        val showUiTips = (prefs.getString("show_ui_tips", "true") ?: "true").toBoolean()
        val sortByRank = (prefs.getString("sort_by_rank", "false") ?: "false").toBoolean()
        val hidePlacement = (prefs.getString("hide_placement", "false") ?: "false").toBoolean()

        val itemOverrides = mutableMapOf<Int, ItemOverride>()
        AvailableItems.getAllIds().forEach { id ->
            val defaultItem = AvailableItems.items.find { it.id == id }
            val r = prefs.getInt("item_${id}_rarity", -1)
            val m = prefs.getFloat("item_${id}_multiplikator", -1f)
            val w = prefs.getInt("item_${id}_wert", -1)
            if (r != -1 || m != -1f || w != -1) {
                itemOverrides[id] = ItemOverride(
                    rarity = if (r != -1) r else (defaultItem?.rarity ?: 1),
                    multiplikator = if (m != -1f) m.toDouble() else (defaultItem?.multiplikator ?: 0.0),
                    wert = if (w != -1) w else (defaultItem?.wert ?: 0)
                )
            }
        }

        _uiState.update { it.copy(
            previousPlayers = players,
            bluetoothAddress = savedAddress, // Adresse in den State schreiben
            SERVICE_UUID = savedService,
            CHARACTERISTIC_UUID = savedCharacteristic,
            vibrate = vibrate,
            showJingle = jingle,
            jingleLocation = jingleLocation,
            showStartupMessage = showStartupMessage,
            showStartupDialog = showStartupMessage,
            showUiTips = showUiTips,
            sortByRank = sortByRank,
            hidePlacement = hidePlacement,
            itemOverrides = itemOverrides,
        ) }
    }

    fun toggleStartupMessage(enabled: Boolean, context: Context) {
        _uiState.update { it.copy(showStartupMessage = enabled, showStartupDialog = if (enabled) true else it.showStartupDialog) }
        val prefs = context.getSharedPreferences("AllesWiegenPrefs", Context.MODE_PRIVATE)
        prefs.edit {
            putString("show_startup_message", enabled.toString())
        }
    }

    fun toggleUiTips(enabled: Boolean, context: Context) {
        _uiState.update { it.copy(showUiTips = enabled) }
        val prefs = context.getSharedPreferences("AllesWiegenPrefs", Context.MODE_PRIVATE)
        prefs.edit {
            putString("show_ui_tips", enabled.toString())
        }
    }

    fun dismissStartupDialog() {
        _uiState.update { it.copy(showStartupDialog = false) }
    }

    // Neue Funktion zum Ändern und Speichern der Adresse:
    fun updateBluetoothAddress(context: android.content.Context, newAddress: String) {
        // State aktualisieren
        _uiState.update { it.copy(bluetoothAddress = newAddress) }

        // Permanent speichern
        val prefs = context.getSharedPreferences("AllesWiegenPrefs", android.content.Context.MODE_PRIVATE)
        prefs.edit {
            putString("bluetooth_address", newAddress)
        }
    }
    fun updateBluetoothServiceUuid(context: android.content.Context, newAddress: String) {
        // State aktualisieren
        _uiState.update { it.copy(SERVICE_UUID = newAddress) }

        // Permanent speichern
        val prefs = context.getSharedPreferences("AllesWiegenPrefs", android.content.Context.MODE_PRIVATE)
        prefs.edit {
            putString("service_uuid", newAddress)
        }
    }
    fun updatecharUudi(context: android.content.Context, newAddress: String) {
        // State aktualisieren
        _uiState.update { it.copy(CHARACTERISTIC_UUID = newAddress) }

        // Permanent speichern
        val prefs = context.getSharedPreferences("AllesWiegenPrefs", android.content.Context.MODE_PRIVATE)
        prefs.edit {
            putString("bluetooth_characteristic", newAddress)
        }
    }

    fun updateGameMode(mode: GameMode) {
        _uiState.update { it.copy(gameMode = mode) }
    }

    fun updateItemVerteilung(distro: String) {
        _uiState.update { it.copy(itemVerteilung = distro) }
    }

    fun updateItemsProRunde(count: Int) {
        _uiState.update { it.copy(itemsProRunde = count) }
    }

    fun toggleErlaubteItem(itemId: Int) {
        _uiState.update { currentState ->
            val currentAllowed = currentState.erlaubteItems.toMutableList()
            if (currentAllowed.contains(itemId)) {
                currentAllowed.remove(itemId)
            } else {
                currentAllowed.add(itemId)
            }
            currentState.copy(erlaubteItems = currentAllowed)
        }
    }

    fun updateItemProperty(context: Context, itemId: Int, rarity: Int, multiplikator: Double, wert: Int) {
        _uiState.update { currentState ->
            val newOverrides = currentState.itemOverrides.toMutableMap()
            newOverrides[itemId] = ItemOverride(rarity, multiplikator, wert)
            currentState.copy(itemOverrides = newOverrides)
        }
        val prefs = context.getSharedPreferences("AllesWiegenPrefs", Context.MODE_PRIVATE)
        prefs.edit {
            putInt("item_${itemId}_rarity", rarity)
            putFloat("item_${itemId}_multiplikator", multiplikator.toFloat())
            putInt("item_${itemId}_wert", wert)
        }
    }

    // 2. Hilfsfunktion zum Speichern innerhalb der Klasse
    private fun saveToDisk(context: android.content.Context, players: List<String>) {
        val prefs = context.getSharedPreferences("AllesWiegenPrefs", android.content.Context.MODE_PRIVATE)
        prefs.edit { putString("previous_players", players.joinToString(",")) }
    }


    fun resetGame(context: android.content.Context) {
        _uiState.update { currentState ->
            val currentNames = currentState.players.map { it.name }
            val oldList = currentState.previousPlayers.toMutableList()

            currentNames.forEach { name ->
                oldList.remove(name)
                oldList.add(0, name)
            }

            val updatedPrevious = oldList.take(15)

            // Speichern
            saveToDisk(context, updatedPrevious)

            currentState.copy(
                players = emptyList(),
                currentRound = 0,
                currentScreen = Screen.SETUP,
                previousPlayers = updatedPrevious,
                currentWeights = emptyMap(),
                activeBluetoothPlayer = null,
                isWeightStable = false,
                playerNeedToEx = 0,
                nfcIsUsed = false,
                gameEnds = false,
                exxer = "",
                showStartupDialog = false
            )
        }
    }
}
