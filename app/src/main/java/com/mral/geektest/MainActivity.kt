package com.mral.geektest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.mral.geektest.ui.theme.MyComposeApplicationTheme
import org.maplibre.android.MapLibre
import org.maplibre.android.maps.MapView
import org.maplibre.android.maps.Style
import androidx.compose.ui.Alignment
import androidx.compose.runtime.*                       // remember, mutableStateOf
import com.google.android.gms.location.LocationServices
import androidx.core.app.ActivityCompat
import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.camera.CameraUpdateFactory
import org.maplibre.android.annotations.MarkerOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import android.os.Looper
import com.mral.LocationHelper.*
import org.maplibre.android.annotations.Marker
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.modes.CameraMode
import org.maplibre.android.location.modes.RenderMode


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MapLibre.getInstance(applicationContext)

        setContent {
            MaterialTheme {
                val context = LocalContext.current
                val fusedLocationClient = remember {
                    LocationServices.getFusedLocationProviderClient(context)
                }

                var mapLibreMap by remember { mutableStateOf<org.maplibre.android.maps.MapLibreMap?>(null) }
                var locationCallback by remember { mutableStateOf<LocationCallback?>(null) }
                var sharedCoords by remember { mutableStateOf("") }
                var isSharing by remember { mutableStateOf(false) }
                var busNumber by remember { mutableStateOf("") }
                var showBusNumberDialog by remember { mutableStateOf(false) }

                val permissionLauncher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    Toast.makeText(
                        context,
                        if (isGranted) "Permission accordée" else "Permission refusée",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.LightGray)
                    ) {
                        AndroidView(
                            factory = { ctx ->
                                MapView(ctx).apply {
                                    onCreate(null)
                                    onStart()
                                    onResume()
                                    getMapAsync { map ->
                                        mapLibreMap = map
                                        map.setStyle(
                                            Style.Builder().fromUri(
                                                "https://api.maptiler.com/maps/streets/style.json?key=3VWchhcacNazLBGKImfz"
                                            )
                                        ) { style ->
                                            if (ContextCompat.checkSelfPermission(
                                                    context,
                                                    Manifest.permission.ACCESS_FINE_LOCATION
                                                ) == PackageManager.PERMISSION_GRANTED
                                            ) {
                                                map.locationComponent.apply {
                                                    activateLocationComponent(
                                                        LocationComponentActivationOptions.builder(context, style)
                                                            .useDefaultLocationEngine(true)
                                                            .build()
                                                    )
                                                    isLocationComponentEnabled = true
                                                    cameraMode = CameraMode.TRACKING
                                                    renderMode = RenderMode.COMPASS
                                                }
                                            }
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )

                        FloatingActionButton(
                            onClick = {
                                if (!LocationHelper.hasLocationPermission(context)) {
                                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                    return@FloatingActionButton
                                }

                                locationCallback?.let {
                                    LocationHelper.stopLocationUpdates(fusedLocationClient, it)
                                }

                                locationCallback = LocationHelper.startLocationUpdates(
                                    context,
                                    fusedLocationClient,
                                    5000L
                                ) { location ->
                                    val latLng = LatLng(location.latitude, location.longitude)
                                    mapLibreMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15.0))
                                }
                            },
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp)
                        ) {
                            Icon(Icons.Default.LocationOn, contentDescription = "Ma position")
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = {
                            if (!LocationHelper.hasLocationPermission(context)) {
                                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                return@Button
                            }

                            if (isSharing) {
                                locationCallback?.let {
                                    LocationHelper.stopLocationUpdates(fusedLocationClient, it)
                                }
                                locationCallback = null

                                LocationHelper.stopSharingOnServer(busNumber)

                                sharedCoords = ""
                                busNumber = ""
                                isSharing = false
                            } else {
                                showBusNumberDialog = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isSharing) "Arrêter le partage" else "Partager sa position")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = { /* À implémenter plus tard */ },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Rechercher un numéro de bus")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (isSharing) {
                        Text(
                            text = sharedCoords,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Black
                        )
                    }

                    if (showBusNumberDialog) {
                        var inputText by remember { mutableStateOf("") }

                        AlertDialog(
                            onDismissRequest = { showBusNumberDialog = false },
                            title = { Text("Numéro de bus") },
                            text = {
                                OutlinedTextField(
                                    value = inputText,
                                    onValueChange = { inputText = it },
                                    label = { Text("Entrez le numéro du bus") },
                                    singleLine = true
                                )
                            },
                            confirmButton = {
                                Button(
                                    onClick = {
                                        busNumber = inputText.trim()
                                        showBusNumberDialog = false

                                        LocationHelper.getCurrentLocation(
                                            context,
                                            fusedLocationClient
                                        ) { location ->
                                            if (location != null) {
                                                sharedCoords =
                                                    "Bus n°$busNumber - Lat: ${location.latitude}, Lng: ${location.longitude}"
                                                LocationHelper.sendPositionToServer(
                                                    busNumber,
                                                    location.latitude,
                                                    location.longitude
                                                )
                                            } else {
                                                sharedCoords = "Bus n°$busNumber - Position non disponible"
                                            }
                                        }

                                        locationCallback?.let {
                                            LocationHelper.stopLocationUpdates(fusedLocationClient, it)
                                        }

                                        locationCallback = LocationHelper.startLocationUpdates(
                                            context,
                                            fusedLocationClient,
                                            5000L
                                        ) { location ->
                                            sharedCoords =
                                                "Bus n°$busNumber - Lat: ${location.latitude}, Lng: ${location.longitude}"
                                            LocationHelper.sendPositionToServer(
                                                busNumber,
                                                location.latitude,
                                                location.longitude
                                            )
                                        }

                                        isSharing = true
                                    }
                                ) {
                                    Text("Valider")
                                }
                            },
                            dismissButton = {
                                OutlinedButton(
                                    onClick = { showBusNumberDialog = false }
                                ) {
                                    Text("Annuler")
                                }
                            }
                        )
                    }
                }

                DisposableEffect(Unit) {
                    onDispose {
                        locationCallback?.let {
                            LocationHelper.stopLocationUpdates(fusedLocationClient, it)
                        }
                    }
                }
            }
        }
    }
}