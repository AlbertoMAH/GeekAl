package com.mral.geektest.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

data class ServiceProvider(
    val id: Int,
    val name: String,
    val initials: String,
    val rating: Double,
    val time: String,
)

val sampleProviders = listOf(
    ServiceProvider(1, "Garage Dubois", "GD", 4.5, "5 min"),
    ServiceProvider(2, "Auto-Réparation Express", "AR", 4.8, "8 min"),
    ServiceProvider(3, "SOS Mécanique", "SM", 4.2, "12 min"),
)

@Composable
fun ServiceProvidersSheetContent(onClose: () -> Unit) {
    var showDialog by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White, shape = RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
                .padding(24.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column {
                    Text("Dépanneurs à proximité", fontWeight = FontWeight.Bold, fontSize = 22.sp)
                    Text("Sélectionnez un professionnel pour continuer.", color = Color.Gray, fontSize = 16.sp)
                }
                IconButton(onClick = onClose) {
                    Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // List of providers
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                sampleProviders.forEach { provider ->
                    ProviderCard(provider = provider, onClick = { showDialog = "You have selected ${provider.name}." })
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action button
            Button(
                onClick = { showDialog = "You selected a 'dépanneur'!" },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899)), // pink-500
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Sélectionnez un dépanneur", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }

        // 'N' icon
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 24.dp, bottom = 16.dp)
                .size(48.dp)
                .background(Color(0xFFE5E7EB), shape = CircleShape)
                .offset(y = 24.dp), // To position it half outside
            contentAlignment = Alignment.Center
        ) {
            Text("N", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.DarkGray)
        }
    }

    if (showDialog != null) {
        CustomAlertDialog(message = showDialog!!, onClose = { showDialog = null })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderCard(provider: ServiceProvider, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9FAFB)), // gray-50
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Initials Circle
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(Color(0xFFE5E7EB), shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(provider.initials, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.DarkGray)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(provider.name, fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Rating
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Star, contentDescription = "Rating", tint = Color(0xFFFBBF24), modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${provider.rating}", color = Color.Gray, fontSize = 14.sp)
                    }
                    // Time
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Schedule, contentDescription = "Time", tint = Color.Gray, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(provider.time, color = Color.Gray, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}

@Composable
fun CustomAlertDialog(message: String, onClose: () -> Unit) {
    Dialog(onDismissRequest = onClose) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(message, textAlign = TextAlign.Center, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onClose,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("OK")
                }
            }
        }
    }
}
