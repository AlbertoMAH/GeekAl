package com.mral.geektest.ui.medication

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LocalPharmacy
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.mral.geektest.model.Medication
import com.mral.geektest.model.MedicationRepository

@Composable
fun AddMedicationForm(onDismiss: () -> Unit, onSave: (Medication) -> Unit) {
    var medicationName by remember { mutableStateOf("") }
    var dosage by remember { mutableStateOf("") }
    var intakeTime by remember { mutableStateOf("") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var showMoreOptions by remember { mutableStateOf(false) }

    // Optional fields
    var form by remember { mutableStateOf("") }
    var dosageUnit by remember { mutableStateOf("") }
    var colorOrPhoto by remember { mutableStateOf("") }
    var quantityPerIntake by remember { mutableStateOf("") }
    var frequency by remember { mutableStateOf("") }
    var reminderMode by remember { mutableStateOf("Notification") }
    var repeatReminder by remember { mutableStateOf(false) }
    var hideNameInNotification by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocalPharmacy,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Nouveau traitement",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fermer",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Scrollable content
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Essential Information Section
                    SectionCard(
                        title = "Informations essentielles",
                        icon = Icons.Default.Medication
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            ModernTextField(
                                value = medicationName,
                                onValueChange = { medicationName = it },
                                label = "Nom du médicament",
                                icon = Icons.Default.Medication
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                ModernTextField(
                                    value = dosage,
                                    onValueChange = { dosage = it },
                                    label = "Dose",
                                    modifier = Modifier.weight(1f)
                                )
                                ModernTextField(
                                    value = intakeTime,
                                    onValueChange = { intakeTime = it },
                                    label = "Heure de prise",
                                    icon = Icons.Default.AccessTime,
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                ModernTextField(
                                    value = startDate,
                                    onValueChange = { startDate = it },
                                    label = "Date de début",
                                    icon = Icons.Default.CalendarToday,
                                    modifier = Modifier.weight(1f)
                                )
                                ModernTextField(
                                    value = endDate,
                                    onValueChange = { endDate = it },
                                    label = "Date de fin",
                                    icon = Icons.Default.CalendarToday,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // Advanced Options Toggle
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showMoreOptions = !showMoreOptions },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Options avancées",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                            Icon(
                                imageVector = if (showMoreOptions) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                contentDescription = if (showMoreOptions) "Réduire" else "Développer",
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }

                    // Advanced Options
                    AnimatedVisibility(
                        visible = showMoreOptions,
                        enter = expandVertically() + fadeIn(),
                        exit = shrinkVertically() + fadeOut()
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                            // Medication Details
                            SectionCard(title = "Détails du médicament") {
                                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                    ModernTextField(
                                        value = form,
                                        onValueChange = { form = it },
                                        label = "Forme (comprimé, gélule, ...)"
                                    )
                                    ModernTextField(
                                        value = dosageUnit,
                                        onValueChange = { dosageUnit = it },
                                        label = "Dosage (mg, ml, UI…)"
                                    )
                                    ModernTextField(
                                        value = colorOrPhoto,
                                        onValueChange = { colorOrPhoto = it },
                                        label = "Couleur ou photo"
                                    )
                                }
                            }

                            // Dosage Information
                            SectionCard(title = "Posologie") {
                                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                    ModernTextField(
                                        value = quantityPerIntake,
                                        onValueChange = { quantityPerIntake = it },
                                        label = "Quantité par prise"
                                    )
                                    ModernTextField(
                                        value = frequency,
                                        onValueChange = { frequency = it },
                                        label = "Fréquence"
                                    )
                                }
                            }

                            // Alerts & Reminders
                            SectionCard(
                                title = "Alertes & rappels",
                                icon = Icons.Default.Notifications
                            ) {
                                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                                    ModernTextField(
                                        value = reminderMode,
                                        onValueChange = { reminderMode = it },
                                        label = "Mode de rappel"
                                    )

                                    ModernSwitchRow(
                                        checked = repeatReminder,
                                        onCheckedChange = { repeatReminder = it },
                                        title = "Répéter si non confirmé",
                                        subtitle = "Relancer la notification en cas d'oubli"
                                    )

                                    ModernSwitchRow(
                                        checked = !hideNameInNotification,
                                        onCheckedChange = { hideNameInNotification = !it },
                                        title = "Afficher le nom du médicament",
                                        subtitle = "Visible dans les notifications"
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Annuler")
                    }
                    Button(
                        onClick = {
                            val newMedication = Medication(
                                id = 0, // Will be replaced by repository
                                name = medicationName,
                                dosage = dosage,
                                intakeTime = intakeTime,
                                startDate = startDate,
                                endDate = endDate,
                                form = form.takeIf { it.isNotBlank() },
                                dosageUnit = dosageUnit.takeIf { it.isNotBlank() },
                                quantityPerIntake = quantityPerIntake.takeIf { it.isNotBlank() },
                                frequency = frequency.takeIf { it.isNotBlank() },
                                reminderMode = reminderMode,
                                repeatReminder = repeatReminder,
                                hideNameInNotification = hideNameInNotification
                            )
                            onSave(newMedication)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = "Enregistrer",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionCard(
    title: String,
    icon: ImageVector? = null,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                icon?.let {
                    Icon(
                        imageVector = it,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
            content()
        }
    }
}

@Composable
private fun ModernTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        ),
        leadingIcon = icon?.let {
            {
                Icon(
                    imageVector = it,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    )
}

@Composable
private fun ModernSwitchRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    title: String,
    subtitle: String? = null
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                subtitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddMedicationFormPreview() {
    MaterialTheme {
        AddMedicationForm(onDismiss = {}, onSave = {})
    }
}
