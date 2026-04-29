package com.example.smartwastemanagementapp.model

data class WasteReport(
    val id: String = "",
    val description: String = "",
    val descriptionHi: String = "",
    val imageUrl: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val locationAccuracyMeters: Float = -1f,
    val locationCapturedAt: Long = 0L,
    val status: String = "Pending", // Pending, Cleaned
    val moderationStatus: String = com.example.smartwastemanagementapp.model.ReportModerationStatus.PENDING_APPROVAL.dbValue,
    val moderationUpdatedAt: Long = 0L,
    val moderationNote: String = "",
    val moderatedBy: String = "",
    val aiSafetyScore: Double = 0.0,
    val aiSafetyLabel: String = "unchecked",
    val locationAddress: String = "", // Added for human-readable location
    val timestamp: Long = System.currentTimeMillis(),
    val reportedBy: String = "",
    val upvotes: Map<String, Boolean> = emptyMap(), // Map of UID -> true
    val priority: Int = 0 // Derived from upvote count
)
