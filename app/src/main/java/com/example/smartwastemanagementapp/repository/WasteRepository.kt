package com.example.smartwastemanagementapp.repository

import android.net.Uri
import com.example.smartwastemanagementapp.model.WasteReport
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class WasteRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()
    private val reportsCollection = firestore.collection("reports")

    suspend fun submitReport(
        description: String,
        imageUri: Uri,
        latitude: Double,
        longitude: Double,
        userId: String
    ): Result<Unit> = try {
        // 1. Upload Image to Firebase Storage
        val fileName = UUID.randomUUID().toString()
        val imageRef = storage.reference.child("waste_images/$fileName")
        imageRef.putFile(imageUri).await()
        val imageUrl = imageRef.downloadUrl.await().toString()

        // 2. Save Report to Firestore
        val reportId = reportsCollection.document().id
        val report = WasteReport(
            id = reportId,
            description = description,
            imageUrl = imageUrl,
            latitude = latitude,
            longitude = longitude,
            reportedBy = userId
        )
        reportsCollection.document(reportId).set(report).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getAllReports(): List<WasteReport> = try {
        reportsCollection.get().await().toObjects(WasteReport::class.java)
    } catch (e: Exception) {
        emptyList()
    }
}
