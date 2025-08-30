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
        verticalArrangement = Arrangement.spacedBy(16.dp)
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
                modifier = Modifier.fillMaxWidth()
            ) { page ->
                RestaurantCard(restaurant = sampleRestaurants[page])
            }
        }

        // === Dish Section ===
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Mets ivoiriens phares",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        item {
            val pagerState = rememberPagerState(pageCount = { sampleDishes.size })
            HorizontalPager(
                state = pagerState,
                contentPadding = PaddingValues(horizontal = 16.dp),
                pageSpacing = 16.dp,
                modifier = Modifier.fillMaxWidth()
            ) { page ->
                DishCard(dish = sampleDishes[page])
            }
        }
    }
}
