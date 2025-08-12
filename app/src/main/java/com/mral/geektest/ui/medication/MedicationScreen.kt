package com.mral.geektest.ui.medication

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Vaccines
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationScreen(onAddMedicationClick: () -> Unit) {
    Scaffold(
        topBar = { TopBar() },
        bottomBar = { BottomNavigationBar() }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            MainContent(onAddMedicationClick = onAddMedicationClick)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar() {
    TopAppBar(
        title = {
            Text(
                text = "Bonjour, Alex",
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
fun MainContent(onAddMedicationClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AsyncImage(
            model = "https://lh3.googleusercontent.com/aida-public/AB6AXuD2r7y_mFXFe387LG7Stst4NVfBzwM3GMBxSII_v2gLXmw1_zeBoRjkdDhqEJZB-uyfIeyExG0FjmCc525uvE5ERO14ckB8HyJ5ZCU2NM2Cf33K8045FvFcYTOQ_2kMqwjuWF_OEE7KXUa-lLTbHeZ1sj18uplQcrL8PIE1wZLiHfRRClp2lfYtSge1nwXM-yta_EfZ7-WYPY6hTTxeJKtaJkihgbsMF8TmBnLBt_fSZOhpqJrnCBicawqPCqndUYWzgjZfl48VIvNs",
            contentDescription = "Medication image",
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16 / 9f)
                .clip(RoundedCornerShape(8.dp)),
            contentScale = ContentScale.Crop
        )
        Text(
            text = "Aucun médicament prévu",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Vous n'avez aucun médicament prévu pour aujourd'hui. Ajoutez un médicament pour commencer.",
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            color = Color.Gray
        )
        Button(
            onClick = onAddMedicationClick,
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF0F2F4))
        ) {
            Text(text = "Ajouter un médicament", color = Color.Black)
        }
    }
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
fun MedicationScreenPreview() {
    MaterialTheme {
        MedicationScreen(onAddMedicationClick = {})
    }
}
