package com.mral.geektest.ui.medication

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun AddMedicationForm(onDismiss: () -> Unit) {
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

    Surface(modifier = Modifier.padding(16.dp)) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Nouveau traitement", style = MaterialTheme.typography.headlineSmall)

            OutlinedTextField(
                value = medicationName,
                onValueChange = { medicationName = it },
                label = { Text("Nom du médicament") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = dosage,
                onValueChange = { dosage = it },
                label = { Text("Dose") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = intakeTime,
                onValueChange = { intakeTime = it },
                label = { Text("Heure(s) de prise") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = startDate,
                    onValueChange = { startDate = it },
                    label = { Text("Date de début") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = endDate,
                    onValueChange = { endDate = it },
                    label = { Text("Date de fin") },
                    modifier = Modifier.weight(1f)
                )
            }

            TextButton(onClick = { showMoreOptions = !showMoreOptions }) {
                Text(if (showMoreOptions) "Moins d'options" else "Plus d'options")
            }

            if (showMoreOptions) {
                // Optional fields
                Text("Informations sur le médicament", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = form,
                    onValueChange = { form = it },
                    label = { Text("Forme (comprimé, gélule, ...)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = dosageUnit,
                    onValueChange = { dosageUnit = it },
                    label = { Text("Dosage (mg, ml, UI…)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = colorOrPhoto,
                    onValueChange = { colorOrPhoto = it },
                    label = { Text("Couleur ou photo") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text("Posologie", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = quantityPerIntake,
                    onValueChange = { quantityPerIntake = it },
                    label = { Text("Quantité par prise") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = frequency,
                    onValueChange = { frequency = it },
                    label = { Text("Fréquence") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))
                Text("Alertes & rappels", style = MaterialTheme.typography.titleMedium)
                // Reminder mode can be a dropdown, but for now a simple text field
                OutlinedTextField(
                    value = reminderMode,
                    onValueChange = { reminderMode = it },
                    label = { Text("Mode de rappel") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = repeatReminder,
                        onCheckedChange = { repeatReminder = it }
                    )
                    Text("Répéter si non confirmé")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(
                        checked = !hideNameInNotification,
                        onCheckedChange = { hideNameInNotification = !it }
                    )
                    Text("Afficher le nom du médicament dans la notification")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Annuler")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = { /* TODO: Save medication */ onDismiss() }) {
                    Text("Enregistrer")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddMedicationFormPreview() {
    AddMedicationForm(onDismiss = {})
}
