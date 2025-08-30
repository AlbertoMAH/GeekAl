package com.example.restaurantefinal.ui.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.restaurantefinal.Restaurant
import com.example.restaurantefinal.ui.theme.Indigo600
import com.example.restaurantefinal.ui.theme.Indigo700

@Composable
fun RestaurantCard(
    restaurant: Restaurant,
    onReserveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.width(256.dp), // w-64
        shape = RoundedCornerShape(16.dp), // rounded-2xl
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp), // shadow-lg
        colors = CardDefaults.cardColors(containerColor = androidx.compose.ui.graphics.Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) { // p-4
            Image(
                painter = rememberAsyncImagePainter(restaurant.imageUrl),
                contentDescription = restaurant.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .height(160.dp) // h-40
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)) // rounded-xl
            )
            Spacer(modifier = Modifier.height(12.dp)) // mb-3
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = restaurant.name,
                    fontSize = 18.sp, // text-lg
                    fontWeight = FontWeight.Bold,
                    color = com.example.restaurantefinal.ui.theme.Gray900
                )
                Text(
                    text = restaurant.cuisine,
                    fontSize = 14.sp, // text-sm
                    color = com.example.restaurantefinal.ui.theme.Gray500,
                    modifier = Modifier.padding(bottom = 8.dp) // mb-2
                )
                Text(
                    text = restaurant.description,
                    fontSize = 12.sp, // text-xs
                    color = com.example.restaurantefinal.ui.theme.Gray800
                )
            }
            Spacer(modifier = Modifier.height(16.dp)) // mt-4
            Button(
                onClick = onReserveClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(50), // rounded-full
                colors = ButtonDefaults.buttonColors(
                    containerColor = Indigo600,
                    contentColor = androidx.compose.ui.graphics.Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp) // shadow-lg
            ) {
                Text(text = "Réserver", fontWeight = FontWeight.Medium)
            }
        }
    }
}
