package com.mral.geektest

import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CarCrash
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.window.Dialog
import com.mral.geektest.ui.composables.MapView
import com.mral.geektest.ui.theme.MyComposeApplicationTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.maplibre.android.MapLibre
import org.maplibre.android.WellKnownTileServer
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.location.LocationComponentActivationOptions
import org.maplibre.android.location.engine.LocationEngineRequest
import org.maplibre.android.location.modes.CameraMode
import org.maplibre.android.location.modes.RenderMode
import org.maplibre.android.maps.MapLibreMap

data class ServiceProvider(
    val id: Int,
    val name: String,
    val initials: String,
    val rating: Double,
    val time: String,
)

val sampleServices = listOf(
    ServiceProvider(1, "Garage Dubois", "GD", 4.5, "5 min"),
    ServiceProvider(2, "Auto-Réparation Express", "AR", 4.8, "8 min"),
    ServiceProvider(3, "SOS Mécanique", "SM", 4.2, "12 min"),
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MapLibre.getInstance(this, getString(R.string.maptiler_api_key), WellKnownTileServer.MapTiler)
        setContent {
            MyComposeApplicationTheme {
                MainScreen()
            }
        }
    }
}

sealed class BottomSheetWorkflowState {
    object Searching : BottomSheetWorkflowState()
    object Results : BottomSheetWorkflowState()
    data class ProblemDetails(val serviceProvider: ServiceProvider) : BottomSheetWorkflowState()
    data class Confirmation(
        val serviceProvider: ServiceProvider,
        val problem: String,
        val description: String
    ) : BottomSheetWorkflowState()
}

sealed class RequestStatus {
    object Confirmation : RequestStatus()
    object Sending : RequestStatus()
    object Sent : RequestStatus()
    object Tracking : RequestStatus()
}

@Composable
fun BottomSheetContent(onClose: () -> Unit) {
    var workflowState by remember { mutableStateOf<BottomSheetWorkflowState>(BottomSheetWorkflowState.Searching) }
    var requestStatus by remember { mutableStateOf<RequestStatus?>(null) }

    LaunchedEffect(requestStatus) {
        when (requestStatus) {
            is RequestStatus.Sending -> {
                delay(3000)
                requestStatus = RequestStatus.Sent
            }
            is RequestStatus.Sent -> {
                delay(2000)
                requestStatus = RequestStatus.Tracking
            }
            else -> {
                // No action needed for other states
            }
        }
    }

    if (requestStatus == null) {
        // Initial workflow to gather information
        LaunchedEffect(workflowState) {
            if (workflowState is BottomSheetWorkflowState.Searching) {
                delay(5000)
                workflowState = BottomSheetWorkflowState.Results
            }
        }

        when (val state = workflowState) {
            is BottomSheetWorkflowState.Searching -> {
                SearchInProgressSheetContent(onClose = onClose)
            }
            is BottomSheetWorkflowState.Results -> {
                MechanicListSheetContent(
                    onClose = onClose,
                    onConfirm = { serviceProvider ->
                        workflowState = BottomSheetWorkflowState.ProblemDetails(serviceProvider)
                    }
                )
            }
            is BottomSheetWorkflowState.ProblemDetails -> {
                ProblemDetailsSheetContent(
                    serviceProvider = state.serviceProvider,
                    onClose = onClose,
                    onBack = { workflowState = BottomSheetWorkflowState.Results },
                    onConfirm = { problem, description ->
                        workflowState = BottomSheetWorkflowState.Confirmation(
                            serviceProvider = state.serviceProvider,
                            problem = problem,
                            description = description
                        )
                    }
                )
            }
            is BottomSheetWorkflowState.Confirmation -> {
                ConfirmationSheetContent(
                    state = state,
                    onClose = onClose,
                    onBack = { workflowState = BottomSheetWorkflowState.ProblemDetails(state.serviceProvider) },
                    onConfirm = { requestStatus = RequestStatus.Confirmation }
                )
            }
        }
    } else {
        // Post-confirmation workflow
        when (requestStatus) {
            is RequestStatus.Confirmation -> {
                ConfirmationDialogContent(
                    onConfirm = { requestStatus = RequestStatus.Sending },
                    onCancel = { requestStatus = null }
                )
            }
            is RequestStatus.Sending -> {
                SendingContent()
            }
            is RequestStatus.Sent -> {
                SentContent()
            }
            is RequestStatus.Tracking -> {
                TrackingContent()
            }
            null -> {
                // Should not happen in this branch
            }
        }
    }
}

