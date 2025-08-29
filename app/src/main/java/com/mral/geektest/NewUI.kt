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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class Restaurant(
    val id: Int,
    val name: String,
    val cuisine: String,
    val rating: Double,
    val deliveryTime: String,
    val imageUrl: String
)

val sampleRestaurants = listOf(
    Restaurant(1, "The Bistro", "Cuisine française", 4.5, "25 min", "https://placehold.co/80x80/d1a3e6/ffffff?text=B"),
    Restaurant(2, "French Pâtisserie", "Boulangerie & Café", 4.8, "15 min", "https://placehold.co/80x80/e8b9f1/ffffff?text=P"),
    Restaurant(3, "Le Jardin", "Cuisine française", 4.6, "20 min", "https://placehold.co/60x60/d1a3e6/ffffff?text=L"),
    Restaurant(4, "La Trattoria", "Cuisine italienne", 4.7, "30 min", "https://placehold.co/60x60/d1a3e6/ffffff?text=L")
)

data class MenuItem(
    val name: String,
    val description: String,
    val price: String,
    val imageUrl: String
)

val sampleMenu = listOf(
    MenuItem("Soupe à l'oignon", "Soupe à l'oignon gratinée traditionnelle.", "12€", "https://placehold.co/80x80/d1a3e6/ffffff?text=S"),
    MenuItem("Salade Niçoise", "Thon, pommes de terre, œufs et haricots verts.", "15€", "https://placehold.co/80x80/d1a3e6/ffffff?text=S"),
    MenuItem("Coq au vin", "Poulet braisé au vin, champignons et ail.", "24€", "https://placehold.co/80x80/d1a3e6/ffffff?text=C")
)

data class Reservation(
    val id: Long,
    val restaurantName: String,
    val cuisine: String,
    val partySize: String,
    val date: String,
    val time: String,
    var status: String // "confirmed" or "canceled"
)

val sampleReservations = mutableStateListOf(
    Reservation(1, "The Bistro", "Cuisine française", "2 personnes", "29 août 2025", "19:30", "confirmed"),
    Reservation(2, "La Trattoria", "Cuisine italienne", "4 personnes", "28 août 2025", "20:00", "canceled")
)

enum class Screen(val label: String, val icon: ImageVector) {
    Home("Accueil", Icons.Default.Home),
    Reservations("Réservations", Icons.Default.Bookmark),
    Profile("Profil", Icons.Default.Person)
}

