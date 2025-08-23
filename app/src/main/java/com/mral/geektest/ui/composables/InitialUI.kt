package com.mral.geektest.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InitialUI() {
    var selectedItem by remember { mutableStateOf(0) }
    val items = listOf("Carte", "Demandes", "Profil")
    val icons = listOf(Icons.Filled.Map, Icons.Filled.Build, Icons.Filled.Person)

    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = { TopBar() },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedItem == index,
                        onClick = { selectedItem = index },
                        icon = { Icon(icons[index], contentDescription = item) },
                        label = { Text(item) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color(0xFFEF4444),
                            selectedTextColor = Color(0xFFEF4444),
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray,
                            indicatorColor = Color.White
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .background(Color(0xFFF3F4F6)) // gray-100 from tailwind
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // This Box represents the map area.
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.LightGray)
                )

                FloatingActionButton(
                    onClick = { /* TODO: My Location action */ },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                    containerColor = Color(0xFF3B82F6), // blue-500 from tailwind
                    shape = CircleShape
                ) {
                    Icon(Icons.Filled.MyLocation, contentDescription = "My Location", tint = Color.White)
                }
            }

            Button(
                onClick = { showBottomSheet = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)), // red-500 from tailwind
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("DÉPANNEZ-MOI", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 18.sp)
            }
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
            containerColor = Color.Transparent, // Make the sheet's container transparent
            scrimColor = Color.Black.copy(alpha = 0.5f) // Dark overlay
        ) {
            SearchInProgressSheetContent(onClose = {
                scope.launch { sheetState.hide() }.invokeOnCompletion {
                    if (!sheetState.isVisible) {
                        showBottomSheet = false
                    }
                }
            })
        }
    }
}

@Composable
fun TopBar() {
    Surface(
        color = Color.White,
        shadowElevation = 8.dp,
        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .background(Color(0xFFFEE2E2), shape = RoundedCornerShape(8.dp)) // red-100
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.DirectionsCar,
                            contentDescription = "Car",
                            tint = Color(0xFFEF4444) // red-500
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Mon Dépanneur PRO", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF1F2937))
                }
                IconButton(onClick = { /* TODO: Settings action */ }) {
                    Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = Color.Gray)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.LocationOn, contentDescription = "Location", tint = Color.Gray)
                Spacer(modifier = Modifier.width(8.dp))
                Text("123 Rue Fictive, Paris", color = Color.Gray)
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { /* TODO: Refresh action */ }) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Refresh", tint = Color.Gray)
                }
            }
        }
    }
}
