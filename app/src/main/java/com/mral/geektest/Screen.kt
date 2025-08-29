package com.mral.geektest

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.graphics.vector.ImageVector

enum class Screen(val label: String, val icon: ImageVector) {
    Home("Accueil", Icons.Default.Home),
    Reservations("Réservations", Icons.Default.Bookmark),
    Profile("Profil", Icons.Default.Person)
}
