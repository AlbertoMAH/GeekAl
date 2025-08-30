package com.example.restaurantefinal.ui.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.restaurantefinal.Dish
import com.example.restaurantefinal.ui.theme.Gray800
import com.example.restaurantefinal.ui.theme.Gray900

@Composable
fun DishCard(dish: Dish, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.width(160.dp), // Increased from 144dp
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White)
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = rememberAsyncImagePainter(dish.imageUrl),
                contentDescription = dish.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .height(100.dp) // Decreased from 112dp
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = dish.name,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Gray900,
                textAlign = TextAlign.Center
            )
            Text(
                text = dish.description,
                fontSize = 12.sp,
                color = Gray800,
                textAlign = TextAlign.Center
            )
        }
    }
}
