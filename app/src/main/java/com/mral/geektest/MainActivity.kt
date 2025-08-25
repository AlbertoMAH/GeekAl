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
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material.icons.filled.LocalParking
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Route
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
        val description: String,
        val vehicleInfo: String,
        val location: String?,
        val isPhotoAdded: Boolean
    ) : BottomSheetWorkflowState()
}

@Composable
fun BottomSheetContent(onClose: () -> Unit) {
    var workflowState by remember { mutableStateOf<BottomSheetWorkflowState>(BottomSheetWorkflowState.Searching) }
    var uiState by remember { mutableStateOf("form") }
    var requestDetails by remember { mutableStateOf<BottomSheetWorkflowState.Confirmation?>(null) }


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
                    // We have the service provider, move to the problem details state
                    // and prepare the data for the new form.
                    requestDetails = BottomSheetWorkflowState.Confirmation(
                        serviceProvider = serviceProvider,
                        problem = "", // Will be filled by the form
                        description = "", // Will be filled by the form
                        vehicleInfo = "", // Will be filled by the form
                        location = null, // Will be filled by the form
                        isPhotoAdded = false // Will be filled by the form
                    )
                    workflowState = BottomSheetWorkflowState.ProblemDetails(serviceProvider)
                }
            )
        }
        is BottomSheetWorkflowState.ProblemDetails -> {
            if (uiState == "form") {
                ProblemDetailsForm(
                    onContinueClick = { breakdownType, breakdownDescription, vehicleInfo, location, isPhotoAdded ->
                        // Update the request details with the form data
                        requestDetails = requestDetails?.copy(
                            problem = breakdownType,
                            description = breakdownDescription,
                            vehicleInfo = vehicleInfo,
                            location = location,
                            isPhotoAdded = isPhotoAdded
                        )
                        // Transition to the tracking screen
                        uiState = "tracking"
                    }
                )
            } else { // uiState == "tracking"
                requestDetails?.let {
                    ActiveRequestCard(
                        requestDetails = it,
                        onCancel = {
                            // Reset the UI state to go back to the form
                            uiState = "form"
                        }
                    )
                }
            }
        }
        is BottomSheetWorkflowState.Confirmation -> {
            // This state is now handled within the new workflow.
            // We can leave this empty or navigate back if reached unexpectedly.
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
                Text("Sélectionnez un professionnel pour continuer.", color = MaterialTheme.colorScheme.onSurfaceVariant)
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
                            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
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
                                .background(MaterialTheme.colorScheme.surfaceVariant, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(service.initials, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(service.name, fontWeight = FontWeight.Bold)
                            Row {
                                Icon(Icons.Default.Star, contentDescription = "Rating", tint = MaterialTheme.colorScheme.secondary)
                                Text("${service.rating}")
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(Icons.Default.Schedule, contentDescription = "Time", tint = MaterialTheme.colorScheme.onSurfaceVariant)
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
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Text("Sélectionnez un dépanneur", modifier = Modifier.padding(vertical = 8.dp))
        }
        Spacer(modifier = Modifier.height(50.dp))
    }
}

@Composable
fun ProblemDetailsForm(
    onContinueClick: (
        breakdownType: String,
        breakdownDescription: String,
        vehicleInfo: String,
        location: String?,
        isPhotoAdded: Boolean
    ) -> Unit
) {
    var breakdownType by remember { mutableStateOf<String?>(null) }
    var breakdownDescription by remember { mutableStateOf("") }
    var vehicleInfo by remember { mutableStateOf("") }
    var location by remember { mutableStateOf<String?>(null) }
    var isPhotoAdded by remember { mutableStateOf(false) }
    var vehicleInfoError by remember { mutableStateOf(false) }

    val isFormValid = breakdownType != null && (breakdownType != "Autre" || breakdownDescription.trim().isNotEmpty()) && !vehicleInfoError && vehicleInfo.trim().isNotEmpty()

    val breakdownOptions = listOf(
        "Batterie" to Icons.Default.BatteryChargingFull,
        "Moteur" to Icons.Default.Build,
        "Pneu" to Icons.Default.DirectionsCar,
        "Essence" to Icons.Default.LocalGasStation,
        "Autre" to Icons.Default.ReportProblem
    )

    val locationOptions = listOf(
        "Route" to Icons.Default.Route,
        "Parking" to Icons.Default.LocalParking,
        "Domicile" to Icons.Default.Home
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Quel est le problème ?",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        Text(
            text = "Donnez des détails pour une meilleure prise en charge.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Divider()

        // Type de panne
        Text(
            text = "Type de panne",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.height(256.dp)
        ) {
            items(breakdownOptions) { (type, icon) ->
                val isSelected = breakdownType == type
                OutlinedButton(
                    onClick = { breakdownType = type },
                    modifier = Modifier.height(80.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = if (isSelected) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary) else ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                    border = BorderStroke(width = if (isSelected) 0.dp else 1.dp, color = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outline)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Icon(icon, contentDescription = type, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(type, textAlign = TextAlign.Center, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    }
                }
            }
        }

        if (breakdownType == "Autre") {
            Spacer(modifier = Modifier.height(16.dp))
            Text("Description du problème", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            OutlinedTextField(
                value = breakdownDescription,
                onValueChange = { breakdownDescription = it },
                placeholder = { Text("Veuillez décrire votre problème en détail...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(12.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        Divider()
        Spacer(modifier = Modifier.height(16.dp))

        // Marque et modèle du véhicule
        Text("Marque et modèle du véhicule", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = vehicleInfo,
            onValueChange = {
                vehicleInfo = it
                if (vehicleInfoError) {
                    vehicleInfoError = false
                }
            },
            placeholder = { Text("Ex : Tesla Model 3") },
            modifier = Modifier.fillMaxWidth(),
            isError = vehicleInfoError,
            shape = RoundedCornerShape(12.dp)
        )
        if (vehicleInfoError) {
            Text("Veuillez entrer le modèle de votre véhicule.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(start = 16.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))
        Divider()
        Spacer(modifier = Modifier.height(16.dp))

        // Lieu de l'incident
        Text("Lieu de l'incident", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.height(88.dp)
        ) {
            items(locationOptions) { (type, icon) ->
                val isSelected = location == type
                 OutlinedButton(
                    onClick = { location = type },
                    modifier = Modifier.height(80.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = if (isSelected) ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = MaterialTheme.colorScheme.onPrimary) else ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
                    border = BorderStroke(width = if (isSelected) 0.dp else 1.dp, color = if (isSelected) Color.Transparent else MaterialTheme.colorScheme.outline)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                        Icon(icon, contentDescription = type, modifier = Modifier.size(24.dp))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(type, textAlign = TextAlign.Center, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Divider()
        Spacer(modifier = Modifier.height(16.dp))

        // Bouton pour ajouter une photo
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Button(
                onClick = { isPhotoAdded = !isPhotoAdded },
                colors = if (isPhotoAdded) ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)) else ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                shape = CircleShape,
                modifier = Modifier.fillMaxWidth(0.8f).height(50.dp)
            ) {
                if (isPhotoAdded) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Photo Added")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Photo ajoutée ✓")
                } else {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Add Photo")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ajouter une photo")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Bouton Continuer
        Button(
            onClick = {
                if (vehicleInfo.trim().isEmpty()) {
                    vehicleInfoError = true
                } else {
                    breakdownType?.let {
                        onContinueClick(
                            it,
                            breakdownDescription,
                            vehicleInfo,
                            location,
                            isPhotoAdded
                        )
                    }
                }
            },
            enabled = isFormValid,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Continuer", fontSize = 18.sp)
        }
    }
}

@Composable
fun Avatar(name: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(MaterialTheme.colorScheme.primaryContainer, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(text = name, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun ActiveRequestCard(
    requestDetails: BottomSheetWorkflowState.Confirmation,
    onCancel: () -> Unit
) {
    var currentStep by remember { mutableStateOf(1) } // 0: Demande envoyée, 1: Confirmé par le dépanneur...

    val timelineSteps = listOf(
        0 to "Demande envoyée",
        1 to "Confirmé par le dépanneur",
        2 to "Dépanneur en route",
        3 to "Intervention terminée"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
            // Header
            Text(
                text = "Suivi de votre demande",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "ID de la demande: #A4B7-89C1",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))

            // Dépanneur info
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        BorderStroke(1.dp, MaterialTheme.colorScheme.primaryContainer),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Avatar(
                    name = requestDetails.serviceProvider.initials,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(requestDetails.serviceProvider.name, fontWeight = FontWeight.Bold)
                    Text("Arrivée estimée : 12 minutes", style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Timeline
            Column {
                timelineSteps.forEach { (stepId, label) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(24.dp)) {
                            when {
                                stepId < currentStep -> Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50))
                                stepId == currentStep -> CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                else -> Icon(Icons.Default.RadioButtonUnchecked, contentDescription = null, tint = Color.Gray)
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(label, fontWeight = FontWeight.SemiBold)
                    }
                    if (stepId < timelineSteps.last().first) {
                        Box(modifier = Modifier.padding(start = 11.dp).width(2.dp).height(24.dp).background(Color.Gray))
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))

            // Récapitulatif
            Text("Récapitulatif de l'intervention", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            SummaryRow(icon = Icons.Default.DirectionsCar, label = "Votre véhicule", value = requestDetails.vehicleInfo)
            SummaryRow(icon = Icons.Default.LocationOn, label = "Lieu", value = requestDetails.location ?: "Non spécifié")
            SummaryRow(icon = Icons.Default.ReportProblem, label = "Problème signalé", value = requestDetails.problem)
            if (requestDetails.description.isNotEmpty()) {
                SummaryRow(icon = Icons.Default.Message, label = "Description", value = "\"${requestDetails.description}\"")
            }
            SummaryRow(icon = Icons.Default.CameraAlt, label = "Photo fournie", value = if (requestDetails.isPhotoAdded) "Oui" else "Non")

            Spacer(modifier = Modifier.height(24.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))

            // Footer buttons
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { /*TODO*/ }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text("Appeler le dépanneur")
                }
                Button(onClick = { /*TODO*/ }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Message, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                    Text("Envoyer un message")
                }
                Button(onClick = onCancel, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                    Text("Annuler la demande")
                }
            }
        }
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
            Icon(icon, contentDescription = label, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.width(8.dp))
            Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Text(value, fontWeight = FontWeight.Bold, textAlign = TextAlign.End)
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
                    .background(MaterialTheme.colorScheme.primary, CircleShape)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.DirectionsCar,
                    contentDescription = "Car",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("RescueMap", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Row {
            Icon(Icons.Filled.BugReport, contentDescription = "Bug Report", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.width(16.dp))
            Icon(Icons.Filled.Settings, contentDescription = "Settings", tint = MaterialTheme.colorScheme.onSurfaceVariant)
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
        Icon(Icons.Filled.LocationOn, contentDescription = "Location", tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(4.dp))
        Text("123 Rue Fictive, Paris", color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.width(8.dp))
        Icon(Icons.Filled.Refresh, contentDescription = "Refresh", tint = MaterialTheme.colorScheme.onSurfaceVariant)
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
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        items.forEachIndexed { index, item ->
            val isSelected = selectedTabIndex == index
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(index) },
                label = {
                    Text(
                        text = item,
                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                icon = {
                    if (item == "Carte") {
                        Box(
                            modifier = Modifier
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(4.dp)
                        ) {
                            Icon(
                                imageVector = icons[index],
                                contentDescription = item,
                                tint = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Icon(
                            imageVector = icons[index],
                            contentDescription = item,
                            tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
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
