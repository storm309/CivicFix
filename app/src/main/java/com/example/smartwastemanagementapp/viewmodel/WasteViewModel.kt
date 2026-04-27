package com.example.smartwastemanagementapp.viewmodel

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartwastemanagementapp.BuildConfig
import com.example.smartwastemanagementapp.model.ReportModerationStatus
import com.example.smartwastemanagementapp.model.WasteReport
import com.example.smartwastemanagementapp.repository.WasteRepository
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class ImageModerationResult(
    val score: Double,
    val label: String,
    val reason: String
)

class WasteViewModel(private val repository: WasteRepository = WasteRepository()) : ViewModel() {

    private val _reports = MutableStateFlow<List<WasteReport>>(emptyList())
    val reports: StateFlow<List<WasteReport>> = _reports

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    private val _aiDescription = mutableStateOf<String?>(null)
    val aiDescription: State<String?> = _aiDescription

    private val _imageModeration = mutableStateOf<ImageModerationResult?>(null)
    val imageModeration: State<ImageModerationResult?> = _imageModeration

    private val _isAnalyzing = mutableStateOf(false)
    val isAnalyzing: State<Boolean> = _isAnalyzing

    private val _pendingReports = MutableStateFlow<List<WasteReport>>(emptyList())
    val pendingReports: StateFlow<List<WasteReport>> = _pendingReports

    // Catches any uncaught exception from a coroutine so the app never crashes
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        _isLoading.value = false
        _isAnalyzing.value = false
        _error.value = throwable.localizedMessage ?: "An unexpected error occurred"
    }

    init {
        fetchReports()
        fetchPendingReports()
    }

    fun fetchReports() {
        viewModelScope.launch(exceptionHandler) {
            try {
                _isLoading.value = true
                _error.value = null
                _reports.value = repository.getAllReports()
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Failed to load reports"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun fetchPendingReports() {
        viewModelScope.launch(exceptionHandler) {
            _pendingReports.value = repository.getPendingModerationReports()
        }
    }

    fun submitReport(
        description: String,
        imageUri: Uri?,             // optional photo
        latitude: Double,
        longitude: Double,
        onSuccess: () -> Unit
    ) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous"
        viewModelScope.launch(exceptionHandler) {
            try {
                val moderation = _imageModeration.value
                if (imageUri != null && moderation == null) {
                    _error.value = "Please analyze image before submitting so unsafe uploads can be blocked"
                    return@launch
                }
                if (imageUri != null && moderation != null && moderation.score < 0.60) {
                    _error.value = "Image blocked by AI moderation. Upload a clear waste-related photo."
                    return@launch
                }
                _isLoading.value = true
                _error.value = null
                val result = repository.submitReport(
                    description = description,
                    imageUri = imageUri,
                    latitude = latitude,
                    longitude = longitude,
                    userId = userId,
                    aiSafetyScore = moderation?.score ?: 0.0,
                    aiSafetyLabel = moderation?.label ?: "unchecked"
                )
                if (result.isSuccess) {
                    onSuccess()
                    fetchReports()
                    fetchPendingReports()
                    _imageModeration.value = null
                } else {
                    _error.value = result.exceptionOrNull()?.localizedMessage ?: "Submit failed"
                }
            } catch (e: Exception) {
                _error.value = e.localizedMessage ?: "Failed to submit report"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /** Analyse a waste photo with Gemini 2.0 Flash and fill aiDescription */
    fun analyzeWasteImage(bitmap: Bitmap) {
        viewModelScope.launch(exceptionHandler) {
            try {
                _isAnalyzing.value = true
                _aiDescription.value = null
                _imageModeration.value = null
                _error.value = null
                val model = GenerativeModel(
                    modelName = "gemini-2.0-flash",
                    apiKey = BuildConfig.GEMINI_API_KEY
                )
                val response = model.generateContent(
                    content {
                        image(bitmap)
                        text(
                            "You are a civic waste management assistant. " +
                            "Look at this image and write a clear, concise 1-2 sentence " +
                            "description of the waste issue for a complaint report. " +
                            "Mention the type of waste and the severity. " +
                            "Then in next line output this format exactly: SAFETY:<score_0_to_1>|<safe_or_unsafe>|<reason>. " +
                            "Mark unsafe if image is unrelated, explicit, abusive, or not waste evidence."
                        )
                    }
                )
                val raw = response.text?.trim().orEmpty()
                _aiDescription.value = raw.lineSequence().firstOrNull()?.trim().orEmpty()
                _imageModeration.value = parseSafetyLine(raw)
            } catch (e: Exception) {
                _error.value = "AI analysis failed: ${e.localizedMessage}"
            } finally {
                _isAnalyzing.value = false
            }
        }
    }

    fun approveReport(reportId: String, note: String = "Approved by admin") {
        viewModelScope.launch(exceptionHandler) {
            val adminId = FirebaseAuth.getInstance().currentUser?.uid ?: "admin"
            val result = repository.updateModerationStatus(
                reportId = reportId,
                status = ReportModerationStatus.APPROVED,
                moderatedBy = adminId,
                note = note
            )
            if (result.isFailure) {
                _error.value = result.exceptionOrNull()?.localizedMessage ?: "Approval failed"
            }
            fetchPendingReports()
            fetchReports()
        }
    }

    fun rejectReport(reportId: String, note: String = "Rejected by admin") {
        viewModelScope.launch(exceptionHandler) {
            val adminId = FirebaseAuth.getInstance().currentUser?.uid ?: "admin"
            val result = repository.updateModerationStatus(
                reportId = reportId,
                status = ReportModerationStatus.REJECTED,
                moderatedBy = adminId,
                note = note
            )
            if (result.isFailure) {
                _error.value = result.exceptionOrNull()?.localizedMessage ?: "Rejection failed"
            }
            fetchPendingReports()
            fetchReports()
        }
    }

    fun clearAiDescription() {
        _aiDescription.value = null
    }

    private fun parseSafetyLine(raw: String): ImageModerationResult {
        val line = raw.lineSequence().firstOrNull { it.trim().startsWith("SAFETY:", ignoreCase = true) }
            ?.substringAfter("SAFETY:")
            ?.trim()
            .orEmpty()
        val parts = line.split("|")
        val score = parts.getOrNull(0)?.trim()?.toDoubleOrNull()?.coerceIn(0.0, 1.0) ?: 0.5
        val label = parts.getOrNull(1)?.trim()?.lowercase().orEmpty().ifBlank {
            if (score >= 0.60) "safe" else "unsafe"
        }
        val reason = parts.getOrNull(2)?.trim().orEmpty().ifBlank {
            if (label == "safe") "Looks relevant to waste issue" else "Image is not clear waste evidence"
        }
        return ImageModerationResult(score = score, label = label, reason = reason)
    }
}
