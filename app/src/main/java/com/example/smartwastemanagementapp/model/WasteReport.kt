package com.example.smartwastemanagementapp.model

import com.google.firebase.firestore.PropertyName

data class WasteReport(
    val id: String = "",
    val description: String = "",
    val imageUrl: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val status: String = "Pending", // Pending, Cleaned
    val timestamp: Long = System.currentTimeMillis(),
    val reportedBy: String = ""
)
