package com.example.smartwastemanagementapp.ui.screens

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.smartwastemanagementapp.model.ReportModerationStatus
import com.example.smartwastemanagementapp.model.WasteReport
import com.example.smartwastemanagementapp.viewmodel.WasteViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
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
    val validReports = reports.filter {
        (it.latitude != 0.0 || it.longitude != 0.0) &&
            ReportModerationStatus.from(it.moderationStatus) == ReportModerationStatus.APPROVED
    }

    var locationPermissionGranted by remember { mutableStateOf(false) }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { locationPermissionGranted = it }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    val defaultLatLng = LatLng(20.5937, 78.9629)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLatLng, 5f)
    }

    var selectedReport by remember { mutableStateOf<WasteReport?>(null) }

    LaunchedEffect(validReports) {
        val first = validReports.firstOrNull()
        if (first != null) {
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(LatLng(first.latitude, first.longitude), 12f)
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Waste Map", fontWeight = FontWeight.ExtraBold)
                        Text(
                            "${validReports.size} approved locations pinned",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = locationPermissionGranted),
                uiSettings = MapUiSettings(
                    zoomControlsEnabled    = true,
                    myLocationButtonEnabled = locationPermissionGranted,
                    mapToolbarEnabled      = true
                ),
                onMapClick = { selectedReport = null }
            ) {
                validReports.forEach { report ->
                    Marker(
                        state   = MarkerState(LatLng(report.latitude, report.longitude)),
                        title   = report.description.take(60).ifBlank { "Waste Report" },
                        snippet = report.locationAddress.ifBlank { "Approved" },
                        icon    = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN),
                        onClick = {
                            selectedReport = report
                            false // let the default behavior happen (center camera, show info window)
                        }
                    )
                }
            }

            // Report Details Sheet/Card
            AnimatedVisibility(
                visible = selectedReport != null,
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it },
                modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
            ) {
                selectedReport?.let { report ->
                    Card(
                        modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                        shape = RoundedCornerShape(20.dp),
                        elevation = CardDefaults.cardElevation(8.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.LocationOn, null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = report.locationAddress.ifBlank { "Unknown Address" },
                                    style = MaterialTheme.typography.titleSmall,
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                                Spacer(Modifier.weight(1f))
                                IconButton(onClick = { selectedReport = null }) {
                                    Icon(Icons.Default.Close, null)
                                }
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = report.description,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 3,
                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                            )
                            if (report.imageUrl.isNotBlank()) {
                                Spacer(Modifier.height(12.dp))
                                Card(shape = RoundedCornerShape(12.dp)) {
                                    coil.compose.AsyncImage(
                                        model = report.imageUrl,
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxWidth().height(120.dp),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AccessTime, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "Reported on ${java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault()).format(java.util.Date(report.timestamp))}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Legend overlay at bottom (only show if no report is selected)
            if (validReports.isNotEmpty() && selectedReport == null) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .padding(bottom = 12.dp),
                    shape  = RoundedCornerShape(20.dp),
                    color  = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    shadowElevation = 8.dp
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Map, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        LegendDot(Color(0xFF2E7D32), "Approved (${validReports.size})")
                    }
                }
            }

            // Empty state
            if (validReports.isEmpty()) {
                Surface(
                    modifier = Modifier.align(Alignment.Center).padding(32.dp),
                    shape  = RoundedCornerShape(20.dp),
                    color  = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    shadowElevation = 8.dp
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🗺️", style = MaterialTheme.typography.displaySmall)
                        Spacer(Modifier.height(8.dp))
                        Text("No pins yet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Submit reports to see them on the map", style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Icon(
            Icons.Default.LocationOn, null,
            tint     = color,
            modifier = Modifier.size(16.dp)
        )
        Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium)
    }
}
