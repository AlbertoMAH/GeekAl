package com.mral.geektest.ui.medication

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
    var quantityPerIntake by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf("") }
    var intakeTime by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var reminderMode by remember { mutableStateOf("Notification") }
    var repeatReminder by remember { mutableStateOf(false) }
    var hideNameInNotification by remember { mutableStateOf(true) }

    var isVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        isVisible = true
    }

    // Touched states for validation
    var medicationNameTouched by remember { mutableStateOf(false) }
    var dosageTouched by remember { mutableStateOf(false) }
    var intakeTimeTouched by remember { mutableStateOf(false) }
    var startDateTouched by remember { mutableStateOf(false) }
    var endDateTouched by remember { mutableStateOf(false) }


    val isFormValid by remember(medicationName, dosage, intakeTime, startDate, endDate) {
        derivedStateOf {
            medicationName.isNotBlank() &&
                    dosage.isNotBlank() &&
                    intakeTime.isNotBlank() &&
                    startDate.isNotBlank() &&
                    endDate.isNotBlank()
        }
    }

    // State for pickers
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val startDatePickerState = rememberDatePickerState()
    val endDatePickerState = rememberDatePickerState()
    val timePickerState = rememberTimePickerState()

    // State for dropdown
    var isReminderModeExpanded by remember { mutableStateOf(false) }
    val reminderOptions = listOf("Notification", "Sonnerie", "Vibration")

    if (showStartDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val selectedDate = startDatePickerState.selectedDateMillis?.let {
                        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                        calendar.timeInMillis = it
                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.time)
                    }
                    startDate = selectedDate ?: ""
                    showStartDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) {
                    Text("Annuler")
                }
            }
        ) {
            DatePicker(state = startDatePickerState)
        }
    }

    if (showEndDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    val selectedDate = endDatePickerState.selectedDateMillis?.let {
                        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                        calendar.timeInMillis = it
                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(calendar.time)
                    }
                    endDate = selectedDate ?: ""
                    showEndDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) {
                    Text("Annuler")
                }
            }
        ) {
            DatePicker(state = endDatePickerState)
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
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(animationSpec = tween(durationMillis = 500)) + slideInVertically(
                initialOffsetY = { it / 10 },
                animationSpec = tween(durationMillis = 500)
            ),
            modifier = Modifier.padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                SectionTitle("Informations sur le médicament")
                StyledTextField(
                    label = "Nom du médicament",
                    placeholder = "Entrez le nom du médicament",
                    value = medicationName,
                    onValueChange = { medicationName = it },
                    isError = medicationNameTouched && medicationName.isBlank(),
                    errorMessage = "Le nom du médicament est requis.",
                    modifier = Modifier.onFocusChanged { if (!it.isFocused) medicationNameTouched = true }
                )
                StyledTextField(label = "Forme", placeholder = "Ex: Comprimé, Capsule", value = form, onValueChange = { form = it })
                StyledTextField(
                    label = "Dosage",
                    placeholder = "Ex: 25mg, 500ml",
                    value = dosage,
                    onValueChange = { dosage = it },
                    isError = dosageTouched && dosage.isBlank(),
                    errorMessage = "Le dosage est requis.",
                    modifier = Modifier.onFocusChanged { if (!it.isFocused) dosageTouched = true }
                )

                Column {
                    Text(text = "Couleur ou photo", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium), modifier = Modifier.padding(bottom = 8.dp))
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                            .clickable { /* TODO: Implement color/photo picker */ },
                        shape = RoundedCornerShape(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.AddPhotoAlternate, contentDescription = "Add photo", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("Ajouter une couleur ou une photo", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                Divider()

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
                        readOnly = true,
                        isError = intakeTimeTouched && intakeTime.isBlank(),
                        errorMessage = "L'heure de prise est requise.",
                        modifier = Modifier.onFocusChanged { if (!it.isFocused) intakeTimeTouched = true },
                        trailingIcon = {
                            if (intakeTime.isNotBlank()) {
                                IconButton(onClick = { intakeTime = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear time")
                                }
                            }
                        }
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Box(modifier = Modifier
                        .weight(1f)
                        .clickable { showStartDatePicker = true }) {
                        StyledTextField(
                            label = "Date de début",
                            placeholder = "Sélectionnez",
                            value = startDate,
                            onValueChange = {},
                            leadingIcon = Icons.Default.CalendarToday,
                            readOnly = true,
                            isError = startDateTouched && startDate.isBlank(),
                            errorMessage = "Requis",
                            modifier = Modifier.onFocusChanged { if (!it.isFocused) startDateTouched = true }
                        )
                    }
                    Box(modifier = Modifier
                        .weight(1f)
                        .clickable { showEndDatePicker = true }) {
                        StyledTextField(
                            label = "Date de fin",
                            placeholder = "Sélectionnez",
                            value = endDate,
                            onValueChange = {},
                            leadingIcon = Icons.Default.CalendarToday,
                            readOnly = true,
                            isError = endDateTouched && endDate.isBlank(),
                            errorMessage = "Requis",
                            modifier = Modifier.onFocusChanged { if (!it.isFocused) endDateTouched = true }
                        )
                    }
                }

                Divider()

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
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(),
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
}

@Composable
fun TimePickerDialog(
    onDismissRequest: () -> Unit,
    confirmButton: @Composable () -> Unit,
    dismissButton: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text("Select Time") },
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
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    readOnly: Boolean = false,
    isError: Boolean = false,
    errorMessage: String = ""
) {
    Column(modifier = modifier) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium), modifier = Modifier.padding(bottom = 8.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            readOnly = readOnly,
            placeholder = { Text(placeholder) },
            leadingIcon = leadingIcon?.let { { Icon(it, contentDescription = null) } },
            trailingIcon = trailingIcon,
            isError = isError,
            supportingText = {
                if (isError) {
                    Text(errorMessage, color = MaterialTheme.colorScheme.error)
                }
            },
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
