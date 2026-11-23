package com.example.alleswiegen

import androidx.annotation.DrawableRes



data class Item(
    val id: Int,
    val name: String,
    @DrawableRes val imageRes: Int,
    val rarity: Int,
    val multiplikator: Double = 0.0,
    val wert: Int = 0,
    val beschreibung: String = "",
    val target: Boolean = false
) {
    fun buildBeschreibung(): String {
        var runningBeschreibung = beschreibung

        runningBeschreibung = runningBeschreibung.replace("%wert%", wert.toString())

        runningBeschreibung = runningBeschreibung.replace("%multi%", multiplikator.toString())


        return runningBeschreibung
    }
}

object AvailableItems {
    val items = listOf(
        Item(
            id = 1,
            name = "Roter Hopfen",
            imageRes = R.drawable.wein, // Platzhalter
            rarity = 2,
            wert = 5,
            beschreibung = "Wähle einen Spieler aus, der einen zusätzlichen Fehler von %wert% bekommt.",
            target = true
        ),
        Item(
            id = 2,
            name = "Blauer Hopfen",
            imageRes = R.drawable.blue, // Platzhalter
            rarity = 3,
            wert = 5,
            multiplikator = 1.5,
            beschreibung = "Der Erstplatzierte erhält in der nächsten Runde einen %multi%-fachen Fehler und sofort einen zusätzlichen Fehler von %wert%.",

        ),
        Item(
            id = 3,
            name = "Reset",
            imageRes = R.drawable.reset, // Platzhalter
            rarity = 2,
            beschreibung = "Dein höchster bisheriger Fehler wird auf 0 gesetzt."
        ),
        Item(
            id = 4,
            name = "Blitz",
            imageRes = R.drawable.blitz, // Platzhalter
            rarity = 3,
            wert = 5,
            beschreibung = "Alle anderen Spieler bekommen einen zusätzlichen Fehler von %wert%."
        ),
        Item(
            id = 5,
            name = "Kristallkugel",
            imageRes = R.drawable.kristall_kugel, // Platzhalter
            rarity = 1,
            beschreibung = "Du darfst beim nächsten Mal einmal zwischenwiegen."
        ),
        Item(
            id = 6,
            name = "Grüner Hopfen",
            imageRes = R.drawable.grun, // Platzhalter
            rarity = 1,
            beschreibung = "Wähle einen Spieler, der einen Strafschluck trinken muss.",
            target = true
        ),
        Item(
            id = 7,
            name = "Gruppen-Hopfen",
            imageRes = R.drawable.tripple_grun, // Platzhalter
            rarity = 2,
            beschreibung = "Alle Spieler außer dir müssen einen Strafschluck trinken.",
            target = false
        ),

    )

    fun getAllIds(): List<Int> {
        return items.map { it.id }
    }
}