@Composable
fun MechanicListSheetContent(
    onClose: () -> Unit,
    onConfirm: (ServiceProvider) -> Unit
) {
    var selectedServiceId by remember { mutableStateOf<Int?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Dépanneurs à proximité", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Sélectionnez un professionnel pour continuer.", color = Color.Gray)
            }
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(sampleServices) { service ->
                val isSelected = service.id == selectedServiceId
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            selectedServiceId = service.id
                        }
                        .border(
                            width = if (isSelected) 2.dp else 0.dp,
                            color = if (isSelected) Color.Red else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(Color.LightGray, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(service.initials, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(service.name, fontWeight = FontWeight.Bold)
                            Row {
                                Icon(Icons.Default.Star, contentDescription = "Rating", tint = Color.Yellow)
                                Text("${service.rating}")
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.Default.Schedule, contentDescription = "Time", tint = Color.Gray)
                                Text(service.time)
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                sampleServices.find { it.id == selectedServiceId }?.let {
                    onConfirm(it)
                }
            },
            enabled = selectedServiceId != null,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899)) // Pink color
        ) {
            Text("Sélectionnez un dépanneur", modifier = Modifier.padding(vertical = 8.dp))
        }
        Spacer(modifier = Modifier.height(50.dp))
    }
}

@Composable
fun ProblemButton(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray
        )
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 16.dp)
        ) {
            Icon(
                icon,
                contentDescription = label,
                modifier = Modifier.size(24.dp),
                tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                label,
                textAlign = TextAlign.Center,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Unspecified
            )
        }
    }
}

@Composable
fun ProblemDetailsSheetContent(
    serviceProvider: ServiceProvider,
    onClose: () -> Unit,
    onBack: () -> Unit,
    onConfirm: (problem: String, description: String) -> Unit
) {
    var selectedProblem by remember { mutableStateOf<String?>(null) }
    var description by remember { mutableStateOf("") }
    val problems = listOf(
        "Batterie" to Icons.Default.BatteryChargingFull,
        "Pneu crevé" to Icons.Default.Build, // Using Build for Wrench
        "Panne d'essence" to Icons.Default.LocalGasStation,
        "Moteur" to Icons.Default.ReportProblem,
        "Autre" to Icons.Default.Build, // Using Build for Wrench
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Quel est le problème ?", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Donnez des détails pour une meilleure prise en charge.", color = Color.Gray)
            }
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(problems) { (label, icon) ->
                val isSelected = selectedProblem == label
                ProblemButton(
                    icon = icon,
                    label = label,
                    isSelected = isSelected,
                    onClick = { selectedProblem = label }
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            placeholder = { Text("Décrivez le problème plus en détail") },
            modifier = Modifier
                .fillMaxWidth()
                .height(128.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                selectedProblem?.let { problem ->
                    onConfirm(problem, description)
                }
            },
            enabled = selectedProblem != null,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Envoyer la demande", modifier = Modifier.padding(vertical = 8.dp))
        }
        Spacer(modifier = Modifier.height(50.dp))
    }
}

@Composable
fun ConfirmationSheetContent(
    state: BottomSheetWorkflowState.Confirmation,
    onClose: () -> Unit,
    onBack: () -> Unit,
    onConfirm: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("Confirmez votre demande", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Veuillez vérifier les informations avant d'envoyer.", color = Color.Gray)
            }
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Summary Box
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Text("Récapitulatif", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))

            SummaryRow(icon = Icons.Default.Person, label = "Dépanneur", value = state.serviceProvider.name)
            SummaryRow(icon = Icons.Default.ReportProblem, label = "Problème", value = state.problem)
            SummaryRow(icon = Icons.Default.ReportProblem, label = "Chat", value = "\"${state.description}\"") // Using ReportProblem as placeholder for Chat

            val currentDateTime = java.text.SimpleDateFormat("dd MMMM 'à' HH:mm", java.util.Locale.FRENCH).format(java.util.Date())
            SummaryRow(icon = Icons.Default.Schedule, label = "Quand", value = currentDateTime)
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Confirm Button
        Button(
            onClick = onConfirm,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEC4899)) // Rose color from mockup
        ) {
            Text("Confirmer et envoyer", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(50.dp))
    }
}

