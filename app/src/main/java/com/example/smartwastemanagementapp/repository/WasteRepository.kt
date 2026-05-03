package com.example.smartwastemanagementapp.repository

import android.net.Uri
import com.example.smartwastemanagementapp.model.ReportModerationStatus
import com.example.smartwastemanagementapp.model.WasteReport
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class WasteRepository {
    // Use explicit URL to ensure it connects to the correct regional instance
    private val firebaseDb = FirebaseDatabase
        .getInstance("https://civicfix-92e86-default-rtdb.firebaseio.com")

    private val reportsRef = firebaseDb.getReference("reports")

    // Use default instance which automatically uses the bucket from google-services.json
    private val storage = FirebaseStorage.getInstance()

    suspend fun submitReport(
        description: String,
        descriptionHi: String = "",
        imageBytes: ByteArray?,
        latitude: Double,
        longitude: Double,
        locationAccuracyMeters: Float,
        locationCapturedAt: Long,
        userId: String,
        aiSafetyScore: Double = 0.0,
        aiSafetyLabel: String = "unchecked",
        locationAddress: String = ""
    ): Result<Unit> = try {
        android.util.Log.d("WasteRepository", "=== Starting Report Submission ===")
        android.util.Log.d("WasteRepository", "User ID: $userId")
        android.util.Log.d("WasteRepository", "Location: ($latitude, $longitude)")

        // Upload image if provided
        val imageUrl = if (imageBytes != null && imageBytes.isNotEmpty()) {
            try {
                val fileName = "waste_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(5)}.jpg"
                val imageRef = storage.reference.child("waste_images/$fileName")

                android.util.Log.d("WasteRepository", "Uploading image: $fileName to ${imageRef.path}")
                imageRef.putBytes(imageBytes).await()
                
                // Get the long HTTPS URL
                val url = imageRef.downloadUrl.await().toString()

                android.util.Log.d("WasteRepository", "✓ Image uploaded successfully. URL: $url")
                url
            } catch (imgError: Exception) {
                android.util.Log.e("WasteRepository", "✗ Image upload failed: ${imgError.message}")
                android.util.Log.e("WasteRepository", "Full error: ${imgError.stackTraceToString()}")
                // Return empty string but log error clearly
                ""
            }
        } else {
            android.util.Log.d("WasteRepository", "→ No image provided or bytes empty")
            ""
        }

        // Create report object
        val reportId = reportsRef.push().key ?: UUID.randomUUID().toString()
        val report = WasteReport(
            id = reportId,
            description = description,
            descriptionHi = descriptionHi,
            imageUrl = imageUrl,
            latitude = latitude,
            longitude = longitude,
            locationAccuracyMeters = locationAccuracyMeters,
            locationCapturedAt = locationCapturedAt,
            moderationStatus = ReportModerationStatus.PENDING_APPROVAL.dbValue,
            moderationUpdatedAt = System.currentTimeMillis(),
            moderationNote = "Awaiting admin review",
            aiSafetyScore = aiSafetyScore,
            aiSafetyLabel = aiSafetyLabel,
            reportedBy = userId,
            locationAddress = locationAddress
        )

        // Write to Firebase
        android.util.Log.d("WasteRepository", "Writing to: /reports/$reportId")
        reportsRef.child(reportId).setValue(report).await()

        android.util.Log.d("WasteRepository", "✓✓✓ Report submitted successfully at /reports/$reportId!")
        Result.success(Unit)

    } catch (e: Exception) {
        val errorMsg = when {
            e.message?.contains("PERMISSION_DENIED") == true ->
                "🔒 Permission Denied: Firebase Rules don't allow writes. " +
                "Check: Firebase Console > Realtime Database > Rules > Set '.write': 'auth != null' for reports"
            e.message?.contains("Object does not exist") == true ||
            e.message?.contains("path", ignoreCase = true) == true ->
                "❌ Database Error: /reports path issue. " +
                "IMPORTANT: Ensure Firebase Realtime Database is enabled for this project and the database URL matches exactly."
            e.message?.contains("does not have permission") == true ->
                "🔒 Permission Error: User lacks write access to /reports"
            e.message?.contains("auth") == true || e.message?.contains("Authentication") == true ->
                "🔑 Authentication Error: User not properly logged in"
            e.message?.contains("network", ignoreCase = true) == true ->
                "🌐 Network Error: Check internet connection"
            e.message?.contains("timeout", ignoreCase = true) == true ->
                "⏱️ Timeout Error: Firebase is slow. Retry."
            else -> "❌ Error: ${e.message?.take(80) ?: "Submit failed"}"
        }
        android.util.Log.e("WasteRepository", "✗ Submission failed: $errorMsg")
        android.util.Log.e("WasteRepository", "Full error: ${e.stackTraceToString()}")
        Result.failure(Exception(errorMsg))
    }

    suspend fun getAllReports(): List<WasteReport> = try {
        val snapshot = reportsRef.get().await()
        snapshot.children.mapNotNull { it.getValue(WasteReport::class.java) }
            .sortedByDescending { it.timestamp }
    } catch (e: Exception) {
        android.util.Log.w("WasteRepository", "Failed to get reports: ${e.message}")
        emptyList()
    }

    suspend fun getPendingModerationReports(): List<WasteReport> {
        return getAllReports().filter {
            ReportModerationStatus.from(it.moderationStatus) == ReportModerationStatus.PENDING_APPROVAL
        }
    }

    suspend fun upvoteReport(reportId: String, userId: String): Result<Unit> = try {
        val upvoteRef = reportsRef.child(reportId).child("upvotes").child(userId)
        val snapshot = upvoteRef.get().await()
        
        if (snapshot.exists()) {
            // Already upvoted, so remove it (Toggle)
            upvoteRef.removeValue().await()
        } else {
            // Add upvote
            upvoteRef.setValue(true).await()
        }
        
        // Recalculate priority (Simple version: priority = upvote count)
        val updatedReportSnapshot = reportsRef.child(reportId).get().await()
        val upvotesCount = updatedReportSnapshot.child("upvotes").childrenCount.toInt()
        reportsRef.child(reportId).child("priority").setValue(upvotesCount).await()
        
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun rewardUser(userId: String, points: Int): Result<Unit> = try {
        android.util.Log.d("WasteRepository", "Attempting to reward user $userId with $points points")
        val userPointsRef = firebaseDb.getReference("users").child(userId).child("impactPoints")
        val snapshot = userPointsRef.get().await()
        val currentPoints = snapshot.getValue(Int::class.java) ?: 0
        userPointsRef.setValue(currentPoints + points).await()
        android.util.Log.d("WasteRepository", "✓ Successfully rewarded user $userId. New total: ${currentPoints + points}")
        Result.success(Unit)
    } catch (e: Exception) {
        android.util.Log.e("WasteRepository", "✗ Failed to reward user $userId: ${e.message}")
        Result.failure(e)
    }

    suspend fun updateModerationStatus(
        reportId: String,
        status: ReportModerationStatus,
        moderatedBy: String,
        note: String
    ): Result<Unit> = try {
        android.util.Log.d("WasteRepository", "Updating moderation status for $reportId to $status")
        val updates = mutableMapOf<String, Any>(
            "moderationStatus" to status.dbValue,
            "moderationUpdatedAt" to System.currentTimeMillis(),
            "moderationNote" to note,
            "moderatedBy" to moderatedBy,
            "status" to if (status == ReportModerationStatus.APPROVED) "Pending" else "Rejected"
        )
        reportsRef.child(reportId).updateChildren(updates).await()
        
        // REWARD LOGIC: If approved, give points to the original reporter
        if (status == ReportModerationStatus.APPROVED) {
            android.util.Log.d("WasteRepository", "Report approved! Fetching reporter ID for $reportId")
            try {
                // Fetch directly to avoid any parsing issues with the whole object
                val reporterSnapshot = reportsRef.child(reportId).child("reportedBy").get().await()
                val reporterId = reporterSnapshot.getValue(String::class.java)
                
                android.util.Log.d("WasteRepository", "Reporter ID fetched: $reporterId")
                
                if (!reporterId.isNullOrBlank()) {
                    android.util.Log.d("WasteRepository", "Triggering reward for $reporterId")
                    rewardUser(reporterId, 50) 
                } else {
                    android.util.Log.w("WasteRepository", "Could not find reporter ID for report $reportId. Snap: ${reporterSnapshot.value}")
                }
            } catch (e: Exception) {
                android.util.Log.e("WasteRepository", "Error fetching reporter ID: ${e.message}")
            }
        }

        Result.success(Unit)
    } catch (e: Exception) {
        android.util.Log.e("WasteRepository", "✗ Failed to update moderation status: ${e.message}")
        Result.failure(e)
    }
}
