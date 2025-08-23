package com.mral.geektest.ui.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SearchInProgressSheetContent(onClose: () -> Unit) {
    Box(
        // This outer Box is necessary to align the 'N' and close buttons relative to the content.
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp), // Inner padding
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(24.dp)) // Spacer for the close button
            Text(
                text = "Recherche en cours...",
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Nous recherchons les dépanneurs les plus proches de votre position.",
                fontSize = 16.sp,
                color = Color.DarkGray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = Color(0xFFEF4444), // red-500
                strokeWidth = 6.dp
            )
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = "Analyse de votre position...",
                fontSize = 16.sp,
                color = Color.DarkGray
            )
            Spacer(modifier = Modifier.height(24.dp)) // Spacer for the 'N' button
        }

        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 8.dp, end = 8.dp)
        ) {
            Icon(Icons.Filled.Close, contentDescription = "Close", tint = Color.Gray)
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 8.dp, start = 8.dp)
                .size(32.dp)
                .background(Color(0xFFE5E7EB), shape = CircleShape), // gray-200
            contentAlignment = Alignment.Center
        ) {
            Text(text = "N", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color.Black)
        }
    }
}
