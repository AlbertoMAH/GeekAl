package com.mral.geektest.ui.medication

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mral.geektest.ui.theme.GeekTestTheme

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
    var reminderMode by remember { mutableStateOf("") }
    var repeatReminder by remember { mutableStateOf(false) }
    var hideNameInNotification by remember { mutableStateOf(true) }

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
                Button(
                    onClick = onClose,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Annuler")
                }
                Button(
                    onClick = { /* TODO: Save medication */ onClose() },
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
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SectionTitle("Informations sur le médicament")
            StyledTextField(label = "Nom du médicament", placeholder = "Entrez le nom du médicament", value = medicationName, onValueChange = { medicationName = it })
            StyledTextField(label = "Forme", placeholder = "Ex: Comprimé, Capsule", value = form, onValueChange = { form = it })
            StyledTextField(label = "Dosage", placeholder = "Ex: 25mg, 500ml", value = dosage, onValueChange = { dosage = it })
            StyledTextField(label = "Couleur ou photo", placeholder = "Ajouter une couleur ou une photo", value = colorOrPhoto, onValueChange = { colorOrPhoto = it })

            SectionTitle("Posologie")
            StyledTextField(label = "Quantité par prise", placeholder = "Ex: 1, 2", value = quantityPerIntake, onValueChange = { quantityPerIntake = it })
            StyledTextField(label = "Fréquence", placeholder = "Ex: 1 fois par jour", value = frequency, onValueChange = { frequency = it })
            StyledTextField(label = "Heures exactes de prise", placeholder = "Sélectionnez l'heure", value = intakeTime, onValueChange = { intakeTime = it })
            StyledTextField(label = "Durée du traitement", placeholder = "Sélectionnez la durée", value = treatmentDuration, onValueChange = { treatmentDuration = it })

            SectionTitle("Alertes & rappels")
            StyledTextField(label = "Mode de rappel", placeholder = "Ex: Sonnerie, Vibration", value = reminderMode, onValueChange = { reminderMode = it })

            SwitchRow(text = "Répétition si non confirmé", checked = repeatReminder, onCheckedChange = { repeatReminder = it })
            SwitchRow(text = "Affichage du nom du médicament dans la notification", checked = hideNameInNotification, onCheckedChange = { hideNameInNotification = it })
        }
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold, fontSize = 22.sp),
        modifier = Modifier.padding(top = 8.dp)
    )
}

@Composable
fun StyledTextField(label: String, placeholder: String, value: String, onValueChange: (String) -> Unit) {
    Column {
        Text(text = label, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium), modifier = Modifier.padding(bottom = 8.dp))
        TextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder) },
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
    GeekTestTheme {
        AddMedicationScreen(onClose = {})
    }
}
