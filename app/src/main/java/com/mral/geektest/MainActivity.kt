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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.mral.geektest.ui.composables.MapView
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

@OptIn(ExperimentalMaterialApi::class)
@SuppressLint("MissingPermission")
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val styleUrl = "https://api.maptiler.com/maps/streets/style.json?key=${context.getString(R.string.maptiler_api_key)}"
    var map: MapLibreMap? by remember { mutableStateOf(null) }
    var hasLocationPermission by remember { mutableStateOf(false) }
    val scaffoldState = rememberBottomSheetScaffoldState()

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

    // JULES: Replaced the original Scaffold with BottomSheetScaffold to implement the bottom sheet UI.
    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        // JULES: The content of the bottom sheet.
        sheetContent = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                // JULES: Moved the button here and renamed its text as requested.
                Button(onClick = { /* TODO: Implement button logic */ }) {
                    Text("depannez-moi")
                }
            }
        },
        // JULES: The FloatingActionButton is placed here to be docked above the bottom sheet.
        floatingActionButton = {
            FloatingActionButton(onClick = {
                locationPermissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }) {
                Icon(Icons.Filled.MyLocation, contentDescription = "My Location")
            }
        }
    ) { paddingValues ->
        // JULES: The main content of the screen (the map) goes here.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            MapView(
                onMapReady = {
                    map = it
                },
                styleUrl = styleUrl,
                initialCenter = LatLng(5.3, -4.0),
                initialZoom = 12.0
            )
        }
    }
}