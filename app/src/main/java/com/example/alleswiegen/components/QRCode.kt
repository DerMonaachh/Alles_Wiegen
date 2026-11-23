package com.example.alleswiegen.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter


@Composable
fun QrCodeDialog(csvData: String, onDismiss: () -> Unit) {
    val bitmap = remember(csvData) { generateQrBitmap(csvData) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("QR Code des Spiels, Achtung bei großen Spielen ist die Übertragung via csv besser geeignet.", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(16.dp))

                bitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = "QR Code",
                        modifier = Modifier.size(250.dp)
                    )
                } ?: Text("QR Code konnte nicht erstellt werden (Daten zu groß?)")

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = csvData,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                TextButton(onClick = onDismiss) {
                    Text("Schließen")
                }
            }
        }
    }
}

fun generateQrBitmap(content: String): Bitmap? {
    return try {
        val writer = QRCodeWriter()
        // Wir nutzen hier eine etwas kleinere Auflösung (512x512)
        val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, 512, 512)
        val width = bitMatrix.width
        val height = bitMatrix.height

        val bitmap = createBitmap(width, height, Bitmap.Config.RGB_565)

        for (x in 0 until width) {
            for (y in 0 until height) {
                // bitMatrix.get(x, y) ist true, wenn der Pixel schwarz sein soll
                val color = if (bitMatrix.get(x, y)) {
                    android.graphics.Color.BLACK
                } else {
                    android.graphics.Color.WHITE
                }
                bitmap[x, y] = color
            }
        }
        bitmap
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}