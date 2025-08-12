package com.mral.geektest.model

import androidx.compose.runtime.mutableStateListOf
import kotlin.random.Random

object MedicationRepository {
    private val medications = mutableStateListOf<Medication>()

    fun getAllMedications(): List<Medication> {
        return medications
    }

    fun addMedication(medication: Medication) {
        // In a real app, the ID would be handled by the database.
        // For this in-memory version, we'll assign a random ID.
        medications.add(medication.copy(id = Random.nextInt()))
    }
}
