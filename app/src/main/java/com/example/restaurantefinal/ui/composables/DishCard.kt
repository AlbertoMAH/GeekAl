package com.example.restaurantefinal.ui.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
        modifier = modifier.width(144.dp), // w-36
        shape = RoundedCornerShape(16.dp), // rounded-2xl
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp), // shadow-lg
        colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White)
    ) {
        Column(
            modifier = Modifier.padding(12.dp).fillMaxWidth(), // p-3
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = rememberAsyncImagePainter(dish.imageUrl),
                contentDescription = dish.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .height(112.dp) // h-28
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp)) // rounded-lg
            )
            Spacer(modifier = Modifier.height(8.dp)) // mb-2
            Text(
                text = dish.name,
                fontSize = 16.sp, // text-base
                fontWeight = FontWeight.Bold,
                color = Gray900,
                textAlign = TextAlign.Center
            )
            Text(
                text = dish.description,
                fontSize = 12.sp, // text-xs
                color = Gray800,
                textAlign = TextAlign.Center
            )
        }
    }
}
