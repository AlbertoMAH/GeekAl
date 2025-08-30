package com.example.restaurantefinal.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.restaurantefinal.sampleDishes
import com.example.restaurantefinal.sampleRestaurants
import com.example.restaurantefinal.ui.composables.DishCard
import com.example.restaurantefinal.ui.composables.RestaurantCard

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HomeScreen() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 16.dp),
    ) {
        // === Restaurant Section ===
        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                Text(
                    text = "Pour toi",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Restaurants partenaires",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Normal
                )
            }
        }

        item {
            val pagerState = rememberPagerState(pageCount = { sampleRestaurants.size })
            HorizontalPager(
                state = pagerState,
                contentPadding = PaddingValues(horizontal = 16.dp),
                pageSpacing = 16.dp,
                modifier = Modifier.fillMaxWidth().height(200.dp) // Set height for pager
            ) { page ->
                RestaurantCard(restaurant = sampleRestaurants[page])
            }
        }

        // === Dish Section ===
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Mets ivoiriens phares",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Using a sub-composable for the grid to keep the main LazyColumn clean
        item {
            DishGrid()
        }
    }
}

@Composable
private fun DishGrid() {
    // This grid is not independently scrollable; it lays out its items within the parent LazyColumn.
    // The height is determined by its content.
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        // We need to set a fixed height for a nested lazy layout.
        // This is a common challenge. A simple way is to calculate the required height.
        // (Number of rows) * (height of a card + vertical spacing)
        // Rows = ceil(dishes.size / 2)
        modifier = Modifier.height( ( (sampleDishes.size + 1) / 2 * 160 ).dp) // Approx height
    ) {
        items(sampleDishes) { dish ->
            DishCard(dish = dish)
        }
    }
}