// Helper composable for the summary rows
@Composable
fun SummaryRow(icon: ImageVector, label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = label, tint = Color.Gray)
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, color = Color.Gray)
        }
        Text(value, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
    }
}

@Composable
fun ConfirmationDialogContent(
    onConfirm: () -> Unit,
    onCancel: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Confirmation requise",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Une fois votre demande envoyée, elle ne pourra plus être modifiée. Le déplacement d'un dépanneur requiert des moyens logistiques importants. Êtes-vous sûr de vouloir continuer ?",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onConfirm,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
        ) {
            Text("Envoyer ma demande", modifier = Modifier.padding(vertical = 8.dp))
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray)
        ) {
            Text("Annuler", modifier = Modifier.padding(vertical = 8.dp), color = Color.Black)
        }
    }
}

@Composable
fun SendingContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(500.dp)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.size(144.dp)
        ) {
            val infiniteTransition = rememberInfiniteTransition(label = "sending_spinner")
            val angle by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 360f,
                animationSpec = infiniteRepeatable(
                    animation = tween(2000, easing = LinearEasing),
                    repeatMode = RepeatMode.Restart
                ),
                label = "angle"
            )

            CircularProgressIndicator(
                modifier = Modifier.size(112.dp),
                strokeWidth = 4.dp
            )
            Text("N", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text("Envoi de la demande...", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Veuillez patienter.", color = Color.Gray)
    }
}

@Composable
fun SentContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(500.dp)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = "Success",
            tint = Color(0xFF4CAF50), // Green color
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text("Demande envoyée !", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("En attente de confirmation de la part de Garage Dubois.", color = Color.Gray, textAlign = TextAlign.Center)
    }
}

@Composable
fun TrackingContent() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text("Demande en cours", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Suivez l'avancement de votre demande en temps réel.", color = Color.Gray)
        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Suivi de votre demande", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("#A4B7-89C1", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("GD", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Garage Dubois", fontWeight = FontWeight.Bold)
                        Text("En attente", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                    }
                }
                Divider(modifier = Modifier.padding(vertical = 16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Arrivée estimée :", color = Color.Gray)
                    Text("12 minutes", fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Column {
                Text("Votre véhicule", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Text("Toyota Camry (ABC-1234)", fontWeight = FontWeight.Medium)
            }
            Column {
                Text("Problème signalé", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Text("Moteur", fontWeight = FontWeight.Medium)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { /*TODO*/ }, modifier = Modifier.fillMaxWidth()) {
                Text("Appeler le dépanneur")
            }
            OutlinedButton(onClick = { /*TODO*/ }, modifier = Modifier.fillMaxWidth()) {
                Text("Envoyer un message")
            }
        }
    }
}


sealed class SheetContentState {
    object Home : SheetContentState()
    data class Details(val serviceType: String) : SheetContentState()
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("MissingPermission")
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val styleUrl = "https://api.maptiler.com/maps/streets/style.json?key=${context.getString(R.string.maptiler_api_key)}"
    var map: MapLibreMap? by remember { mutableStateOf(null) }
    var hasLocationPermission by remember { mutableStateOf(false) }
    var selectedTabIndex by remember { mutableStateOf(0) }
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true,
        confirmValueChange = { it != SheetValue.Hidden }
    )
    val scope = rememberCoroutineScope()
    var showBottomSheet by remember { mutableStateOf(false) }

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

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                Column(modifier = Modifier.background(Color.White)) {
                    RescueMapHeader()
                    RescueMapSubHeader()
                }
            },
            bottomBar = {
                RescueMapBottomNav(
                    selectedTabIndex = selectedTabIndex,
                    onTabSelected = { selectedTabIndex = it }
                )
            },
            containerColor = Color(0xFFF3F4F6)
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .padding(paddingValues)
                    .fillMaxSize()
            ) {
                MapView(
                    modifier = Modifier.fillMaxSize(),
                    onMapReady = { map = it },
                    styleUrl = styleUrl,
                    initialCenter = LatLng(5.3, -4.0),
                    initialZoom = 12.0
                )
                // The large button is part of the content
                Button(
                    onClick = { showBottomSheet = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .align(Alignment.BottomCenter),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text(
                        text = "DÉPANNEZ-MOI",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }

        if (showBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = sheetState,
                dragHandle = null
            ) {
                BottomSheetContent(onClose = { showBottomSheet = false })
            }
        }
    }
}

