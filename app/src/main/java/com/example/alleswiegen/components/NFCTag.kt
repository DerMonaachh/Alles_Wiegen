package com.example.alleswiegen.components

import android.app.Activity
import android.nfc.NfcAdapter
import android.nfc.Tag
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
fun NfcScannerEffect(onTagDetected: (String) -> Unit) {
    val context = LocalContext.current
    val activity = context as? Activity ?: return
    val nfcAdapter = remember { NfcAdapter.getDefaultAdapter(context) }

    DisposableEffect(nfcAdapter) {
        if (nfcAdapter == null) return@DisposableEffect onDispose {}

        val callback = NfcAdapter.ReaderCallback { tag: Tag ->
            // Convert byte array to Hex String
            val id = tag.id.joinToString("") { "%02x".format(it) }.uppercase()
            activity.runOnUiThread { onTagDetected(id) }
        }

        // Enable Reader Mode
        nfcAdapter.enableReaderMode(
            activity,
            callback,
            NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_NFC_B,
            null
        )

        onDispose {
            nfcAdapter.disableReaderMode(activity)
        }
    }
}