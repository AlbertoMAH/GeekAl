package com.mral.geektest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.mral.geektest.ui.medication.AddMedicationScreen
import com.mral.geektest.ui.medication.MedicationScreen
import com.mral.geektest.ui.theme.GeekTestTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            GeekTestTheme {
                var showAddMedicationScreen by remember { mutableStateOf(false) }

                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    if (showAddMedicationScreen) {
                        AddMedicationScreen(onClose = { showAddMedicationScreen = false })
                    } else {
                        MedicationScreen(onAddMedicationClick = { showAddMedicationScreen = true })
                    }
                }
            }
        }
    }
}