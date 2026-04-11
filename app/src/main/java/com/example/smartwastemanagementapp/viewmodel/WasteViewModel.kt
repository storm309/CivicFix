package com.example.smartwastemanagementapp.viewmodel

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartwastemanagementapp.model.WasteReport
import com.example.smartwastemanagementapp.repository.WasteRepository
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

private const val GEMINI_API_KEY = "AIzaSyBMGGfKztkvtlovWp27oj1GlrngN0DBLKc"

class WasteViewModel(private val repository: WasteRepository = WasteRepository()) : ViewModel() {

    private val _reports = MutableStateFlow<List<WasteReport>>(emptyList())
    val reports: StateFlow<List<WasteReport>> = _reports

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    private val _aiDescription = mutableStateOf<String?>(null)
    val aiDescription: State<String?> = _aiDescription

    private val _isAnalyzing = mutableStateOf(false)
    val isAnalyzing: State<Boolean> = _isAnalyzing

    // Catches any uncaught exception from a coroutine so the app never crashes
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        _isLoading.value = false
        _isAnalyzing.value = false
        _error.value = throwable.localizedMessage ?: "An unexpected error occurred"
    }

    init {
        fetchReports()
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
                _isLoading.value = true
                _error.value = null
                val result = repository.submitReport(description, imageUri, latitude, longitude, userId)
                if (result.isSuccess) {
                    onSuccess()
                    fetchReports()
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
                _error.value = null
                val model = GenerativeModel(
                    modelName = "gemini-2.0-flash",
                    apiKey = GEMINI_API_KEY
                )
                val response = model.generateContent(
                    content {
                        image(bitmap)
                        text(
                            "You are a civic waste management assistant. " +
                            "Look at this image and write a clear, concise 1-2 sentence " +
                            "description of the waste issue for a complaint report. " +
                            "Mention the type of waste and the severity."
                        )
                    }
                )
                _aiDescription.value = response.text?.trim()
            } catch (e: Exception) {
                _error.value = "AI analysis failed: ${e.localizedMessage}"
            } finally {
                _isAnalyzing.value = false
            }
        }
    }

    fun clearAiDescription() { _aiDescription.value = null }
}
