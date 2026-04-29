package com.example.smartwastemanagementapp.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.smartwastemanagementapp.R
import com.example.smartwastemanagementapp.model.WasteReport
import com.example.smartwastemanagementapp.viewmodel.WasteViewModel
import com.example.smartwastemanagementapp.util.LanguageManager
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(onBack: () -> Unit, viewModel: WasteViewModel) {
    val reports by viewModel.reports.collectAsState()
    val context = LocalContext.current
    val currentLang = LanguageManager.getSelectedLanguage(context)
    val validReports = reports.filter { 
        (it.latitude != 0.0 || it.longitude != 0.0) && 
        com.example.smartwastemanagementapp.model.ReportModerationStatus.from(it.moderationStatus) == com.example.smartwastemanagementapp.model.ReportModerationStatus.APPROVED 
    }

    var locationPermissionGranted by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { locationPermissionGranted = it }
    LaunchedEffect(Unit) { permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION) }

    val cameraPositionState = rememberCameraPositionState { position = CameraPosition.fromLatLngZoom(LatLng(20.5937, 78.9629), 5f) }
    var selectedReport by remember { mutableStateOf<WasteReport?>(null) }

    LaunchedEffect(validReports) { validReports.firstOrNull()?.let { cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 12f)) } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Column { Text(stringResource(R.string.waste_map), fontWeight = FontWeight.ExtraBold); Text(stringResource(R.string.pinned_locations, validReports.size.toString()), style = MaterialTheme.typography.bodySmall) } },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            GoogleMap(modifier = Modifier.fillMaxSize(), cameraPositionState = cameraPositionState, properties = MapProperties(isMyLocationEnabled = locationPermissionGranted), uiSettings = MapUiSettings(zoomControlsEnabled = true, myLocationButtonEnabled = locationPermissionGranted), onMapClick = { selectedReport = null }) {
                validReports.forEach { report ->
                    val desc = if (currentLang == "hi" && report.descriptionHi.isNotBlank()) report.descriptionHi else report.description
                    Marker(state = MarkerState(LatLng(report.latitude, report.longitude)), title = desc.take(60), snippet = report.locationAddress, icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN), onClick = { selectedReport = report; false })
                }
            }
            if (validReports.isEmpty()) {
                Surface(modifier = Modifier.align(Alignment.Center).padding(32.dp), shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f), shadowElevation = 8.dp) {
                    Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🗺️", style = MaterialTheme.typography.displaySmall)
                        Text(stringResource(R.string.no_pins), fontWeight = FontWeight.Bold)
                        Text(stringResource(R.string.submit_to_see_map), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
