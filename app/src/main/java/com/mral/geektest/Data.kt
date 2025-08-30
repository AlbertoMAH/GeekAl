package com.mral.geektest

import androidx.compose.runtime.mutableStateListOf

data class Restaurant(
    val id: Long,
    val name: String,
    val cuisine: String,
    val rating: Double,
    val deliveryTime: String,
    val imageUrl: String,
    val latitude: Double,
    val longitude: Double
)

val sampleRestaurants = listOf(
    Restaurant(1L, "The Bistro", "Cuisine française", 4.5, "25 min", "https://loremflickr.com/800/600/restaurant", 48.8584, 2.2945),
    Restaurant(2L, "French Pâtisserie", "Boulangerie & Café", 4.8, "15 min", "https://loremflickr.com/800/600/patisserie", 48.8572, 2.3598),
    Restaurant(3L, "Le Jardin", "Cuisine française", 4.6, "20 min", "https://loremflickr.com/800/600/garden,restaurant", 48.8462, 2.3372),
    Restaurant(4L, "La Trattoria", "Cuisine italienne", 4.7, "30 min", "https://loremflickr.com/800/600/trattoria", 48.8501, 2.3446)
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
    Reservation(1L, "The Bistro", "Cuisine française", "2 personnes", "29 août 2025", "19:30", "confirmed"),
    Reservation(2L, "La Trattoria", "Cuisine italienne", "4 personnes", "28 août 2025", "20:00", "canceled")
)
