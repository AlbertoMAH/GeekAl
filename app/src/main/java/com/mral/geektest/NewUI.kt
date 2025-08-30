package com.mral.geektest

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.*
import coil.compose.rememberAsyncImagePainter
import com.mral.geektest.ui.theme.GreenConfirmedText
import com.mral.geektest.ui.theme.StarYellow
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

sealed class NavRoutes(val route: String) {
    object Home : NavRoutes("home")
    object RestaurantDetails : NavRoutes("restaurant/{restaurantId}") {
        fun createRoute(restaurantId: Long) = "restaurant/$restaurantId"
    }
    object Reservations : NavRoutes("reservations")
    object ReservationDetails : NavRoutes("reservation/{reservationId}") {
        fun createRoute(reservationId: Long) = "reservation/$reservationId"
    }
    object Profile : NavRoutes("profile")
}

@Composable
fun NewMainScreen() {
    val navController = rememberNavController()
    var bookingState by remember { mutableStateOf(BookingState.Idle) }
    var reservationToCancel by remember { mutableStateOf<Reservation?>(null) }
    val coroutineScope = rememberCoroutineScope()
    var lastBookedRestaurant by remember { mutableStateOf<Restaurant?>(null) }
    var lastBookedReservationId by remember { mutableStateOf<Long?>(null) }

    if (reservationToCancel != null) {
        CancelConfirmationDialog(
            onConfirm = {
                sampleReservations.find { it.id == reservationToCancel?.id }?.status = "canceled"
                reservationToCancel = null
                navController.navigate(NavRoutes.Reservations.route) {
                    popUpTo(navController.graph.startDestinationId)
                }
            },
            onDismiss = { reservationToCancel = null }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            bottomBar = { AppBottomNavBar(navController = navController) }
        ) { innerPadding ->
            AppNavigation(
                navController = navController,
                modifier = Modifier.padding(innerPadding),
                onBook = { restaurant ->
                    lastBookedRestaurant = restaurant
                    coroutineScope.launch {
                        bookingState = BookingState.CheckingAvailability
                        delay(2000)
                        bookingState = BookingState.AvailabilityConfirmed
                    }
                },
                onCancelBooking = { reservation ->
                    reservationToCancel = reservation
                }
            )
        }

        if (bookingState != BookingState.Idle) {
            BookingOverlay(
                bookingState = bookingState,
                onContinue = {
                    coroutineScope.launch {
                        bookingState = BookingState.Confirming
                        delay(2000)
                        val newReservation = Reservation(
                            System.currentTimeMillis(),
                            lastBookedRestaurant?.name ?: "The Bistro",
                            lastBookedRestaurant?.cuisine ?: "Cuisine française",
                            "2 personnes",
                            "30 août 2025",
                            "20:00",
                            "confirmed"
                        )
                        sampleReservations.add(newReservation)
                        lastBookedReservationId = newReservation.id
                        bookingState = BookingState.BookingConfirmed
                    }
                },
                onCancel = { bookingState = BookingState.Idle },
                onViewReservation = {
                    bookingState = BookingState.Idle
                    lastBookedReservationId?.let {
                        navController.navigate(NavRoutes.ReservationDetails.createRoute(it))
                    }
                },
                onDone = {
                    bookingState = BookingState.Idle
                    navController.navigate(NavRoutes.Home.route) {
                        popUpTo(navController.graph.startDestinationId)
                    }
                }
            )
        }
    }
}

@Composable
fun CancelConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.Cancel, contentDescription = "Cancel", tint = Color.Red, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("Annuler la réservation?", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Êtes-vous sûr de vouloir annuler cette réservation?", color = Color.Gray, modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(8.dp), modifier = Modifier.weight(1f)) { Text("Non") }
                    Button(
                        onClick = onConfirm,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) { Text("Oui") }
                }
            }
        }
    }
}

@Composable
fun AppNavigation(
    navController: NavHostController,
    modifier: Modifier,
    onBook: (Restaurant) -> Unit,
    onCancelBooking: (Reservation) -> Unit
) {
    NavHost(navController, startDestination = NavRoutes.Home.route, modifier = modifier) {
        composable(NavRoutes.Home.route) {
            HomeScreen(onRestaurantClick = { restaurant ->
                navController.navigate(NavRoutes.RestaurantDetails.createRoute(restaurant.id))
            })
        }
        composable(NavRoutes.RestaurantDetails.route) { backStackEntry ->
            val restaurantId = backStackEntry.arguments?.getString("restaurantId")?.toLongOrNull()
            val restaurant = sampleRestaurants.find { it.id == restaurantId }
            if (restaurant != null) {
                RestaurantDetailsScreen(
                    restaurant = restaurant,
                    onBack = { navController.popBackStack() },
                    onBook = { onBook(restaurant) }
                )
            }
        }
        composable(NavRoutes.Reservations.route) {
            ReservationsScreen(onReservationClick = { reservationId ->
                navController.navigate(NavRoutes.ReservationDetails.createRoute(reservationId))
            })
        }
        composable(NavRoutes.ReservationDetails.route) { backStackEntry ->
            val reservationId = backStackEntry.arguments?.getString("reservationId")?.toLongOrNull()
            val reservation = sampleReservations.find { it.id == reservationId }
            if (reservation != null) {
                ReservationDetailsScreen(
                    reservation = reservation,
                    onBack = { navController.popBackStack() },
                    onCancelBooking = { onCancelBooking(reservation) }
                )
            }
        }
        composable(NavRoutes.Profile.route) {
            ProfileScreen()
        }
    }
}

