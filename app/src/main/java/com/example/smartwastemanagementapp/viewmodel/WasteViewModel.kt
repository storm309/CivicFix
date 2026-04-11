package com.example.smartwastemanagementapp.viewmodel

import android.net.Uri
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartwastemanagementapp.model.WasteReport
import com.example.smartwastemanagementapp.repository.WasteRepository
import com.google.firebase.auth.FirebaseAuth
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

    init {
        fetchReports()
    }

    fun fetchReports() {
        viewModelScope.launch {
            _isLoading.value = true
            _reports.value = repository.getAllReports()
            _isLoading.value = false
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
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.submitReport(description, imageUri, latitude, longitude, userId)
            _isLoading.value = false
            if (result.isSuccess) {
                onSuccess()
                fetchReports()
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Unknown Error"
            }
        }
    }
}
