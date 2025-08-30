package com.example.restaurantefinal

import android.os.Parcelable
import androidx.compose.runtime.mutableStateListOf
import kotlinx.parcelize.Parcelize

@Parcelize
data class Restaurant(
    val id: Long,
    val name: String,
    val description: String,
    val cuisine: String,
    val rating: Double,
    val deliveryTime: String,
    val imageUrl: String
) : Parcelable

val sampleRestaurants = listOf(
    Restaurant(
        id = 1L,
        name = "Le Maquis",
        description = "L'endroit idéal pour savourer un bon poulet braisé.",
        cuisine = "Africain - Ivoirien",
        rating = 4.5,
        deliveryTime = "25-35 min",
        imageUrl = "https://images.unsplash.com/photo-1555939594-58d7cb561ad1?q=80&w=1887&auto=format&fit=crop"
    ),
    Restaurant(
        id = 2L,
        name = "Chez Tantie",
        description = "La spécialiste du Garba, un plat incontournable.",
        cuisine = "Ivoirien - Street Food",
        rating = 4.8,
        deliveryTime = "15-20 min",
        imageUrl = "https://images.unsplash.com/photo-1541592106381-b31e9677c0e5?q=80&w=1887&auto=format&fit=crop"
    ),
    Restaurant(
        id = 3L,
        name = "Allocodrome",
        description = "Alloco, brochettes, tout y est pour se régaler.",
        cuisine = "Ivoirien - Grill",
        rating = 4.3,
        deliveryTime = "20-30 min",
        imageUrl = "https://images.unsplash.com/photo-1540189549336-e6e99c3679fe?q=80&w=1887&auto=format&fit=crop"
    ),
    Restaurant(
        id = 4L,
        name = "La Villa",
        description = "Un cadre chic pour une cuisine raffinée.",
        cuisine = "Européen - Gastronomique",
        rating = 4.9,
        deliveryTime = "40-50 min",
        imageUrl = "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?q=80&w=2070&auto=format&fit=crop"
    )
)

@Parcelize
data class Dish(
    val id: Long,
    val name: String,
    val imageUrl: String
) : Parcelable

val sampleDishes = listOf(
    Dish(1L, "Attiéké Poisson", "https://plus.unsplash.com/premium_photo-1673809794993-524128c03554?q=80&w=1887&auto=format&fit=crop"),
    Dish(2L, "Foutou Banane", "https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?q=80&w=1981&auto=format&fit=crop"),
    Dish(3L, "Placali", "https://images.unsplash.com/photo-1567620905732-2d1ec7ab7445?q=80&w=1980&auto=format&fit=crop"),
    Dish(4L, "Kédjénou", "https://images.unsplash.com/photo-1484723051597-63b3b1c86e89?q=80&w=1780&auto=format&fit=crop"),
    Dish(5L, "N'Gouan", "https://images.unsplash.com/photo-1482049016688-2d3e1b311543?q=80&w=1910&auto=format&fit=crop")
)
