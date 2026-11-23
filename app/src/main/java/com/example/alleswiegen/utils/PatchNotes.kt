package com.example.alleswiegen.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp

object PatchNotes {
    fun getPatchNotes(): AnnotatedString {
        return buildAnnotatedString {
            withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, fontSize = 20.sp)) {
                append("Patch Notes\n\n")
            }


            append("\n")

            versionHeader("Version 1.18")
            bulletPoint("Feedback vom geschlossenen Test eingearbeitet")
            bulletPoint("Einwiegen vor und nach dem Bagel visuell überarbeitet")
            bulletPoint("Beschreibung der Punkte-Berechnung in den Optionen visuell & grammatikalisch überarbeitet")
            bulletPoint("Rechtschreibfehler: Farpalette")
            bulletPoint("Abgeschnittene UI: Reiter in Optionen, Buttons auf dem Startbildschirm und Events passen sich dynamisch an")
            bulletPoint("Zielwert-Eingabe auf ganze Zahlen (Integer) beschränkt")
            bulletPoint("UI-Tipps (Hinweise) ausblendbar & in Spiel-Optionen steuerbar")
            bulletPoint("Überspringe doppeltes Spiel einlesen bei mehrfachem QR Code scannen.")
            bulletPoint("Falsche Anzeige von Ziel:0 entfernt")
            bulletPoint("Anzeige der weggelassenen Werte bei den verschiedenen Ranking Methoden verbessert")
            bulletPoint("Spieler Name verschwindet beim Eingeben nicht mehr hinter der Tastatur")
            bulletPoint("Nach dem Beenden des Scan-Vorgangs werden keine Bluetooth-Waagen mehr angezeigt")
            bulletPoint("Leerzeile bei Events entfernt.")
            bulletPoint("Sortierungs-Einstellungen werden zwischen App Starts gespeichert")
            bulletPoint("Animation des Gewinners am Ende beigefügt")
            append("\n")

            versionHeader("Version 1.17")
            bulletPoint("Kleinere Graphik Bugs und Rechtschreibung behoben")
            bulletPoint("Initiale Version für den geschlossenen Test.")

            append("\n")

            versionHeader("Version 1.16")
            bulletPoint("Aktives Scannen nach Waagen in der Nähe")
            bulletPoint("Webhook html angepasst")

            append("\n")
            versionHeader("Version 1.15")
            bulletPoint("Lokaler Server für Multi-Handy Nutzung eingefügt")
            bulletPoint("Farbpalette angepasst")

            append("\n")
            versionHeader("Version 1.14")
            bulletPoint("Neue Item Logos und personalisierbare Items")
            bulletPoint("Score besser lesbar im Dark-Mode")
            bulletPoint("Nachricht am Spiel-Anfang")
            append("\n")
            versionHeader("Version 1.13")
            bulletPoint("Infos zu Items und Punkte Berechnung in Optionen eingefügt")
            bulletPoint("Verhindern, dass der Erste keinen Blauen Hopfen bekommt")

            append("\n")
            versionHeader("Version 1.12")
            bulletPoint("Einführung von Patch-Notes")
            bulletPoint("Item-Modus")
            bulletPoint("Verbessertes Spiel Transfer Verhalten csv/QR")
            append("\n")

            // Version 1.0.0 example
            versionHeader("Version 1.0.0")
            bulletPoint("Erste Funktionale Version")

        }
    }

    private fun AnnotatedString.Builder.versionHeader(version: String) {
        withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = Color(0xFF6200EE), fontSize = 18.sp)) {
            append("$version\n")
        }
    }

    private fun AnnotatedString.Builder.bulletPoint(text: String) {
        append("  • ")
        append("$text\n")
    }
}
