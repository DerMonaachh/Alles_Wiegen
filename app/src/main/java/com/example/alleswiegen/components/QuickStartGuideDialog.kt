package com.example.alleswiegen.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun QuickStartGuideDialog(
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            val pagerState = rememberPagerState(pageCount = { 3 })
            val coroutineScope = rememberCoroutineScope()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = "Quick Start Guide",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.weight(1f)
                ) { page ->
                    when (page) {
                        0 -> GuidePage1()
                        1 -> GuidePage2()
                        2 -> GuidePage3()
                    }
                }

                // Pager Indicators
                Row(
                    Modifier
                        .height(30.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(pagerState.pageCount) { iteration ->
                        val color = if (pagerState.currentPage == iteration) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .clip(CircleShape)
                                .background(color)
                                .size(8.dp)
                        )
                    }
                }

                // Navigation Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (pagerState.currentPage > 0) {
                        TextButton(onClick = {
                            coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                        }) {
                            Text("Zurück")
                        }
                    } else {
                        Spacer(modifier = Modifier.width(64.dp))
                    }

                    if (pagerState.currentPage < pagerState.pageCount - 1) {
                        Button(onClick = {
                            coroutineScope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                        }) {
                            Text("Weiter")
                        }
                    } else {
                        Button(onClick = onDismiss) {
                            Text("Fertig")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GuidePage1() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text("Das Spielprinzip", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            "Bei Alles Wiegen geht es darum, präzise zu trinken. Das System berechnet für jeden Spieler ein individuelles Ziel (in Gramm) auf Basis der Eingaben und bereits getrunkenen Menge.",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Regel: Das Glas darf nur einmal angesetzt werden. Nachkorrigieren ist strengstens verboten!",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text("Ablauf:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text("1. Startgewichte aller Gläser erfassen.")
        Text("2. Rundenziel festlegen.")
        Text("3. Trinken!")
        Text("4. Neues Gewicht einwiegen.")
        Text("5. Auswerten und nächste Runde.")
    }
}

@Composable
fun GuidePage2() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text("Waage, Bluetooth & NFC", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        
        Text("Manuelles Wiegen:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
        Text("Du kannst das Gewicht jederzeit per Hand eintippen.", style = MaterialTheme.typography.bodyMedium)
        
        Spacer(modifier = Modifier.height(12.dp))
        Text("Bluetooth-Waage koppeln:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
        Text("Gehe in die Optionen zum Tab 'Bluetooth' und scanne nach Waagen. Wähle deine Waage aus und speichere sie, dann verbindet sie auf dem Startbildschirm.", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(4.dp))
        Text("Das Gewicht der verbundenen Waage kann dann mit dem Button neben dem Namen in den Wiege-Menüs übernommen werden.", style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(12.dp))
        Text("Spielen mit NFC-Chips:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
        Text("Klebe NFC-Sticker an die Gläser. Halte das Smartphone an das Glas, um den Spieler zu scannen, stelle es auf die Waage und bestätige das Gewicht über den Button. In folgenden Runden passiert die Zuweisung nach dem Scannen automatisch.", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun GuidePage3() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text("Auswertung & Bagel", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(12.dp))
        
        Text("Auswertung:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
        Text("Am Ende jeder Runde wird die Abweichung vom Ziel angezeigt.", style = MaterialTheme.typography.bodyMedium)
        Text("- Negativer Wert = Schluck war zu klein.")
        Text("- Positiver Wert = Schluck war zu groß.")
        Spacer(modifier = Modifier.height(8.dp))
        Text("Der schlechteste Spieler (größte Abweichung) trinkt einen Strafschluck. Der beste Spieler darf einen Schluck verteilen.", style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(12.dp))
        Text("Die Bagel-Regel (\uD83E\uDD6F):", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
        Text("Trifft ein Spieler sein Ziel auf das Gramm genau (0g Abweichung), so darf er einen anderen Spieler wählen, der sein Getränk komplett austrinken muss!", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(4.dp))
        Text("Das ist der Bagel-Modus. Falls deaktiviert, trinken stattdessen einfach alle anderen einen Strafschluck.", style = MaterialTheme.typography.bodyMedium)
    }
}
