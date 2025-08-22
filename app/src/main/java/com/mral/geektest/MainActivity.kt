package com.mral.geektest

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CarCrash
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material.rememberBottomSheetState
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapLibre.getInstance(this, getString(R.string.maptiler_api_key), WellKnownTileServer.MapTiler)
        setContent {
            MaterialTheme {
                MainScreen()
            }
        }
    }
}

sealed class SheetContentState {
    object Home : SheetContentState()
    data class Details(val serviceType: String) : SheetContentState()
}

@OptIn(ExperimentalMaterialApi::class)
@SuppressLint("MissingPermission")
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val styleUrl = "https://api.maptiler.com/maps/streets/style.json?key=${context.getString(R.string.maptiler_api_key)}"
    var map: MapLibreMap? by remember { mutableStateOf(null) }
    var hasLocationPermission by remember { mutableStateOf(false) }

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberBottomSheetState(initialValue = BottomSheetValue.Collapsed)
    )
    val modalSheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    val scope = rememberCoroutineScope()
    var sheetContentState by remember { mutableStateOf<SheetContentState>(SheetContentState.Home) }

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
                map?.locationComponent?.let {
                    val locationEngineRequest = LocationEngineRequest.Builder(750)
                        .setFastestInterval(750)
                        .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                        .build()

                    it.activateLocationComponent(
                        LocationComponentActivationOptions.builder(context, style)
                            .useDefaultLocationEngine(true)
                            .locationEngineRequest(locationEngineRequest)
                            .build()
                    )
                    it.isLocationComponentEnabled = true
                    it.cameraMode = CameraMode.TRACKING
                    it.renderMode = RenderMode.COMPASS
                }
            }
        }
    }

    ModalBottomSheetLayout(
        sheetState = modalSheetState,
        sheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        sheetContent = {
            ServiceSelectionSheetContent(onConfirm = { serviceType ->
                scope.launch {
                    modalSheetState.hide()
                    sheetContentState = SheetContentState.Details(serviceType)
                    scaffoldState.bottomSheetState.expand()
                }
            })
        }
    ) {
        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetPeekHeight = if (sheetContentState is SheetContentState.Home) 250.dp else 0.dp,
            sheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            sheetContent = {
                when (val state = sheetContentState) {
                    is SheetContentState.Home -> HomeSheetContent(
                        onShowServiceSelection = { scope.launch { modalSheetState.show() } }
                    )
                    is SheetContentState.Details -> RequestDetailsSheetContent(
                        serviceType = state.serviceType,
                        onSend = {
                            // TODO: Implement final submission
                        }
                    )
                }
            }
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                MapView(
                    modifier = Modifier.fillMaxSize(),
                    onMapReady = { map = it },
                    styleUrl = styleUrl,
                    initialCenter = LatLng(5.3, -4.0),
                    initialZoom = 12.0
                )

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
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                ) {
                    Icon(Icons.Filled.MyLocation, "My Location")
                }
            }
        }
    }
}

@Composable
fun HomeSheetContent(onShowServiceSelection: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF3F4F6))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .height(4.dp)
                .fillMaxWidth(0.15f)
                .clip(RoundedCornerShape(2.dp))
                .background(Color.LightGray)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onShowServiceSelection,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
        ) {
            Text("Demander un dépannage")
        }
        Spacer(modifier = Modifier.height(16.dp))

        var selectedItem by remember { mutableStateOf(0) }
        val items = listOf("Accueil", "Dépanneurs", "Paramètres")
        val icons = listOf(Icons.Filled.Home, Icons.Filled.Construction, Icons.Filled.Settings)

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
                )
            }
        }
    }
}

@Composable
fun ServiceSelectionSheetContent(onConfirm: (String) -> Unit) {
    var selectedService by remember { mutableStateOf("Dépannage") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
            icon = Icons.Filled.CarCrash,
            isSelected = selectedService == "Remorquage",
            onClick = { selectedService = "Remorquage" }
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { onConfirm(selectedService) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
        ) {
            Text("Confirmer")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestDetailsSheetContent(serviceType: String, onSend: () -> Unit) {
    var make by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var description by remember { mutableStateOf(serviceType) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .height(4.dp)
                .fillMaxWidth(0.15f)
                .clip(RoundedCornerShape(2.dp))
                .background(Color.LightGray)
                .align(Alignment.CenterHorizontally)
        )
        Text(
            "Détails de votre demande",
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            OutlinedTextField(
                value = make,
                onValueChange = { make = it },
                label = { Text("Marque") },
                placeholder = { Text("Ex: Peugeot") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = model,
                onValueChange = { model = it },
                label = { Text("Modèle") },
                placeholder = { Text("Ex: 208") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = year,
                onValueChange = { year = it },
                label = { Text("Année") },
                placeholder = { Text("Ex: 2022") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description du problème") },
                placeholder = { Text("Décrivez le problème ou la raison de votre demande...") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 4
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onSend,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text("Envoyer la demande")
            }
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