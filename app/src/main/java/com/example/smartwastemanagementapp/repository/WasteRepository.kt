package com.example.smartwastemanagementapp.repository

import android.net.Uri
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
        userId: String
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
}
