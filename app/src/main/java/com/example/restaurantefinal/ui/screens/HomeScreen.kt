package com.example.restaurantefinal.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.restaurantefinal.Restaurant
import com.example.restaurantefinal.sampleDishes
import com.example.restaurantefinal.sampleRestaurants
import com.example.restaurantefinal.ui.composables.DishCard
import com.example.restaurantefinal.ui.composables.RestaurantCard
import com.example.restaurantefinal.ui.theme.Gray500
import com.example.restaurantefinal.ui.theme.Gray800
import com.example.restaurantefinal.ui.theme.Indigo600

@Composable
fun HomeScreen(onShowMessage: (String) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        // Header
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "Taste of Africa",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Indigo600,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = "Réservez une table et découvrez les saveurs d'Abidjan.",
                    fontSize = 16.sp,
                    color = Gray500
                )
            }
        }

        // Spacer
        item {
            Spacer(modifier = Modifier.height(32.dp))
        }

        // Restaurant Section
        item {
            Column {
                Text(
                    text = "Réserver une table",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Gray800,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(sampleRestaurants) { restaurant ->
                        RestaurantCard(
                            restaurant = restaurant,
                            onReserveClick = {
                                onShowMessage("Réservation pour ${restaurant.name}")
                            }
                        )
                    }
                }
            }
        }

        // Spacer
        item {
            Spacer(modifier = Modifier.height(32.dp))
        }

        // Dish Section
        item {
            Column {
                Text(
                    text = "Mets phares ivoiriens",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Gray800,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(sampleDishes) { dish ->
                        DishCard(dish = dish)
                    }
                }
            }
        }
    }
}
