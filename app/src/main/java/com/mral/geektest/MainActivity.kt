package com.mral.geektest

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import com.mral.geektest.ui.composables.MapView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.maplibre.android.MapLibre
import org.maplibre.android.WellKnownTileServer
import org.maplibre.android.camera.CameraPosition
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.engine.LocationEngineRequest
import org.maplibre.android.location.modes.CameraMode
import org.maplibre.android.location.modes.RenderMode
import org.maplibre.android.maps.MapLibreMap
import org.maplibre.android.snapshotter.MapSnapshotter
import java.util.*

data class RequestData(
    val serviceType: String,
    val vehicleMake: String,
    val vehicleModel: String,
    val vehicleYear: String,
    val problemDescription: String,
    val location: LatLng,
    val address: String
)

sealed class Screen {
    object Map : Screen()
    data class Details(val serviceType: String) : Screen()
    data class Confirmation(val requestData: RequestData) : Screen()
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapLibre.getInstance(this, getString(R.string.maptiler_api_key), WellKnownTileServer.MapTiler)
        setContent {
            MaterialTheme {
                var currentScreen by remember { mutableStateOf<Screen>(Screen.Map) }
                var serviceType by remember { mutableStateOf("Dépannage") }
                var lastKnownLocation by remember { mutableStateOf(LatLng(5.3, -4.0)) }
                var currentAddress by remember { mutableStateOf("Loading address...") }
                val context = LocalContext.current

                LaunchedEffect(lastKnownLocation) {
                    currentAddress = getAddressFromCoordinates(context, lastKnownLocation)
                }

                when (val screen = currentScreen) {
                    is Screen.Map -> MainScreen(
                        onNavigateToDetails = { selectedService ->
                            serviceType = selectedService
                            currentScreen = Screen.Details(selectedService)
                        },
                        onLocationUpdate = { latLng ->
                            lastKnownLocation = latLng
                        }
                    )
                    is Screen.Details -> RequestDetailsScreen(
                        serviceType = screen.serviceType,
                        onNavigateBack = { currentScreen = Screen.Map },
                        onNavigateToConfirmation = { vehicleMake, vehicleModel, vehicleYear, problemDescription ->
                            currentScreen = Screen.Confirmation(
                                RequestData(
                                    serviceType = serviceType,
                                    vehicleMake = vehicleMake,
                                    vehicleModel = vehicleModel,
                                    vehicleYear = vehicleYear,
                                    problemDescription = problemDescription,
                                    location = lastKnownLocation,
                                    address = currentAddress
                                )
                            )
                        }
                    )
                    is Screen.Confirmation -> ConfirmationScreen(
                        requestData = screen.requestData,
                        onNavigateBack = { currentScreen = Screen.Details(screen.requestData.serviceType) }
                    )
                }
            }
        }
    }
}

suspend fun getAddressFromCoordinates(context: Context, latLng: LatLng): String = withContext(Dispatchers.IO) {
    try {
        val geocoder = Geocoder(context, Locale.getDefault())
        val addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
        if (addresses?.isNotEmpty() == true) {
            addresses[0].getAddressLine(0)
        } else {
            "Address not found"
        }
    } catch (e: Exception) {
        Log.e("Geocoder", "Failed to get address", e)
        "Could not retrieve address"
    }
}


