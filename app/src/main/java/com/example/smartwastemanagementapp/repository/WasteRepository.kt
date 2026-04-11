package com.example.smartwastemanagementapp.repository

import android.net.Uri
import com.example.smartwastemanagementapp.model.WasteReport
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class WasteRepository {
    private val db = FirebaseFirestore.getInstance()
    private val reportsCollection = db.collection("reports")
    private val storage = FirebaseStorage.getInstance()

    suspend fun submitReport(
        description: String,
        imageUri: Uri,
        latitude: Double,
        longitude: Double,
        userId: String
    ): Result<Unit> = try {
        // 1. Upload image to Firebase Storage
        val fileName = UUID.randomUUID().toString()
        val imageRef = storage.reference.child("waste_images/$fileName")
        imageRef.putFile(imageUri).await()
        val imageUrl = imageRef.downloadUrl.await().toString()

        // 2. Save report to Firestore
        val reportRef = reportsCollection.document()
        val report = WasteReport(
            id = reportRef.id,
            description = description,
            imageUrl = imageUrl,
            latitude = latitude,
            longitude = longitude,
            reportedBy = userId
        )
        reportRef.set(report).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getAllReports(): List<WasteReport> = try {
        val snapshot = reportsCollection
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .get()
            .await()
        snapshot.toObjects(WasteReport::class.java)
    } catch (e: Exception) {
        emptyList()
    }
}
