package com.example.restaurantefinal

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Restaurant(
    val id: Long,
    val name: String,
    val cuisine: String,
    val description: String,
    val imageUrl: String
) : Parcelable

val sampleRestaurants = listOf(
    Restaurant(
        id = 1L,
        name = "Le Manguier",
        cuisine = "Cuisine traditionnelle",
        description = "Un havre de paix culinaire au cœur d'Abidjan.",
        imageUrl = "https://placehold.co/600x400/5C6BC0/FFFFFF?text=Le+Manguier"
    ),
    Restaurant(
        id = 2L,
        name = "Chez Fatou",
        cuisine = "Spécialités de maquis",
        description = "Découvrez les saveurs de la rue ivoirienne.",
        imageUrl = "https://placehold.co/600x400/29B6F6/FFFFFF?text=Chez+Fatou"
    ),
    Restaurant(
        id = 3L,
        name = "Le Cordon Bleu",
        cuisine = "Fusion cuisine",
        description = "Une touche moderne sur des classiques ivoiriens.",
        imageUrl = "https://placehold.co/600x400/7CB342/FFFFFF?text=Le+Cordon+Bleu"
    )
)

@Parcelize
data class Dish(
    val id: Long,
    val name: String,
    val description: String,
    val imageUrl: String
) : Parcelable

val sampleDishes = listOf(
    Dish(1L, "Attiéké", "Couscous de manioc", "https://placehold.co/400x400/EF5350/FFFFFF?text=Atti%C3%A9k%C3%A9"),
    Dish(2L, "Alloco", "Banane plantain frite", "https://placehold.co/400x400/AB47BC/FFFFFF?text=Alloco"),
    Dish(3L, "Poisson Braisé", "Poisson grillé et épicé", "https://placehold.co/400x400/FFB74D/FFFFFF?text=Poisson+Braise"),
    Dish(4L, "Foutou Sauce", "Pâte de manioc avec sauce", "https://placehold.co/400x400/81C784/FFFFFF?text=Foutou+Sauce"),
    Dish(5L, "Kédjenou", "Ragoût de poulet en poterie", "https://placehold.co/400x400/D4E157/FFFFFF?text=Kedjenou")
)
