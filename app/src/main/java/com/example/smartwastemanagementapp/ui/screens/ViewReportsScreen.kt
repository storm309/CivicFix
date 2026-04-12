package com.example.smartwastemanagementapp.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.smartwastemanagementapp.model.WasteReport
import com.example.smartwastemanagementapp.ui.theme.*
import com.example.smartwastemanagementapp.viewmodel.WasteViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewReportsScreen(
    onBack: () -> Unit,
    viewModel: WasteViewModel
) {
    val reports   by viewModel.reports.collectAsState()
    val isLoading by viewModel.isLoading

    var selectedFilter by remember { mutableStateOf("All") }
    val filters = listOf("All", "Pending", "Cleaned")

    val filtered = remember(reports, selectedFilter) {
        when (selectedFilter) {
            "Pending" -> reports.filter { it.status.equals("Pending", ignoreCase = true) }
            "Cleaned" -> reports.filter { it.status.equals("Cleaned", ignoreCase = true) }
            else      -> reports
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Waste Reports", fontWeight = FontWeight.ExtraBold)
                        Text(
                            "${reports.size} total reports",
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
                actions = {
                    IconButton(onClick = { viewModel.fetchReports() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // ── Filter chips ─────────────────────────────────────
            LazyRow(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filters) { filter ->
                    val isSelected = selectedFilter == filter
                    val chipColor  = when (filter) {
                        "Pending" -> StatusPending
                        "Cleaned" -> StatusCleaned
                        else      -> MaterialTheme.colorScheme.primary
                    }
                    FilterChip(
                        selected    = isSelected,
                        onClick     = { selectedFilter = filter },
                        label       = {
                            Text(
                                text = if (filter == "All") "All (${reports.size})"
                                       else "$filter (${reports.count { it.status.equals(filter, ignoreCase = true) }})",
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        leadingIcon = if (isSelected) {
                            { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor   = chipColor.copy(alpha = 0.15f),
                            selectedLabelColor       = chipColor,
                            selectedLeadingIconColor = chipColor
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled          = true,
                            selected         = isSelected,
                            selectedBorderColor = chipColor,
                            borderColor      = MaterialTheme.colorScheme.outlineVariant
                        )
                    )
                }
            }

            // ── Content ───────────────────────────────────────────
            when {
                isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(12.dp))
                            Text("Loading reports…", style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }

                filtered.isEmpty() -> EmptyState(selectedFilter)

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(filtered) { index, report ->
                            ReportCard(report = report, index = index)
                        }
                        item { Spacer(Modifier.height(16.dp)) }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(filter: String) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            val emoji = if (filter == "Cleaned") "✅" else "📭"
            Text(emoji, fontSize = 64.sp)
            Spacer(Modifier.height(16.dp))
            Text(
                text = if (filter == "All") "No Reports Yet"
                       else "No $filter Reports",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text  = if (filter == "All")
                            "Be the first to report a waste issue in your area!"
                        else
                            "No reports with '$filter' status found.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
fun ReportCard(report: WasteReport, index: Int = 0) {
    val isPending = report.status.equals("Pending", ignoreCase = true)
    val statusColor     = if (isPending) StatusPending else StatusCleaned
    val statusContainer = if (isPending) StatusPendingContainer else StatusCleanedContainer
    val statusIcon      = if (isPending) Icons.Default.HourglassEmpty else Icons.Default.CheckCircle

    val dateStr = remember(report.timestamp) {
        SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())
            .format(Date(report.timestamp))
    }

    // Staggered entrance animation
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(index * 60L)
        visible = true
    }

    AnimatedVisibility(
        visible = visible,
        enter   = fadeIn(tween(250)) + slideInVertically(tween(250)) { it / 3 }
    ) {
        Card(
            modifier  = Modifier.fillMaxWidth(),
            shape     = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column {
                // Image or placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    if (report.imageUrl.isNotBlank()) {
                        Image(
                            painter            = rememberAsyncImagePainter(report.imageUrl),
                            contentDescription = "Waste image",
                            modifier           = Modifier.fillMaxSize(),
                            contentScale       = ContentScale.Crop
                        )
                    } else {
                        // Placeholder
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            MaterialTheme.colorScheme.surfaceVariant,
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.CameraAlt, null,
                                    modifier = Modifier.size(36.dp),
                                    tint     = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                )
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    "No photo attached",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                                )
                            }
                        }
                    }

                    // Status badge overlaid on image
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(10.dp),
                        shape = RoundedCornerShape(20.dp),
                        color = statusContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(statusIcon, null, tint = statusColor, modifier = Modifier.size(14.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text  = report.status,
                                style = MaterialTheme.typography.labelSmall,
                                color = statusColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                // Card content
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text     = report.description.ifBlank { "No description provided" },
                        style    = MaterialTheme.typography.titleSmall,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )

                    Spacer(Modifier.height(10.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(Modifier.height(10.dp))

                    // Meta row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Location
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.LocationOn, null,
                                tint     = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text  = "${"%.4f".format(report.latitude)}, ${"%.4f".format(report.longitude)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Timestamp
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.AccessTime, null,
                                tint     = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text  = dateStr,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
