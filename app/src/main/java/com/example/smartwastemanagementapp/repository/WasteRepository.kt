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

    // Use the EXACT bucket name from google-services.json to fix "Object not found"
    private val storage = FirebaseStorage.getInstance("gs://civicfix-92e86.firebasestorage.app")

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

                android.util.Log.d("WasteRepository", "Uploading image: $fileName")
                val uploadTask = imageRef.putBytes(imageBytes).await()
                val url = uploadTask.storage.downloadUrl.await().toString()

                android.util.Log.d("WasteRepository", "✓ Image uploaded: ${url.take(50)}...")
                url
            } catch (imgError: Exception) {
                android.util.Log.w("WasteRepository", "Image upload failed: ${imgError.message}, continuing without image")
                ""
            }
        } else {
            android.util.Log.d("WasteRepository", "→ No image provided")
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
        val userPointsRef = firebaseDb.getReference("users").child(userId).child("impactPoints")
        val currentPoints = userPointsRef.get().await().getValue(Int::class.java) ?: 0
        userPointsRef.setValue(currentPoints + points).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateModerationStatus(
        reportId: String,
        status: ReportModerationStatus,
        moderatedBy: String,
        note: String
    ): Result<Unit> = try {
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
            val reporterId = reportsRef.child(reportId).child("reportedBy").get().await().getValue(String::class.java)
            if (reporterId != null) {
                rewardUser(reporterId, 50) // Give 50 points for approved report
            }
        }

        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