@Composable
fun AppBottomNavBar(navController: NavController) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        listOf(
            Screen.Home to NavRoutes.Home.route,
            Screen.Reservations to NavRoutes.Reservations.route,
            Screen.Profile to NavRoutes.Profile.route
        ).forEach { (screen, route) ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.label) },
                label = { Text(screen.label, fontSize = 12.sp) },
                selected = currentRoute == route,
                onClick = {
                    navController.navigate(route) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = Color.Gray,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedTextColor = Color.Gray,
                    indicatorColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HomeScreen(onRestaurantClick: (Restaurant) -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Découvrir", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color.Gray)
        }
        Spacer(modifier = Modifier.height(16.dp))
        var searchText by remember { mutableStateOf("") }
        var searchActive by remember { mutableStateOf(false) }

        DockedSearchBar(
            query = searchText,
            onQueryChange = { searchText = it },
            onSearch = { searchActive = false },
            active = searchActive,
            onActiveChange = { searchActive = it },
            placeholder = { Text("Rechercher des restaurants") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            modifier = Modifier.fillMaxWidth()
        ) {
            val filteredRestaurants = sampleRestaurants.filter {
                it.name.contains(searchText, ignoreCase = true) ||
                it.cuisine.contains(searchText, ignoreCase = true)
            }
            LazyColumn(modifier = Modifier.fillMaxWidth()) {
                items(filteredRestaurants) { restaurant ->
                    NearbyRestaurantItem(
                        restaurant = restaurant,
                        onRestaurantClick = {
                            searchActive = false
                            onRestaurantClick(restaurant)
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text("Pour Vous", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        val pagerState = rememberPagerState(pageCount = { sampleRestaurants.take(2).size })
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.height(240.dp),
            contentPadding = PaddingValues(horizontal = 32.dp),
            pageSpacing = 16.dp
        ) { page ->
            RestaurantCard(
                restaurant = sampleRestaurants.take(2)[page],
                onRestaurantClick = onRestaurantClick
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text("Restaurants à proximité", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(sampleRestaurants.drop(2)) { restaurant ->
                NearbyRestaurantItem(restaurant, onRestaurantClick = onRestaurantClick)
            }
        }
    }
}

@Composable
fun ReservationsScreen(onReservationClick: (Long) -> Unit) {
    var filter by remember { mutableStateOf("all") }
    var reservationToDelete by remember { mutableStateOf<Reservation?>(null) }

    val filteredReservations = when (filter) {
        "confirmed" -> sampleReservations.filter { it.status == "confirmed" }
        "canceled" -> sampleReservations.filter { it.status == "canceled" }
        else -> sampleReservations
    }

    if (reservationToDelete != null) {
        DeleteConfirmationDialog(
            onConfirm = {
                sampleReservations.remove(reservationToDelete)
                reservationToDelete = null
            },
            onDismiss = { reservationToDelete = null }
        )
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Mes Réservations", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterButton(text = "Tout", isSelected = filter == "all") { filter = "all" }
            Spacer(modifier = Modifier.width(8.dp))
            FilterButton(text = "Confirmées", isSelected = filter == "confirmed") { filter = "confirmed" }
            Spacer(modifier = Modifier.width(8.dp))
            FilterButton(text = "Annulées", isSelected = filter == "canceled") { filter = "canceled" }
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (filteredReservations.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.EventBusy,
                        contentDescription = "No Reservations",
                        modifier = Modifier.size(64.dp),
                        tint = Color.Gray.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Aucune réservation.",
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.Gray.copy(alpha = 0.7f)
                    )
                    Text(
                        text = when (filter) {
                            "confirmed" -> "Vous n'avez aucune réservation confirmée."
                            "canceled" -> "Vous n'avez aucune réservation annulée."
                            else -> "Vos réservations apparaîtront ici."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                items(filteredReservations) { reservation ->
                    ReservationCard(
                        reservation = reservation,
                        onClick = { onReservationClick(reservation.id) },
                        onDelete = { reservationToDelete = reservation }
                    )
                }
            }
        }
    }
}

@Composable
fun DeleteConfirmationDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Default.DeleteForever, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("Supprimer la réservation?", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Êtes-vous sûr de vouloir supprimer cette réservation de la liste ?", color = Color.Gray, modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(8.dp), modifier = Modifier.weight(1f)) { Text("Annuler") }
                    Button(
                        onClick = onConfirm,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) { Text("Supprimer") }
                }
            }
        }
    }
}

@Composable
fun FilterButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary
        ),
        elevation = if (isSelected) ButtonDefaults.buttonElevation(defaultElevation = 2.dp) else null,
        border = if (!isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Text(text)
    }
}

@Composable
fun ReservationCard(reservation: Reservation, onClick: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(reservation.restaurantName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(reservation.cuisine, fontSize = 14.sp, color = Color.Gray)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("${reservation.partySize} • ${reservation.date} à ${reservation.time}", fontSize = 14.sp)
                    Text(
                        text = reservation.status.replaceFirstChar { it.uppercase() },
                        color = if (reservation.status == "confirmed") GreenConfirmedText else Color.Gray,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Reservation", tint = Color.Red.copy(alpha = 0.7f))
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
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = rememberAsyncImagePainter(restaurant.imageUrl),
                    contentDescription = restaurant.name,
                    modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp)),
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
                    Icon(Icons.Default.Star, contentDescription = "Rating", tint = StarYellow)
                    Text(" ${restaurant.rating} • ${restaurant.deliveryTime}", fontSize = 14.sp)
                }
                Button(
                    onClick = { onRestaurantClick(restaurant) },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
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
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = rememberAsyncImagePainter(restaurant.imageUrl),
                contentDescription = restaurant.name,
                modifier = Modifier.size(60.dp).clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(restaurant.name, fontWeight = FontWeight.Bold)
                Text(restaurant.cuisine, fontSize = 12.sp, color = Color.Gray)
            }
            Button(
                onClick = { onRestaurantClick(restaurant) },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Réserver", fontSize = 12.sp)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantDetailsScreen(restaurant: Restaurant, onBack: () -> Unit, onBook: () -> Unit) {
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
            contentColor = MaterialTheme.colorScheme.primary
        ) {
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
fun BookingForm(onBook: () -> Unit) {
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
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface
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
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
            OutlinedTextField(
                value = time,
                onValueChange = { time = it },
                label = { Text("Heure") },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onBook,
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
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
                    modifier = Modifier.size(80.dp).clip(RoundedCornerShape(12.dp)),
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
fun ReservationDetailsScreen(reservation: Reservation, onBack: () -> Unit, onCancelBooking: () -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.height(200.dp)) {
            Image(
                painter = rememberAsyncImagePainter("https://placehold.co/450x200/d1a3e6/ffffff?text=${reservation.restaurantName.replace(" ", "+")}"),
                contentDescription = reservation.restaurantName,
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.Crop
            )
            IconButton(onClick = onBack, modifier = Modifier.padding(16.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(reservation.restaurantName, style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold)
            Text(reservation.cuisine, style = MaterialTheme.typography.bodyLarge, color = Color.Gray)
            Spacer(modifier = Modifier.height(16.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarToday, contentDescription = "Date", tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("${reservation.date} à ${reservation.time}", style = MaterialTheme.typography.bodyLarge)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Group, contentDescription = "Party Size", tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(reservation.partySize, style = MaterialTheme.typography.bodyLarge)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = "Address", tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("123 Example Street, City, State 12345", style = MaterialTheme.typography.bodyLarge)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.QrCode2, contentDescription = "QR Code", modifier = Modifier.size(150.dp), tint = MaterialTheme.colorScheme.onSurface)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Montrez ce QR code au restaurant")
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            OutlinedButton(
                onClick = onCancelBooking,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red)
            ) {
                Text("Annuler la réservation")
            }
        }
    }
}

@Composable
fun BookingOverlay(
    bookingState: BookingState,
    onContinue: () -> Unit,
    onCancel: () -> Unit,
    onViewReservation: () -> Unit,
    onDone: () -> Unit
) {
    Dialog(onDismissRequest = onCancel) {
        Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (bookingState) {
                    BookingState.CheckingAvailability -> {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Vérification de la disponibilité...", fontWeight = FontWeight.Bold)
                    }
                    BookingState.AvailabilityConfirmed -> {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Confirmed", tint = GreenConfirmedText, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Disponibilité confirmée!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Nous avons trouvé 2 tables pour vous.", color = Color.Gray)
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            OutlinedButton(onClick = onCancel, shape = RoundedCornerShape(8.dp), modifier = Modifier.weight(1f)) { Text("Annuler") }
                            Button(onClick = onContinue, shape = RoundedCornerShape(8.dp), modifier = Modifier.weight(1f), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)) { Text("Continuer") }
                        }
                    }
                    BookingState.Confirming -> {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Confirmation en cours...", fontWeight = FontWeight.Bold)
                    }
                    BookingState.BookingConfirmed -> {
                        Icon(Icons.Default.CheckCircle, contentDescription = "Confirmed", tint = GreenConfirmedText, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Votre table est réservée !", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = onViewReservation,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                            ) { Text("Voir la réservation") }
                            OutlinedButton(
                                onClick = onDone,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) { Text("Fait") }
                        }
                    }
                    BookingState.Idle -> {}
                }
            }
        }
    }
}
