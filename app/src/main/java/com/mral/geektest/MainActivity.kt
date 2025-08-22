package com.mral.geektest

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.outlined.Build
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mral.geektest.ui.composables.MapView
import kotlinx.coroutines.launch
import org.maplibre.android.MapLibre
import org.maplibre.android.WellKnownTileServer
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.engine.LocationEngineRequest
import org.maplibre.android.location.modes.CameraMode
import org.maplibre.android.location.modes.RenderMode
import org.maplibre.android.maps.MapLibreMap

sealed class Screen {
    object Map : Screen()
    object Details : Screen()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapLibre.getInstance(this, getString(R.string.maptiler_api_key), WellKnownTileServer.MapTiler)
        setContent {
            MaterialTheme {
                var currentScreen by remember { mutableStateOf<Screen>(Screen.Map) }
                when (currentScreen) {
                    is Screen.Map -> MainScreen(onNavigateToDetails = { currentScreen = Screen.Details })
                    is Screen.Details -> RequestDetailsScreen(onNavigateBack = { currentScreen = Screen.Map })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@SuppressLint("MissingPermission")
@Composable
fun MainScreen(onNavigateToDetails: () -> Unit) {
    val context = LocalContext.current
    val styleUrl = "https://api.maptiler.com/maps/streets/style.json?key=${context.getString(R.string.maptiler_api_key)}"
    var map: MapLibreMap? by remember { mutableStateOf(null) }
    var hasLocationPermission by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    val scope = rememberCoroutineScope()

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            hasLocationPermission = true
        } else {
            // TODO: Location permission denied
        }
    }

    LaunchedEffect(map, hasLocationPermission) {
        if (map != null && hasLocationPermission) {
            map?.getStyle { style ->
                map?.locationComponent?.apply {
                    val locationEngineRequest = LocationEngineRequest.Builder(750)
                        .setFastestInterval(750)
                        .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                        .build()

                    activateLocationComponent(
                        LocationComponentActivationOptions.builder(context, style)
                            .useDefaultLocationEngine(true)
                            .locationEngineRequest(locationEngineRequest)
                            .build()
                    )
                    isLocationComponentEnabled = true
                    cameraMode = CameraMode.TRACKING
                    renderMode = RenderMode.COMPASS
                }
            }
        }
    }

    ModalBottomSheetLayout(
        sheetState = sheetState,
        sheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        sheetContent = {
            ServiceSelectionSheetContent(onConfirm = {
                scope.launch {
                    sheetState.hide()
                    onNavigateToDetails()
                }
            })
        }
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Layer 1: MapView
            MapView(
                modifier = Modifier.fillMaxSize(),
                onMapReady = { map = it },
                styleUrl = styleUrl,
                initialCenter = LatLng(5.3, -4.0),
                initialZoom = 12.0
            )

            // Layer 2: UI Elements on top of the map
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Floating Action Button
                FloatingActionButton(
                    onClick = {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    },
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(bottom = 16.dp),
                    containerColor = Color.White
                ) {
                    Icon(
                        imageVector = Icons.Filled.MyLocation,
                        contentDescription = "My Location",
                        tint = Color(0xFF2563EB)
                    )
                }

                // Bottom Control Panel
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White.copy(alpha = 0.8f))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Gradient Button
                    Button(
                        onClick = {
                            scope.launch { sheetState.show() }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(Color(0xFF3B82F6), Color(0xFF6366F1))
                                    )
                                )
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Demander un dépannage", color = Color.White)
                        }
                    }

                    // Bottom Navigation
                    var selectedItem by remember { mutableStateOf(0) }
                    val items = listOf("Accueil", "Dépanneurs", "Paramètres")
                    val icons = listOf(Icons.Outlined.Home, Icons.Outlined.Build, Icons.Outlined.Settings)

                    NavigationBar(
                        containerColor = Color.Transparent,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items.forEachIndexed { index, item ->
                            NavigationBarItem(
                                icon = { Icon(icons[index], contentDescription = item) },
                                label = { Text(item) },
                                selected = selectedItem == index,
                                onClick = { selectedItem = index },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF2563EB),
                                    selectedTextColor = Color(0xFF2563EB),
                                    unselectedIconColor = Color.Gray,
                                    unselectedTextColor = Color.Gray,
                                    indicatorColor = Color.Transparent
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ServiceSelectionSheetContent(onConfirm: () -> Unit) {
    var selectedService by remember { mutableStateOf("Dépannage") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Handle
        Box(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .height(4.dp)
                .fillMaxWidth(0.15f)
                .clip(RoundedCornerShape(2.dp))
                .background(Color.LightGray)
        )

        Text(
            text = "Sélectionnez le type de service",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        ServiceOption(
            title = "Dépannage",
            icon = Icons.Filled.Build,
            isSelected = selectedService == "Dépannage",
            onClick = { selectedService = "Dépannage" }
        )
        Spacer(modifier = Modifier.height(16.dp))
        ServiceOption(
            title = "Remorquage",
            icon = Icons.Filled.Build, // Using Build icon as a placeholder
            isSelected = selectedService == "Remorquage",
            onClick = { selectedService = "Remorquage" }
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onConfirm,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D7FF2)),
            shape = RoundedCornerShape(50)
        ) {
            Text("Confirmer", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ServiceOption(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) Color(0xFF0D7FF2) else Color.LightGray
    val backgroundColor = if (isSelected) Color(0xFF0D7FF2).copy(alpha = 0.1f) else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = borderColor, shape = RoundedCornerShape(12.dp))
            .background(color = backgroundColor, shape = RoundedCornerShape(12.dp))
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFFF3F4F6)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = Color(0xFF111827))
        }
        Spacer(modifier = Modifier.padding(8.dp))
        Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestDetailsScreen(onNavigateBack: () -> Unit) {
    var make by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Détails de la demande", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBackIosNew, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFFF8FAFC).copy(alpha = 0.8f)
                )
            )
        },
        bottomBar = {
            Box(modifier = Modifier.padding(16.dp)) {
                Button(
                    onClick = { /* TODO: Handle send request */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D7FF2)),
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Envoyer la demande", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Informations sur votre véhicule",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            FormInput(label = "Marque", value = make, onValueChange = { make = it }, placeholder = "Ex: Peugeot")
            Spacer(modifier = Modifier.height(16.dp))
            FormInput(label = "Modèle", value = model, onValueChange = { model = it }, placeholder = "Ex: 208")
            Spacer(modifier = Modifier.height(16.dp))
            FormInput(label = "Année", value = year, onValueChange = { year = it }, placeholder = "Ex: 2022")
            Spacer(modifier = Modifier.height(16.dp))
            FormInput(label = "Description du problème", value = description, onValueChange = { description = it }, placeholder = "Décrivez le problème ou la raison de votre demande...", singleLine = false, minLines = 4)
        }
    }
}

@Composable
fun FormInput(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    Column {
        Text(text = label, style = MaterialTheme.typography.bodySmall, color = Color.Gray, modifier = Modifier.padding(bottom = 8.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = singleLine,
            minLines = minLines,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFF0D7FF2),
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = Color(0xFFEEF2F7),
                unfocusedContainerColor = Color(0xFFEEF2F7)
            ),
            shape = RoundedCornerShape(12.dp)
        )
    }
}