@OptIn(ExperimentalMaterialApi::class)
@SuppressLint("MissingPermission")
@Composable
fun MainScreen(onNavigateToDetails: (String) -> Unit, onLocationUpdate: (LatLng) -> Unit) {
    val context = LocalContext.current
    val styleUrl = "https://api.maptiler.com/maps/streets/style.json?key=${context.getString(R.string.maptiler_api_key)}"
    var map: MapLibreMap? by remember { mutableStateOf(null) }
    var hasLocationPermission by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(initialValue = ModalBottomSheetValue.Hidden)
    val scope = rememberCoroutineScope()
    var selectedService by remember { mutableStateOf("Dépannage") }

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
                    it.addOnLocationClickListener {
                        it.lastKnownLocation?.let { loc -> onLocationUpdate(LatLng(loc.latitude, loc.longitude)) }
                    }
                }
            }
        }
    }

    ModalBottomSheetLayout(
        sheetState = sheetState,
        sheetShape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        sheetContent = {
            ServiceSelectionSheetContent(
                initialService = selectedService,
                onConfirm = { service ->
                    selectedService = service
                    scope.launch {
                        sheetState.hide()
                        onNavigateToDetails(service)
                    }
                }
            )
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color.White.copy(alpha = 0.8f))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
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

                    var selectedNavItem by remember { mutableStateOf(0) }
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
                                selected = selectedNavItem == index,
                                onClick = { selectedNavItem = index },
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
fun ServiceSelectionSheetContent(initialService: String, onConfirm: (String) -> Unit) {
    var selectedService by remember { mutableStateOf(initialService) }

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
fun RequestDetailsScreen(
    serviceType: String,
    onNavigateBack: () -> Unit,
    onNavigateToConfirmation: (String, String, String, String) -> Unit
) {
    var make by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var description by remember { mutableStateOf(serviceType) }

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
                    onClick = { onNavigateToConfirmation(make, model, year, description) },
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmationScreen(requestData: RequestData, onNavigateBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Confirmation", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center) },
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
                    onClick = { /* TODO: Final Submit */ },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D7FF2)),
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Confirmer et envoyer", fontSize = 18.sp, fontWeight = FontWeight.Bold)
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
                .padding(bottom = 16.dp)
        ) {
            SummarySection(requestData)
            Spacer(modifier = Modifier.height(24.dp))
            LocationSection(requestData)
        }
    }
}

@Composable
fun SummarySection(data: RequestData) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Résumé de la demande",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color.Gray
        )
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(horizontal = 16.dp)
        ) {
            SummaryRow(icon = Icons.Filled.Build, title = "Service demandé", details = data.serviceType)
            Divider(color = Color.LightGray.copy(alpha = 0.5f))
            SummaryRow(icon = Icons.Filled.DirectionsCar, title = "Véhicule", details = "${data.vehicleMake} ${data.vehicleModel} ${data.vehicleYear}")
            Divider(color = Color.LightGray.copy(alpha = 0.5f))
            SummaryRow(icon = Icons.Filled.ReportProblem, title = "Description du problème", details = data.problemDescription)
        }
    }
}

@Composable
fun LocationSection(requestData: RequestData) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(
            text = "Localisation",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color.Gray
        )
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
        ) {
            MapSnapshot(latLng = requestData.location)
            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = "Location",
                    tint = Color.Gray,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text("Votre position actuelle", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    Text(requestData.address, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun SummaryRow(icon: ImageVector, title: String, details: String) {
    Row(modifier = Modifier.padding(vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = Color.Gray,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(50))
                .background(Color.LightGray.copy(alpha = 0.3f))
                .padding(12.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = title, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            Text(text = details, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun MapSnapshot(latLng: LatLng) {
    var image by remember { mutableStateOf<android.graphics.Bitmap?>(null) }
    val context = LocalContext.current
    val styleUrl = "https://api.maptiler.com/maps/streets/style.json?key=${context.getString(R.string.maptiler_api_key)}"

    LaunchedEffect(latLng) {
        val options = MapSnapshotter.Options(500, 300)
            .withStyle(styleUrl)
            .withCameraPosition(
                CameraPosition.Builder()
                    .target(latLng)
                    .zoom(15.0)
                    .build()
            )

        val snapshotter = MapSnapshotter(context, options)
        snapshotter.start(
            { snapshot ->
                val mutableBitmap = snapshot.bitmap.copy(Bitmap.Config.ARGB_8888, true)
                val canvas = Canvas(mutableBitmap)
                val markerDrawable = ContextCompat.getDrawable(context, R.drawable.ic_marker_pin)
                markerDrawable?.let {
                    val markerBitmap = it.toBitmap()
                    val markerPosition = snapshot.pixelForLatLng(latLng)
                    canvas.drawBitmap(
                        markerBitmap,
                        markerPosition.x - markerBitmap.width / 2,
                        markerPosition.y - markerBitmap.height, // Align to bottom of the pin
                        null
                    )
                }
                image = mutableBitmap
            },
            { error ->
                Log.e("MapSnapshot", "Failed to generate snapshot: $error")
            }
        )
    }

    if (image != null) {
        Image(
            bitmap = image!!.asImageBitmap(),
            contentDescription = "Map snapshot with marker",
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .background(Color.LightGray),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}