enum class BookingState {
    Idle,
    CheckingAvailability,
    AvailabilityConfirmed,
    Confirming,
    BookingConfirmed
}

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
                    NavigationBar {
                        Screen.values().forEach { screen ->
                            NavigationBarItem(
                                icon = { Icon(screen.icon, contentDescription = screen.label) },
                                label = { Text(screen.label) },
                                selected = currentScreen == screen,
                                onClick = { currentScreen = screen }
                            )
                        }
                    }
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
                    // Add the new reservation
                    // For now, we'll just add a sample reservation
                    sampleReservations.add(
                        Reservation(
                            System.currentTimeMillis(),
                            "The Bistro",
                            "Cuisine française",
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
fun HomeScreen(onRestaurantClick: (Restaurant) -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Découvrir", style = MaterialTheme.typography.headlineMedium)
            Icon(Icons.Default.Notifications, contentDescription = "Notifications")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search Bar
        OutlinedTextField(
            value = "",
            onValueChange = { },
            placeholder = { Text("Rechercher des restaurants") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(50)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Pour Vous section
        Text("Pour Vous", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items(sampleRestaurants.take(2)) { restaurant ->
                RestaurantCard(restaurant, onRestaurantClick = onRestaurantClick)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Restaurants à proximité section
        Text("Restaurants à proximité", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
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
            horizontalArrangement = Arrangement.Center
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

            items(filteredReservations) { reservation ->
                ReservationCard(
                    reservation = reservation,
                    onDelete = { showDeleteDialog = reservation }
                )
                Spacer(modifier = Modifier.height(16.dp))
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
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        modifier = Modifier.height(40.dp)
    ) {
        Text(text, color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray)
    }
}

@Composable
fun ReservationCard(reservation: Reservation, onDelete: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = rememberAsyncImagePainter("https://placehold.co/80x80/d1a3e6/ffffff?text=${reservation.restaurantName.first()}"),
                contentDescription = reservation.restaurantName,
                modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(reservation.restaurantName, style = MaterialTheme.typography.titleMedium)
                Text(reservation.cuisine, style = MaterialTheme.typography.bodySmall)
                Text(
                    text = reservation.status.replaceFirstChar { it.uppercase() },
                    color = if (reservation.status == "confirmed") Color.Green else Color.Red,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .background(
                            color = if (reservation.status == "confirmed") Color.Green.copy(alpha = 0.1f) else Color.Red.copy(
                                alpha = 0.1f
                            ),
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 6.dp, vertical = 2.dp)
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
    Text("Profile Screen")
}

@Composable
fun RestaurantCard(restaurant: Restaurant, onRestaurantClick: (Restaurant) -> Unit) {
    Card(
        modifier = Modifier.width(300.dp).clickable { onRestaurantClick(restaurant) },
        shape = RoundedCornerShape(16.dp)
    ) {
        Column {
            Image(
                painter = rememberAsyncImagePainter(restaurant.imageUrl),
                contentDescription = restaurant.name,
                modifier = Modifier
                    .height(150.dp)
                    .fillMaxWidth(),
                contentScale = ContentScale.Crop
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Text(restaurant.name, style = MaterialTheme.typography.titleLarge)
                Text(restaurant.cuisine, style = MaterialTheme.typography.bodyMedium)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Star, contentDescription = "Rating", tint = Color.Yellow)
                        Text("${restaurant.rating} • ${restaurant.deliveryTime}")
                    }
                    Button(onClick = { onRestaurantClick(restaurant) }) {
                        Text("Réserver")
                    }
                }
            }
        }
    }
}

@Composable
fun NearbyRestaurantItem(restaurant: Restaurant, onRestaurantClick: (Restaurant) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onRestaurantClick(restaurant) },
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = rememberAsyncImagePainter(restaurant.imageUrl),
                contentDescription = restaurant.name,
                modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(restaurant.name, style = MaterialTheme.typography.titleMedium)
                Text(restaurant.cuisine, style = MaterialTheme.typography.bodySmall)
            }
            Button(onClick = { onRestaurantClick(restaurant) }) {
                Text("Réserver")
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
            Text(restaurant.name, style = MaterialTheme.typography.headlineLarge)
            Text(restaurant.cuisine, style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
        }

        TabRow(selectedTabIndex = selectedTabIndex) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = { Text(title) }
                )
            }
        }

        when (selectedTabIndex) {
            0 -> BookingForm(onBook = onBook)
            1 -> MenuTab()
            2 -> DescriptionTab(restaurant)
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
        Text("Détails de la réservation", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = partySize,
                onValueChange = { },
                readOnly = true,
                label = { Text("Nombre de personnes") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.fillMaxWidth().menuAnchor()
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
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = time,
                onValueChange = { time = it },
                label = { Text("Heure") },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onBook(partySize, date, time) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Réserver")
        }
    }
}

@Composable
fun MenuTab() {
    LazyColumn(modifier = Modifier.padding(16.dp)) {
        item {
            Text("Menu du Chef", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(16.dp))
        }
        items(sampleMenu) { menuItem ->
            Row(modifier = Modifier.padding(vertical = 8.dp)) {
                Image(
                    painter = rememberAsyncImagePainter(menuItem.imageUrl),
                    contentDescription = menuItem.name,
                    modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(menuItem.name, style = MaterialTheme.typography.titleMedium)
                    Text(menuItem.description, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                    Text(menuItem.price, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
                }
            }
        }
    }
}

@Composable
fun DescriptionTab(restaurant: Restaurant) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Cuisine ${restaurant.cuisine}", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Bienvenue à ${restaurant.name}, un charmant restaurant offrant le meilleur de la cuisine française. Notre ambiance chaleureuse et notre décor élégant en font l'endroit idéal pour une soirée mémorable.")
        Spacer(modifier = Modifier.height(16.dp))
        Text("Horaires d'ouverture", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        Text("Lundi - Vendredi: 12:00 - 22:00")
        Text("Samedi: 12:00 - 23:00")
        Text("Dimanche: Fermé")
        Spacer(modifier = Modifier.height(16.dp))
        Text("Contact & Localisation", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(8.dp))
        Text("123 Example Street, City, State 12345")
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
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (bookingState) {
                    BookingState.CheckingAvailability -> {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Vérification de la disponibilité...")
                    }
                    BookingState.AvailabilityConfirmed -> {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Confirmed", tint = Color.Green, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Disponibilité confirmée!")
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Nous avons trouvé 2 tables pour vous.")
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Button(onClick = onCancel) { Text("Annuler") }
                            Button(onClick = onContinue) { Text("Continuer") }
                        }
                    }
                    BookingState.Confirming -> {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Confirmation en cours...")
                    }
                    BookingState.BookingConfirmed -> {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Confirmed", tint = Color.Green, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Votre table est réservée !")
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onDone) { Text("Fait") }
                    }
                    BookingState.Idle -> { }
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
        Card(shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text)
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Annuler")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = onConfirm) {
                        Text("Confirmer")
                    }
                }
            }
        }
    }
}
