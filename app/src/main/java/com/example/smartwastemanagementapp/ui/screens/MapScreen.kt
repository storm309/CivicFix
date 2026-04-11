package com.example.smartwastemanagementapp.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.example.smartwastemanagementapp.viewmodel.WasteViewModel
import com.google.android.gms.maps.CameraUpdateFactory
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

    var locationPermissionGranted by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { locationPermissionGranted = it }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    // Default: India centre; zoom to first report if available
    val defaultLatLng = LatLng(20.5937, 78.9629)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLatLng, 5f)
    }

    LaunchedEffect(reports) {
        val first = reports.firstOrNull { it.latitude != 0.0 || it.longitude != 0.0 }
        if (first != null) {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(LatLng(first.latitude, first.longitude), 12f)
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Waste Locations", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        GoogleMap(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            cameraPositionState = cameraPositionState,
            properties = MapProperties(
                isMyLocationEnabled = locationPermissionGranted
            ),
            uiSettings = MapUiSettings(
                zoomControlsEnabled = true,
                myLocationButtonEnabled = locationPermissionGranted
            )
        ) {
            reports.forEach { report ->
                if (report.latitude != 0.0 || report.longitude != 0.0) {
                    Marker(
                        state = MarkerState(
                            position = LatLng(report.latitude, report.longitude)
                        ),
                        title = report.description.take(60).ifBlank { "Waste Report" },
                        snippet = "Status: ${report.status}"
                    )
                }
            }
        }
    }
}
