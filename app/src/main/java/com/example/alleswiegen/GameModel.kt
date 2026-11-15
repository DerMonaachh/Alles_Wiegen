package com.example.alleswiegen

data class Player(
    val name: String,
    val totalDrunk: Int= 0,
    val totalError: Int= 0,
    val lastError: Int= 0,
    val currentWeight: Int= 0,
    val lastValue: Int= 0,
    val personalTarget: Int= 0,
    val position: Int = 0,
    val sipsErrors: List<SipError> = emptyList(),
    val nfcId: String = "",
    val extraDrunkAccount: Int = 0,
    val items: List<Item> = emptyList(),
    val nextErrorMultiplier: Double = 1.0,
    val itemHit:Int=0
) {



    fun upcomingError(enteredValue : Int): Int{
        val drunkAmount = currentWeight - enteredValue
        val lastError = drunkAmount - personalTarget
        return lastError
    }

    fun averageError():Double{
        if (sipsErrors.isEmpty()) return 0.0
        return totalError.toDouble()/sipsErrors.size
    }
    fun regularError():Int{
        return sipsErrors.filter { !it.exxer }.sumOf { it.amount }
    }
    fun reuglarCount():Int{
        return sipsErrors.filter { !it.exxer }.size
    }
    fun getBagelCount():Int{
        return sipsErrors.filter { it.amount==0 }.size
    }

}
fun List<Player>.getRankOf(player: Player, byRegularError: Boolean = false): Int {
    val sorted = if (byRegularError) {
        this.sortedBy { it.regularError() }
    } else {
        this.sortedBy { it.totalError }
    }
    return sorted.indexOf(player) + 1
}
data class SipError(
    val amount:Int,
    val exxer: Boolean,
)

enum class Screen {
    SETUP,
    INITIAL_WEIGHING,
    SET_TARGET,
    PlAYER_WEIGHING,
    RANKING,
    SHOW_BEST_AND_WORST,
    PICK_EXXER,
    WEIGH_BEFORE_EXXEN,
    WEIGH_AFTER_EXXEN,
    OPTIONS,
    EVENT,
    ALL_EVENTS,
    ITEM_HANDLING,




}
/*
data class PlayerScoreEntry(
    val name: String="",
    val points: Int=0,
    val averageError: Double=0.0,
    val totatErrorClassic: Double=0.0,
    val totalErrorWithoutWorst: Double=0.0,
    val totalErrorWithoutExxer: Double=0.0,
    val totalErrorWithoutWorstAndExxer: Double=0.0,
    val date: Long,


)*/
enum class ItemDistro(val displayName: String) {
    letzer("Rubberband"),
    erster("Bleib Unten!"),
    runden_erster("Runden Erster"),
    runden_letzter("Runden Letzter"),
    gleichmasig("Gleichheit");



    // Hilfsfunktion, um eine Liste für das Dropdown zu bekommen
    companion object {
        fun getDisplayNames() = entries.map { it.displayName }
    }
}

enum class GameMode(val displayName: String) {
    Normal("Normal"),
    Items("Items");

    companion object {
        fun getDisplayNames() = entries.map { it.displayName }
        fun fromInt(value: Int): GameMode = when (value) {
            1 -> Items
            else -> Normal
        }
    }
    fun toInt(): Int = when (this) {
        Normal -> 0
        Items -> 1
    }
}



data class ItemOverride(
    val rarity: Int,
    val multiplikator: Double,
    val wert: Int
)

data class GameUiState(
    val previousScreen: Screen = Screen.SETUP,
    val players: List<Player> = emptyList(),
    val currentRound: Int = 0,
    val currentScreen: Screen = Screen.SETUP,
    val previousPlayers: List<String> = emptyList(),
    val currentWeights: Map<String, String> = emptyMap(),
    val activeBluetoothPlayer: String? = null,
    val isWeightStable: Boolean = false,
    val isBluetoothConnected: Boolean = false,
    val playerNeedToEx : Int=0,
    val nfcIsUsed: Boolean = false,
    val info: String = "Viel Spaß",
    val exenByNull: Boolean=true,
    val sortByRank: Boolean=false,
    val showJingle: Boolean=true,
    val jingleLocation: String="",
    val vibrate: Boolean=true,
    val requested: Int= 0,
    val exxer:String="",
    val gameEnds:Boolean=false,
    val bluetoothAddress: String = "FF:03:00:6D:E3:07",
    val SERVICE_UUID: String = "fff0",
    val CHARACTERISTIC_UUID: String = "fff1",
    val lastStoredGameTime: Long = 0,
    val gameMode: GameMode = GameMode.Normal,
    val itemVerteilung:String= ItemDistro.runden_erster.displayName,
    val itemsProRunde:Int=2,
    val erlaubteItems: List<Int> = AvailableItems.getAllIds(),
    val itemOverrides: Map<Int, ItemOverride> = emptyMap()
) {
    fun getEffectiveItems(): List<Item> {
        return AvailableItems.items.map { item ->
            val override = itemOverrides[item.id]
            if (override != null) {
                item.copy(
                    rarity = override.rarity,
                    multiplikator = override.multiplikator,
                    wert = override.wert
                )
            } else {
                item
            }
        }
    }
}
