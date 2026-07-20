package com.example.alleswiegen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.isEmpty
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit

data class EventData(
    val name: String = "",
    val startDate: Long? = System.currentTimeMillis(),
    val endDate: Long? = System.currentTimeMillis()
)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventEditDialog(initialEvent: EventData = EventData(),
                    onDismiss: () -> Unit,
                    onConfirm: (EventData) -> Unit
) {
    var name by remember { mutableStateOf(initialEvent.name) }

    // Date Picker States
    val startDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialEvent.startDate
    )
    val endDatePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialEvent.endDate
    )

    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                onConfirm(EventData(name, startDatePickerState.selectedDateMillis, endDatePickerState.selectedDateMillis))
            }) {
                Text("Speichern")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Abbrechen") }
        },
        title = { Text(if (initialEvent.name.isEmpty()) "Neues Event" else "Event bearbeiten") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Event Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // Start Date Trigger
                OutlinedTextField(
                    value = startDatePickerState.selectedDateMillis?.let {
                        java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault()).format(it)
                    } ?: "",
                    onValueChange = {},
                    label = { Text("Startdatum") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { showStartDatePicker = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Datum wählen")
                        }
                    }
                )

                // End Date Trigger
                OutlinedTextField(
                    value = endDatePickerState.selectedDateMillis?.let {
                        java.text.SimpleDateFormat("dd.MM.yyyy", java.util.Locale.getDefault()).format(it)
                    } ?: "",
                    onValueChange = {},
                    label = { Text("Enddatum") },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { showEndDatePicker = true }) {
                            Icon(Icons.Default.Edit, contentDescription = "Datum wählen")
                        }
                    }
                )
            }
        }
    )

    // Actual Date Picker Modals
    if (showStartDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = { showStartDatePicker = false }) { Text("OK") }
            }
        ) {
            DatePicker(state = startDatePickerState)
        }
    }

    if (showEndDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = { showEndDatePicker = false }) { Text("OK") }
            }
        ) {
            DatePicker(state = endDatePickerState)
        }
    }
}