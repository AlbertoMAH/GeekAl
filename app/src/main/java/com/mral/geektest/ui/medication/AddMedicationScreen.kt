package com.mral.geektest.ui.medication

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicationScreen(onClose: () -> Unit) {
    var medicationName by remember { mutableStateOf("") }
    var form by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var colorOrPhoto by remember { mutableStateOf("") }
    var quantityPerIntake by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf("") }
    var intakeTime by remember { mutableStateOf("") }
    var treatmentDuration by remember { mutableStateOf("") }
    var reminderMode by remember { mutableStateOf("Notification") }
    var repeatReminder by remember { mutableStateOf(false) }
    var hideNameInNotification by remember { mutableStateOf(true) }

    val isFormValid by remember(medicationName, dosage, intakeTime, treatmentDuration) {
        derivedStateOf {
            medicationName.isNotBlank() &&
            dosage.isNotBlank() &&
            intakeTime.isNotBlank() &&
            treatmentDuration.isNotBlank()
        }
    }

    // State for pickers
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val datePickerState = rememberDatePickerState()
    val timePickerState = rememberTimePickerState()

    // State for dropdown
    var isReminderModeExpanded by remember { mutableStateOf(false) }
    val reminderOptions = listOf("Notification", "Sonnerie", "Vibration")

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val selectedDate = datePickerState.selectedDateMillis?.let {
                        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                        calendar.timeInMillis = it
                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.time)
                    }
                    treatmentDuration = selectedDate ?: ""
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Annuler")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        TimePickerDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        intakeTime = String.format("%02d:%02d", timePickerState.hour, timePickerState.minute)
                        showTimePicker = false
                    }
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(
                    onClick = { showTimePicker = false }
                ) { Text("Annuler") }
            }
        ) {
             TimePicker(state = timePickerState)
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Ajouter un médicament",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                },
                actions = {
                    Spacer(modifier = Modifier.width(48.dp)) // To balance the title
                }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TextButton(
                    onClick = onClose,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Annuler")
                }
                Button(
                    onClick = { /* TODO: Save medication */ onClose() },
                    enabled = isFormValid,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Enregistrer")
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            SectionTitle("Informations sur le médicament")
            StyledTextField(label = "Nom du médicament", placeholder = "Entrez le nom du médicament", value = medicationName, onValueChange = { medicationName = it })
            StyledTextField(label = "Forme", placeholder = "Ex: Comprimé, Capsule", value = form, onValueChange = { form = it })
            StyledTextField(label = "Dosage", placeholder = "Ex: 25mg, 500ml", value = dosage, onValueChange = { dosage = it })

            Column {
                Text(text = "Couleur ou photo", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium), modifier = Modifier.padding(bottom = 8.dp))
                OutlinedButton(
                    onClick = { /* TODO: Implement color/photo picker */ },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Ajouter une couleur ou une photo")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Divider()
            Spacer(modifier = Modifier.height(8.dp))

            SectionTitle("Posologie")
            StyledTextField(label = "Quantité par prise", placeholder = "Ex: 1, 2", value = quantityPerIntake, onValueChange = { quantityPerIntake = it })
            StyledTextField(label = "Fréquence", placeholder = "Ex: 1 fois par jour", value = frequency, onValueChange = { frequency = it })

            Box(modifier = Modifier.clickable { showTimePicker = true }) {
                StyledTextField(
                    label = "Heures exactes de prise",
                    placeholder = "Sélectionnez l'heure",
                    value = intakeTime,
                    onValueChange = {},
                    leadingIcon = Icons.Default.Schedule,
                    readOnly = true
                )
            }

            Box(modifier = Modifier.clickable { showDatePicker = true }) {
                StyledTextField(
                    label = "Durée du traitement",
                    placeholder = "Sélectionnez la durée",
                    value = treatmentDuration,
                    onValueChange = {},
                    leadingIcon = Icons.Default.CalendarToday,
                    readOnly = true
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Divider()
            Spacer(modifier = Modifier.height(8.dp))

            SectionTitle("Alertes & rappels")

            Column {
                Text(text = "Mode de rappel", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium), modifier = Modifier.padding(bottom = 8.dp))
                ExposedDropdownMenuBox(
                    expanded = isReminderModeExpanded,
                    onExpandedChange = { isReminderModeExpanded = !isReminderModeExpanded }
                ) {
                    TextField(
                        value = reminderMode,
                        onValueChange = {},
                        readOnly = true,
                        placeholder = { Text("Sélectionnez le mode") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isReminderModeExpanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        shape = RoundedCornerShape(8.dp),
                        colors = TextFieldDefaults.colors(
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = isReminderModeExpanded,
                        onDismissRequest = { isReminderModeExpanded = false }
                    ) {
                        reminderOptions.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option) },
                                onClick = {
                                    reminderMode = option
                                    isReminderModeExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            SwitchRow(text = "Répétition si non confirmé", checked = repeatReminder, onCheckedChange = { repeatReminder = it })
            SwitchRow(text = "Affichage du nom du médicament dans la notification", checked = hideNameInNotification, onCheckedChange = { hideNameInNotification = it })
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// A custom TimePickerDialog because the M3 one is not available yet
@Composable
fun TimePickerDialog(
    title: @Composable () -> Unit = {},
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = title,
        text = content,
        confirmButton = confirmButton,
        dismissButton = dismissButton
    )
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
fun StyledTextField(
    label: String,
    placeholder: String,
    value: String,
    onValueChange: (String) -> Unit,
    leadingIcon: ImageVector? = null,
    readOnly: Boolean = false
) {
    Column {
        Text(text = label, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium), modifier = Modifier.padding(bottom = 8.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            readOnly = readOnly,
            placeholder = { Text(placeholder) },
            leadingIcon = leadingIcon?.let { { Icon(it, contentDescription = null) } },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )
    }
}

@Composable
fun SwitchRow(text: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text, style = MaterialTheme.typography.bodyLarge)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                checkedTrackColor = MaterialTheme.colorScheme.primary
            )
        )
    }
}


@Preview(showBackground = true)
@Composable
fun AddMedicationScreenPreview() {
    MaterialTheme {
        AddMedicationScreen(onClose = {})
    }
}
