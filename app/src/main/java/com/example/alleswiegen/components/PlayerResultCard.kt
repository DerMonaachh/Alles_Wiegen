package com.example.alleswiegen.components

import android.R
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.error
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.alleswiegen.Player
import kotlin.math.abs

/*
@Composable
fun PlayerResultCard(player: Player,
                     liveWeight: String,
                     onCaptured: (String) -> Unit,
                     modifier: Modifier = Modifier,
                     round: Int=10,enteredValue: String = "") {
    // Farblogik basierend auf der Abweichung (lastError)
    val releventError=if (enteredValue==""){
        player.lastError
    }else{
        player.upcomingError(enteredValue.toDouble())
    }
    val backgroundColor = when {
        releventError > 0 -> Color(0xFFFFF1AC)
        releventError < 0 -> Color(0xFFFFDAD6)
        else -> Color(0xFFE2F4D8) // Helles Grün
    }

    val contentColor = when {
        releventError > 0 ->Color(0xFF221B00)
        releventError < 0 -> Color(0xFF410002)
        else -> Color(0xFF002204)
    }



    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onCaptured(liveWeight) },
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor,
            contentColor = contentColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (round>1)
                {
                    Text(
                        text = "${player.position}. ${player.name} (${player.personalTarget}g)",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )

                }
                else{
                    Text(
                        text = "${player.name} (${player.personalTarget}g)",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (releventError == 0.0 && round>1) {
                    Text("Perfekt! 🎯", style = MaterialTheme.typography.labelLarge)
                }
                if (releventError <= -30) {
                    Text("Pussy! 🐱", style = MaterialTheme.typography.labelLarge)
                }
                if (releventError >= 30) {
                    Text("Schluckspecht! 🐦‍⬛", style = MaterialTheme.typography.labelLarge)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "Letzter Fehler",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.alpha(0.7f)
                    )
                    Text(
                        text = "${"%.1f".format(releventError)} g",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Column {
                    Text(
                        text = "Gewicht",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.alpha(0.7f)
                    )
                    Text(
                        text = "${"%.1f".format((player.currentWeight))} g",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Gesamtfehler",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.alpha(0.7f)
                    )
                    Text(
                        text = "${"%.1f".format(player.totalError)} g",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}*/
@Composable
fun PlayerResultCard(
    player: Player,
    liveWeight: String,
    round: Int,
    enteredValue: String,
    isError: Boolean,
    onCaptured: (String) -> Unit,
    inputField: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    landscape: Boolean=false,
    hidePlacement: Boolean = false
) {
    val releventError=if (enteredValue==""){
        player.lastError
    }else{
        (player.upcomingError(enteredValue.toInt()).toDouble()*player.nextErrorMultiplier).toInt()
    }
    val releventAbsolutError=if (enteredValue==""){
        player.totalError
    }else{
        (abs(player.upcomingError(enteredValue.toInt()))*player.nextErrorMultiplier).toInt()+player.totalError
    }
    Card(
        modifier = modifier.fillMaxWidth(),
        if (landscape) RoundedCornerShape(2.dp) else RoundedCornerShape(20.dp),

        colors = CardDefaults.cardColors(
            containerColor = if (isError) MaterialTheme.colorScheme.errorContainer
            else MaterialTheme.colorScheme.background
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onPrimaryContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {

            Row(
                modifier = Modifier
                    .padding(2.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Spacer(modifier= Modifier.width(10.dp))
                InnerResultCadd(releventError, releventAbsolutError, player, liveWeight, onCaptured, round, landscape, hidePlacement, inputField)

            }


    }
}