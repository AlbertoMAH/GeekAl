package com.mral.geektest.ui.medication

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Vaccines
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mral.geektest.model.Medication
import com.mral.geektest.model.MedicationRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationListScreen() {
    var showAddMedicationDialog by remember { mutableStateOf(false) }

    if (showAddMedicationDialog) {
        AddMedicationForm(
            onDismiss = { showAddMedicationDialog = false },
            onSave = { newMedication ->
                MedicationRepository.addMedication(newMedication)
            }
        )
    }

    Scaffold(
        topBar = { TopBar() },
        bottomBar = { BottomNavigationBar() },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddMedicationDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Medication")
            }
        }
    ) { paddingValues ->
        MedicationList(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )
    }
}

@Composable
fun MedicationList(modifier: Modifier = Modifier) {
    val medications = MedicationRepository.getAllMedications()

    if (medications.isEmpty()) {
        Box(
            modifier = modifier.padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Aucun médicament prévu.\nCliquez sur '+' pour commencer.",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    } else {
        LazyColumn(
            modifier = modifier,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(medications) { medication ->
                MedicationListItem(medication = medication)
            }
        }
    }
}

@Composable
fun MedicationListItem(medication: Medication) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = medication.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Dosage: ${medication.dosage}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Heure: ${medication.intakeTime}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar() {
    TopAppBar(
        title = {
            Text(
                text = "Mes Médicaments",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        actions = {
            IconButton(onClick = { /* TODO */ }) {
                Icon(Icons.Default.Settings, contentDescription = "Settings")
            }
        }
    )
}

@Composable
fun BottomNavigationBar() {
    NavigationBar {
        NavigationBarItem(
            icon = { Icon(Icons.Default.Home, contentDescription = "Accueil") },
            label = { Text("Accueil") },
            selected = true,
            onClick = { }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Outlined.CalendarMonth, contentDescription = "Calendrier") },
            label = { Text("Calendrier") },
            selected = false,
            onClick = { }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Outlined.Vaccines, contentDescription = "Médicaments") },
            label = { Text("Médicaments") },
            selected = false,
            onClick = { }
        )
        NavigationBarItem(
            icon = { Icon(Icons.Outlined.Person, contentDescription = "Profil") },
            label = { Text("Profil") },
            selected = false,
            onClick = { }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MedicationListScreenPreview() {
    MaterialTheme {
        MedicationListScreen()
    }
}
