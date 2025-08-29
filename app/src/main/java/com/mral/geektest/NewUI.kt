package com.mral.geektest

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter
import com.mral.geektest.ui.theme.CanceledRed
import com.mral.geektest.ui.theme.ConfirmedGreen
import com.mral.geektest.ui.theme.InputGray
import com.mral.geektest.ui.theme.PurpleLight
import com.mral.geektest.ui.theme.PurplePrimary
import com.mral.geektest.ui.theme.PurpleSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewMainScreen() {
    var currentScreen by remember { mutableStateOf(Screen.Home) }
    var selectedRestaurant by remember { mutableStateOf<Restaurant?>(null) }
    var bookingState by remember { mutableStateOf(BookingState.Idle) }
    val coroutineScope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        if (selectedRestaurant != null) {
            RestaurantDetailsScreen(
                restaurant = selectedRestaurant!!,
                onBack = { selectedRestaurant = null },
                onBook = { partySize, date, time ->
                    coroutineScope.launch {
                        bookingState = BookingState.CheckingAvailability
                        delay(2000)
                        bookingState = BookingState.AvailabilityConfirmed
                    }
                }
            )
        } else {
            Scaffold(
                bottomBar = {
                    AppBottomNavBar(
                        currentScreen = currentScreen,
                        onScreenSelected = { screen -> currentScreen = screen }
                    )
                }
            ) { innerPadding ->
                Box(modifier = Modifier.padding(innerPadding)) {
                    when (currentScreen) {
                        Screen.Home -> HomeScreen(onRestaurantClick = { restaurant -> selectedRestaurant = restaurant })
                        Screen.Reservations -> ReservationsScreen()
                        Screen.Profile -> ProfileScreen()
                    }
                }
            }
        }

        if (bookingState != BookingState.Idle) {
            BookingOverlay(
                bookingState = bookingState,
                onContinue = {
                    coroutineScope.launch {
                        bookingState = BookingState.Confirming
                        delay(2000)
                        bookingState = BookingState.BookingConfirmed
                    }
                },
                onCancel = { bookingState = BookingState.Idle },
                onDone = {
                    sampleReservations.add(
                        Reservation(
                            System.currentTimeMillis(),
                            selectedRestaurant?.name ?: "The Bistro",
                            selectedRestaurant?.cuisine ?: "Cuisine française",
                            "2 personnes",
                            "30 août 2025",
                            "20:00",
                            "confirmed"
                        )
                    )
                    bookingState = BookingState.Idle
                }
            )
        }
    }
}

@Composable
fun AppBottomNavBar(currentScreen: Screen, onScreenSelected: (Screen) -> Unit) {
    NavigationBar(
        modifier = Modifier.clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)),
        containerColor = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(PurpleSecondary, PurplePrimary)
                    )
                )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                Screen.values().forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.label) },
                        label = { Text(screen.label, fontSize = 12.sp) },
                        selected = currentScreen == screen,
                        onClick = { onScreenSelected(screen) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            unselectedIconColor = Color.White.copy(alpha = 0.7f),
                            selectedTextColor = Color.White,
                            unselectedTextColor = Color.White.copy(alpha = 0.7f),
                            indicatorColor = Color.White.copy(alpha = 0.2f)
                        )
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onRestaurantClick: (Restaurant) -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Découvrir", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color.Gray)
        }
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = "",
            onValueChange = {},
            placeholder = { Text("Rechercher des restaurants") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(50),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = PurplePrimary,
                unfocusedContainerColor = InputGray,
                focusedContainerColor = InputGray
            )
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text("Pour Vous", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items(sampleRestaurants.take(2)) { restaurant ->
                RestaurantCard(restaurant, onRestaurantClick = onRestaurantClick)
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text("Restaurants à proximité", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(sampleRestaurants.drop(2)) { restaurant ->
                NearbyRestaurantItem(restaurant, onRestaurantClick = onRestaurantClick)
            }
        }
    }
}

