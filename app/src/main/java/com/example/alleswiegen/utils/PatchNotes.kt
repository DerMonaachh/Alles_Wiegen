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


            versionHeader("Dank")
            bulletPoint("Dank geht raus an Nico, Klari und Rici fürs rege Testen ( auf Festivals und daheim).")
            bulletPoint("Dank geht raus and David für die Idee mit den NFC-Tags, auch wenns nur Trashtalk war.")


            append("\n")
            versionHeader("Version 1.13")
            bulletPoint("Infos zu Items und Punkte Berechnung in Optionen eingefügt")
            bulletPoint("Verhindern, dass der Erste eine Blue Shell bekommt")

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
