package com.example.smartwastemanagementapp.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.smartwastemanagementapp.viewmodel.WasteViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onBack: () -> Unit,
    viewModel: WasteViewModel
) {
    val reports by viewModel.reports.collectAsState()
    
    // Default position (can be changed to user's current location)
    val defaultPos = LatLng(20.5937, 78.9629) // India
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultPos, 5f)
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Waste Locations Map") })
        }
    ) { padding ->
        GoogleMap(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            cameraPositionState = cameraPositionState
        ) {
            reports.forEach { report ->
                Marker(
                    state = MarkerState(position = LatLng(report.latitude, report.longitude)),
                    title = report.description,
                    snippet = "Status: ${report.status}"
                )
            }
        }
    }
}