@Composable
fun ReservationsScreen() {
    var filter by remember { mutableStateOf("all") }
    var showDeleteDialog by remember { mutableStateOf<Reservation?>(null) }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterButton("Tout", filter == "all") { filter = "all" }
            Spacer(modifier = Modifier.width(8.dp))
            FilterButton("Confirmées", filter == "confirmed") { filter = "confirmed" }
            Spacer(modifier = Modifier.width(8.dp))
            FilterButton("Annulées", filter == "canceled") { filter = "canceled" }
        }
        LazyColumn(modifier = Modifier.padding(horizontal = 16.dp)) {
            val filteredReservations = when (filter) {
                "confirmed" -> sampleReservations.filter { it.status == "confirmed" }
                "canceled" -> sampleReservations.filter { it.status == "canceled" }
                else -> sampleReservations
            }
            if (filteredReservations.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillParentMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Aucune réservation")
                    }
                }
            } else {
                items(filteredReservations) { reservation ->
                    ReservationCard(
                        reservation = reservation,
                        onDelete = { showDeleteDialog = reservation }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }

    if (showDeleteDialog != null) {
        ConfirmationDialog(
            title = "Supprimer la réservation?",
            text = "Êtes-vous sûr de vouloir supprimer cette réservation de la liste ?",
            onConfirm = {
                sampleReservations.remove(showDeleteDialog)
                showDeleteDialog = null
            },
            onDismiss = { showDeleteDialog = null }
        )
    }
}

@Composable
fun FilterButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) PurplePrimary else Color.Transparent,
            contentColor = if (isSelected) Color.White else Color.Gray
        ),
        elevation = if (isSelected) ButtonDefaults.buttonElevation(defaultElevation = 4.dp) else null
    ) {
        Text(text)
    }
}

@Composable
fun ReservationCard(reservation: Reservation, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = com.mral.geektest.ui.theme.LightGray)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = rememberAsyncImagePainter("https://placehold.co/80x80/d1a3e6/ffffff?text=${reservation.restaurantName.first()}"),
                contentDescription = reservation.restaurantName,
                modifier = Modifier.size(60.dp).clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(reservation.restaurantName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(reservation.cuisine, fontSize = 14.sp, color = Color.Gray)
                Text(
                    text = reservation.status.replaceFirstChar { it.uppercase() },
                    color = if (reservation.status == "confirmed") ConfirmedGreen else CanceledRed,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .background(
                            color = if (reservation.status == "confirmed") com.mral.geektest.ui.theme.ConfirmedGreenBg else com.mral.geektest.ui.theme.CanceledRedBg,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray)
            }
        }
    }
}

@Composable
fun ProfileScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Profile Screen")
    }
}

