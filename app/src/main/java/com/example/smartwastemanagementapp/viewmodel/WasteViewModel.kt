package com.example.smartwastemanagementapp.viewmodel

import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartwastemanagementapp.model.WasteReport
import com.example.smartwastemanagementapp.repository.WasteRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class WasteViewModel(private val repository: WasteRepository = WasteRepository()) : ViewModel() {

    private val _reports = MutableStateFlow<List<WasteReport>>(emptyList())
    val reports: StateFlow<List<WasteReport>> = _reports

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _error = mutableStateOf<String?>(null)
    val error: State<String?> = _error

    // Catches any uncaught exception from a coroutine so the app never crashes
    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        _isLoading.value = false
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
        imageUri: Uri,
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
}