@Composable
fun RescueMapHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.Red, CircleShape)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.DirectionsCar,
                    contentDescription = "Car",
                    tint = Color.White
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("RescueMap", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Row {
            Icon(Icons.Filled.BugReport, contentDescription = "Bug Report", tint = Color.Gray)
            Spacer(modifier = Modifier.width(16.dp))
            Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = Color.Gray)
        }
    }
}

@Composable
fun RescueMapSubHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.LocationOn, contentDescription = "Location", tint = Color.Gray)
        Spacer(modifier = Modifier.width(4.dp))
        Text("123 Rue Fictive, Paris", color = Color.Gray)
        Spacer(modifier = Modifier.width(8.dp))
        Icon(Icons.Filled.Refresh, contentDescription = "Refresh", tint = Color.Gray)
    }
}

@Composable
fun RescueMapBottomNav(
    selectedTabIndex: Int,
    onTabSelected: (Int) -> Unit
) {
    val items = listOf("Carte", "Demandes", "Profil")
    val icons = listOf(Icons.Filled.Map, Icons.AutoMirrored.Filled.List, Icons.Filled.Person)

    NavigationBar(
        modifier = Modifier.clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
        containerColor = Color.White
    ) {
        items.forEachIndexed { index, item ->
            val isSelected = selectedTabIndex == index
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(index) },
                label = {
                    Text(
                        text = item,
                        color = if (isSelected) Color.Red else Color.Gray
                    )
                },
                icon = {
                    if (item == "Carte") {
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isSelected) Color.Red else Color.LightGray,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(4.dp)
                        ) {
                            Icon(
                                imageVector = icons[index],
                                contentDescription = item,
                                tint = if (isSelected) Color.White else Color.Gray
                            )
                        }
                    } else {
                        Icon(
                            imageVector = icons[index],
                            contentDescription = item,
                            tint = if (isSelected) Color.Red else Color.Gray
                        )
                    }
                }
            )
        }
    }
}

@Composable
fun SearchInProgressSheetContent(onClose: () -> Unit) {
    Box {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            IconButton(
                onClick = onClose,
                modifier = Modifier.align(Alignment.End)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Recherche en cours...", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Nous recherchons les dépanneurs les plus proches de votre position.", textAlign = TextAlign.Center, color = Color.Gray)
            Spacer(modifier = Modifier.height(24.dp))
            CircularProgressIndicator(color = Color.Red)
            Spacer(modifier = Modifier.height(24.dp))
            Text("Analyse de votre position...", color = Color.Gray)
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
fun AppBottomNavigation(selectedItem: Int, onItemSelected: (Int) -> Unit) {
    val items = listOf("Accueil", "Dépanneurs", "Paramètres")
    val icons = listOf(Icons.Filled.Home, Icons.Filled.Construction, Icons.Filled.Settings)

    NavigationBar(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                icon = { Icon(icons[index], contentDescription = item) },
                label = { Text(item) },
                selected = selectedItem == index,
                onClick = { onItemSelected(index) },
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

@Composable
fun HomeSheetContent(onShowServiceSelection: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF3F4F6)),
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
        Column(modifier = Modifier.padding(all = 16.dp)) {
            Button(
                onClick = onShowServiceSelection,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            ) {
                Text("Demander un dépannage")
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