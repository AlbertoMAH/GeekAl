package com.mral.geektest.model

data class Medication(
    val id: Int,
    val name: String,
    val dosage: String,
    val intakeTime: String,
    val startDate: String,
    val endDate: String,
    // Optional fields
    val form: String? = null,
    val dosageUnit: String? = null,
    val quantityPerIntake: String? = null,
    val frequency: String? = null,
    val reminderMode: String = "Notification",
    val repeatReminder: Boolean = false,
    val hideNameInNotification: Boolean = false
)
