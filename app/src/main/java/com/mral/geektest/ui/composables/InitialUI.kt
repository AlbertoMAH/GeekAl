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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

sealed class BottomSheetState {
    object Hidden : BottomSheetState()
    object Searching : BottomSheetState()
    object ProvidersList : BottomSheetState()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InitialUI() {
    var selectedItem by remember { mutableStateOf(0) }
    val items = listOf("Carte", "Demandes", "Profil")
    val icons = listOf(Icons.Filled.Map, Icons.Filled.Build, Icons.Filled.Person)

    var bottomSheetState by remember { mutableStateOf<BottomSheetState>(BottomSheetState.Hidden) }
    val modalSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        topBar = { TopBar() },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        selected = selectedItem == index,
                        onClick = { selectedItem = index },
                        icon = { Icon(icons[index], contentDescription = item) },
                        label = { Text(item) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            indicatorColor = MaterialTheme.colorScheme.surface
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
                .background(MaterialTheme.colorScheme.background)
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
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                )

                FloatingActionButton(
                    onClick = { /* TODO: My Location action */ },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary,
                    shape = CircleShape
                ) {
                    Icon(Icons.Filled.MyLocation, contentDescription = "My Location")
                }
            }

            Button(
                onClick = { bottomSheetState = BottomSheetState.Searching },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("DÉPANNEZ-MOI", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
        }
    }

    if (bottomSheetState !is BottomSheetState.Hidden) {
        ModalBottomSheet(
            onDismissRequest = { bottomSheetState = BottomSheetState.Hidden },
            sheetState = modalSheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            scrimColor = Color.Black.copy(alpha = 0.5f)
        ) {
            when (bottomSheetState) {
                is BottomSheetState.Searching -> {
                    SearchInProgressSheetContent(onClose = { bottomSheetState = BottomSheetState.Hidden })
                }
                is BottomSheetState.ProvidersList -> {
                    ServiceProvidersSheetContent(onClose = { bottomSheetState = BottomSheetState.Hidden })
                }
                else -> {} // Should not happen
            }
        }
    }

    LaunchedEffect(bottomSheetState) {
        if (bottomSheetState is BottomSheetState.Searching) {
            delay(5000)
            bottomSheetState = BottomSheetState.ProvidersList
        }
    }
}

@Composable
fun TopBar() {
    Surface(
        color = MaterialTheme.colorScheme.surface,
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
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp))
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.DirectionsCar,
                            contentDescription = "Car",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Mon Dépanneur PRO", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurface)
                }
                IconButton(onClick = { /* TODO: Settings action */ }) {
                    Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Filled.LocationOn, contentDescription = "Location", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.width(8.dp))
                Text("123 Rue Fictive, Paris", color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = { /* TODO: Refresh action */ }) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Refresh", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