@Composable
fun RestaurantCard(restaurant: Restaurant, onRestaurantClick: (Restaurant) -> Unit) {
    Card(
        modifier = Modifier.width(300.dp).clickable { onRestaurantClick(restaurant) },
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = if (restaurant.name == "The Bistro") PurpleLight else Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = rememberAsyncImagePainter(restaurant.imageUrl),
                    contentDescription = restaurant.name,
                    modifier = Modifier.size(80.dp).clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(restaurant.name, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(restaurant.cuisine, fontSize = 14.sp, color = Color.Gray)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Star, contentDescription = "Rating", tint = Color(0xFFFFC107))
                    Text(" ${restaurant.rating} • ${restaurant.deliveryTime}", fontSize = 14.sp)
                }
                Button(
                    onClick = { onRestaurantClick(restaurant) },
                    shape = RoundedCornerShape(24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
                ) {
                    Text("Réserver", fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun NearbyRestaurantItem(restaurant: Restaurant, onRestaurantClick: (Restaurant) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onRestaurantClick(restaurant) },
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = com.mral.geektest.ui.theme.LightGray)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = rememberAsyncImagePainter(restaurant.imageUrl),
                contentDescription = restaurant.name,
                modifier = Modifier.size(60.dp).clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(restaurant.name, fontWeight = FontWeight.Bold)
                Text(restaurant.cuisine, fontSize = 12.sp, color = Color.Gray)
            }
            Button(
                onClick = { onRestaurantClick(restaurant) },
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
            ) {
                Text("Réserver", fontSize = 12.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantDetailsScreen(restaurant: Restaurant, onBack: () -> Unit, onBook: (String, String, String) -> Unit) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("Réserver", "Menu", "Description")

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.height(200.dp)) {
            Image(
                painter = rememberAsyncImagePainter("https://placehold.co/450x200/d1a3e6/ffffff?text=${restaurant.name.replace(" ", "+")}"),
                contentDescription = restaurant.name,
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.Crop
            )
            IconButton(onClick = onBack, modifier = Modifier.padding(16.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
        }
        Column(modifier = Modifier.padding(16.dp)) {
            Text(restaurant.name, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Text(restaurant.cuisine, style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
        }
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color.Transparent,
            contentColor = PurplePrimary,
            indicator = {}
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title) },
                    selectedContentColor = PurplePrimary,
                    unselectedContentColor = Color.Gray,
                    modifier = if (selectedTabIndex == index) Modifier.background(PurpleLight, RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)) else Modifier
                )
            }
        }
        Box(modifier = Modifier.background(PurpleLight)) {
            when (selectedTabIndex) {
                0 -> BookingForm(onBook = onBook)
                1 -> MenuTab()
                2 -> DescriptionTab(restaurant)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingForm(onBook: (String, String, String) -> Unit) {
    var partySize by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    val partySizeOptions = (1..10).map { "$it personne${if (it > 1) "s" else ""}" }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Détails de la réservation", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = partySize,
                onValueChange = {},
                readOnly = true,
                label = { Text("Nombre de personnes") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor(),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = PurplePrimary,
                    unfocusedContainerColor = InputGray,
                    focusedContainerColor = InputGray
                )
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                partySizeOptions.forEach { selectionOption ->
                    DropdownMenuItem(
                        text = { Text(selectionOption) },
                        onClick = {
                            partySize = selectionOption
                            expanded = false
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedTextField(
                value = date,
                onValueChange = { date = it },
                label = { Text("Date") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = PurplePrimary,
                    unfocusedContainerColor = InputGray,
                    focusedContainerColor = InputGray
                )
            )
            OutlinedTextField(
                value = time,
                onValueChange = { time = it },
                label = { Text("Heure") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = PurplePrimary,
                    unfocusedContainerColor = InputGray,
                    focusedContainerColor = InputGray
                )
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { onBook(partySize, date, time) },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)
        ) {
            Text("Réserver", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun MenuTab() {
    LazyColumn(modifier = Modifier.padding(16.dp)) {
        item {
            Text("Menu du Chef", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
        }
        items(sampleMenu) { menuItem ->
            Row(modifier = Modifier.padding(vertical = 8.dp)) {
                Image(
                    painter = rememberAsyncImagePainter(menuItem.imageUrl),
                    contentDescription = menuItem.name,
                    modifier = Modifier.size(80.dp).clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(menuItem.name, fontWeight = FontWeight.Bold)
                    Text(menuItem.description, fontSize = 14.sp, color = Color.Gray)
                    Text(menuItem.price, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 8.dp))
                }
            }
        }
    }
}

@Composable
fun DescriptionTab(restaurant: Restaurant) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Cuisine ${restaurant.cuisine}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Bienvenue à ${restaurant.name}, un charmant restaurant offrant le meilleur de la cuisine française. Notre ambiance chaleureuse et notre décor élégant en font l'endroit idéal pour une soirée mémorable.", fontSize = 14.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Horaires d'ouverture", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Lundi - Vendredi: 12:00 - 22:00", fontSize = 14.sp, color = Color.Gray)
        Text("Samedi: 12:00 - 23:00", fontSize = 14.sp, color = Color.Gray)
        Text("Dimanche: Fermé", fontSize = 14.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(16.dp))
        Text("Contact & Localisation", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Text("123 Example Street, City, State 12345", fontSize = 14.sp, color = Color.Gray)
    }
}

@Composable
fun BookingOverlay(
    bookingState: BookingState,
    onContinue: () -> Unit,
    onCancel: () -> Unit,
    onDone: () -> Unit
) {
    Dialog(onDismissRequest = onCancel) {
        Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (bookingState) {
                    BookingState.CheckingAvailability -> {
                        CircularProgressIndicator(color = PurplePrimary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Vérification de la disponibilité...", fontWeight = FontWeight.Bold)
                    }
                    BookingState.AvailabilityConfirmed -> {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Confirmed", tint = ConfirmedGreen, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Disponibilité confirmée!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Nous avons trouvé 2 tables pour vous.", color = Color.Gray)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            OutlinedButton(onClick = onCancel, shape = RoundedCornerShape(24.dp), modifier = Modifier.weight(1f)) { Text("Annuler") }
                            Button(onClick = onContinue, shape = RoundedCornerShape(24.dp), modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)) { Text("Continuer") }
                        }
                    }
                    BookingState.Confirming -> {
                        CircularProgressIndicator(color = PurplePrimary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Confirmation en cours...", fontWeight = FontWeight.Bold)
                    }
                    BookingState.BookingConfirmed -> {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Confirmed", tint = ConfirmedGreen, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Votre table est réservée !", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onDone, shape = RoundedCornerShape(24.dp), colors = ButtonDefaults.buttonColors(containerColor = PurplePrimary)) { Text("Fait") }
                    }
                    BookingState.Idle -> {}
                }
            }
        }
    }
}

@Composable
fun ConfirmationDialog(
    title: String,
    text: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text, color = Color.Gray)
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(24.dp)) {
                        Text("Annuler")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = onConfirm, shape = RoundedCornerShape(24.dp), colors = ButtonDefaults.buttonColors(containerColor = CanceledRed)) {
                        Text("Confirmer")
                    }
                }
            }
        }
    }
}
