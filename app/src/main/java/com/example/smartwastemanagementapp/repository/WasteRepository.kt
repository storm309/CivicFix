package com.example.smartwastemanagementapp.repository

import android.net.Uri
import com.example.smartwastemanagementapp.model.ReportModerationStatus
import com.example.smartwastemanagementapp.model.WasteReport
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class WasteRepository {
    private val database = FirebaseDatabase
        .getInstance("https://civicfix-92e86-default-rtdb.firebaseio.com")
        .getReference("reports")
    private val storage = FirebaseStorage.getInstance()

    suspend fun submitReport(
        description: String,
        imageUri: Uri?,          // nullable – photo is optional
        latitude: Double,
        longitude: Double,
        userId: String,
        aiSafetyScore: Double = 0.0,
        aiSafetyLabel: String = "unchecked"
    ): Result<Unit> = try {
        // Upload image only if provided
        val imageUrl = if (imageUri != null) {
            val fileName = UUID.randomUUID().toString()
            val imageRef = storage.reference.child("waste_images/$fileName")
            imageRef.putFile(imageUri).await()
            imageRef.downloadUrl.await().toString()
        } else {
            ""
        }

        val reportId = database.push().key ?: UUID.randomUUID().toString()
        val report = WasteReport(
            id = reportId,
            description = description,
            imageUrl = imageUrl,
            latitude = latitude,
            longitude = longitude,
            moderationStatus = ReportModerationStatus.PENDING_APPROVAL.dbValue,
            moderationUpdatedAt = System.currentTimeMillis(),
            moderationNote = "Awaiting admin review",
            aiSafetyScore = aiSafetyScore,
            aiSafetyLabel = aiSafetyLabel,
            reportedBy = userId
        )
        database.child(reportId).setValue(report).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getAllReports(): List<WasteReport> = try {
        val snapshot = database.get().await()
        snapshot.children.mapNotNull { it.getValue(WasteReport::class.java) }
            .sortedByDescending { it.timestamp }
    } catch (e: Exception) {
        emptyList()
    }

    suspend fun getPendingModerationReports(): List<WasteReport> {
        return getAllReports().filter {
            ReportModerationStatus.from(it.moderationStatus) == ReportModerationStatus.PENDING_APPROVAL
        }
    }

    suspend fun updateModerationStatus(
        reportId: String,
        status: ReportModerationStatus,
        moderatedBy: String,
        note: String
    ): Result<Unit> = try {
        val updates = mapOf(
            "moderationStatus" to status.dbValue,
            "moderationUpdatedAt" to System.currentTimeMillis(),
            "moderationNote" to note,
            "moderatedBy" to moderatedBy,
            "status" to if (status == ReportModerationStatus.APPROVED) "Pending" else "Rejected"
        )
        database.child(reportId).updateChildren(updates).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
