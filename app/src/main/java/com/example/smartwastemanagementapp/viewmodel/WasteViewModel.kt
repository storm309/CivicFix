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

    private val _aiDescriptionHi = mutableStateOf<String?>(null)
    val aiDescriptionHi: State<String?> = _aiDescriptionHi

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
        context: android.content.Context,
        description: String,
        descriptionHi: String = "",
        address: String,
        imageUri: Uri?,
        latitude: Double,
        longitude: Double,
        locationAccuracyMeters: Float,
        onSuccess: () -> Unit
    ) {
        val currentUser = FirebaseAuth.getInstance().currentUser

        if (currentUser == null) {
            _error.value = "❌ Authentication Error: You must be logged in to submit a report."
            return
        }

        val userId = currentUser.uid

        viewModelScope.launch(exceptionHandler) {
            try {
                val moderation = _imageModeration.value
                if (imageUri != null && moderation == null) {
                    _error.value = "Please analyze image before submitting"
                    return@launch
                }
                if (imageUri != null && moderation != null && moderation.score < 0.60) {
                    _error.value = "Image blocked by AI moderation."
                    return@launch
                }

                _isLoading.value = true
                _error.value = null

                val imageBytes = imageUri?.let { uri ->
                    context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                }

                val result = repository.submitReport(
                    description = description,
                    descriptionHi = descriptionHi,
                    imageBytes = imageBytes,
                    latitude = latitude,
                    longitude = longitude,
                    locationAccuracyMeters = locationAccuracyMeters,
                    locationCapturedAt = System.currentTimeMillis(),
                    userId = userId,
                    aiSafetyScore = moderation?.score ?: 0.0,
                    aiSafetyLabel = moderation?.label ?: "unchecked",
                    locationAddress = address
                )
                if (result.isSuccess) {
                    onSuccess()
                    fetchReports()
                    fetchPendingReports()
                    _imageModeration.value = null
                    _aiDescriptionHi.value = null
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

    /** Analyse a waste photo with configured Gemini model and fill aiDescription */
    fun analyzeWasteImage(bitmap: Bitmap) {
        viewModelScope.launch(exceptionHandler) {
            try {
                _isAnalyzing.value = true
                _aiDescription.value = null
                _imageModeration.value = null
                _error.value = null

                if (BuildConfig.GEMINI_API_KEY.isBlank()) {
                    _error.value = "Gemini API key is missing. Add GEMINI_API_KEY in local.properties"
                    return@launch
                }

                // Primary model based on your API key's availability
                val modelsToTry = listOf("gemini-2.5-flash", "gemini-2.0-flash", "gemini-1.5-flash")
                var raw = ""
                var lastException: Exception? = null

                for (modelName in modelsToTry) {
                    try {
                        android.util.Log.d("WasteViewModel", "Attempting AI analysis with $modelName")
                        val model = GenerativeModel(
                            modelName = modelName,
                            apiKey = BuildConfig.GEMINI_API_KEY
                        )
                        val response = model.generateContent(
                            content {
                                image(bitmap)
                                text(
                                    "You are an expert environmental and waste management inspector. " +
                                        "Look at this image and write a detailed, professional 2-3 sentence " +
                                        "description of the waste issue for a formal complaint report. " +
                                        "1. Identify the specific materials (e.g., plastic, organic, construction debris, electronics). " +
                                        "2. Estimate the volume/severity (e.g., overflowing bin, scattered litter, large dump). " +
                                        "3. Mention any immediate risks (e.g., blocking a sidewalk, near a water body, hygiene hazard). " +
                                        "Provide the description in BOTH English and Hindi. The Hindi should be professional and natural-sounding. " +
                                        "Respond strictly in this format:\n" +
                                        "EN: <english_description>\n" +
                                        "HI: <hindi_description>\n" +
                                        "SAFETY:<score_0_to_1>|<safe_or_unsafe>|<reason>\n" +
                                        "Mark unsafe if image is unrelated to waste, explicit, abusive, or contains personal identification."
                                )
                            }
                        )
                        raw = response.text?.trim().orEmpty()
                        if (raw.isNotBlank()) break // Success!
                    } catch (e: Exception) {
                        android.util.Log.w("WasteViewModel", "Model $modelName failed: ${e.message}")
                        lastException = e
                    }
                }

                if (raw.isBlank()) {
                    throw lastException ?: IllegalStateException("All AI models failed")
                }

                val enDesc = raw.lineSequence()
                    .firstOrNull { it.startsWith("EN:", ignoreCase = true) }?.substringAfter(":")?.trim()
                val hiDesc = raw.lineSequence()
                    .firstOrNull { it.startsWith("HI:", ignoreCase = true) }?.substringAfter(":")?.trim()

                _aiDescription.value = enDesc ?: raw.lineSequence().firstOrNull()?.trim()
                _aiDescriptionHi.value = hiDesc ?: _aiDescription.value
                _imageModeration.value = parseSafetyLine(raw)
            } catch (e: Exception) {
                _error.value = mapAiError(e)
            } finally {
                _isAnalyzing.value = false
            }
        }
    }

    private fun mapAiError(error: Throwable): String {
        val msg = error.localizedMessage.orEmpty()
        android.util.Log.e("WasteViewModel", "AI Error Detail: $msg")
        
        return when {
            msg.contains("PERMISSION_DENIED", ignoreCase = true) || msg.contains("403") ->
                "AI access denied (403). Check API key billing and 'Generative Language API' in Google Cloud Console."
            msg.contains("quota", ignoreCase = true) || msg.contains("429") ->
                "AI quota exceeded. Paid keys still have limits. Please retry in a minute."
            msg.contains("API key", ignoreCase = true) || msg.contains("invalid", ignoreCase = true) ->
                "Invalid Gemini API key. Please check your key in AI Studio."
            msg.contains("network", ignoreCase = true) || msg.contains("timeout", ignoreCase = true) ->
                "Network issue. Check WiFi/Data and retry."
            else -> "AI error: ${msg.take(80)}. Check API setup or billing status."
        }
    }

    fun upvoteReport(reportId: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        viewModelScope.launch(exceptionHandler) {
            repository.upvoteReport(reportId, userId)
            fetchReports()
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
        _aiDescriptionHi.value = null
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
