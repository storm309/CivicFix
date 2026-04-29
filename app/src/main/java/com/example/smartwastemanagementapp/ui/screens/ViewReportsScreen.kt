package com.example.smartwastemanagementapp.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.smartwastemanagementapp.R
import com.example.smartwastemanagementapp.model.ReportModerationStatus
import com.example.smartwastemanagementapp.model.WasteReport
import com.example.smartwastemanagementapp.util.LanguageManager
import com.example.smartwastemanagementapp.viewmodel.WasteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewReportsScreen(onBack: () -> Unit, viewModel: WasteViewModel) {
    val reports by viewModel.reports.collectAsState()
    val isLoading by viewModel.isLoading
    var selectedFilter by remember { mutableStateOf("All") }

    LaunchedEffect(Unit) { viewModel.fetchReports() }

    val filteredReports = remember(reports, selectedFilter) {
        val sorted = reports.sortedByDescending { it.priority } // Sort by most supported first
        when (selectedFilter) {
            "Pending" -> sorted.filter { 
                ReportModerationStatus.from(it.moderationStatus) == ReportModerationStatus.PENDING_APPROVAL 
            }
            "Approved" -> sorted.filter { 
                ReportModerationStatus.from(it.moderationStatus) == ReportModerationStatus.APPROVED 
            }
            "Rejected" -> sorted.filter { 
                ReportModerationStatus.from(it.moderationStatus) == ReportModerationStatus.REJECTED 
            }
            else -> sorted
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Column { Text(stringResource(R.string.waste_reports), fontWeight = FontWeight.Bold); Text(stringResource(R.string.total_reports, reports.size.toString()), style = MaterialTheme.typography.bodySmall) } },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Row(modifier = Modifier.fillMaxWidth().padding(16.dp).horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("All", "Pending", "Approved", "Rejected").forEach { filter ->
                    val label = when(filter) {
                        "All" -> stringResource(R.string.filter_all)
                        "Pending" -> stringResource(R.string.filter_pending)
                        "Approved" -> stringResource(R.string.filter_approved)
                        "Rejected" -> stringResource(R.string.filter_rejected)
                        else -> filter
                    }
                    FilterChip(selected = selectedFilter == filter, onClick = { selectedFilter = filter }, label = { Text(label) })
                }
            }
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else if (filteredReports.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(stringResource(R.string.no_reports_yet)) }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    itemsIndexed(filteredReports) { _, report -> 
                        ReportCard(report, onUpvote = { viewModel.upvoteReport(report.id) }) 
                    }
                }
            }
        }
    }
}

@Composable
private fun ReportCard(report: WasteReport, onUpvote: () -> Unit) {
    val context = LocalContext.current
    val currentLang = LanguageManager.getSelectedLanguage(context)
    val displayDescription = if (currentLang == "hi" && report.descriptionHi.isNotBlank()) report.descriptionHi else report.description
    val currentUserId = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val hasUpvoted = report.upvotes.containsKey(currentUserId)

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), elevation = CardDefaults.cardElevation(2.dp)) {
        Column {
            if (report.imageUrl.isNotBlank()) {
                Image(painter = rememberAsyncImagePainter(report.imageUrl), contentDescription = null, modifier = Modifier.fillMaxWidth().height(180.dp), contentScale = ContentScale.Crop)
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = displayDescription.ifBlank { stringResource(R.string.no_reports_yet) }, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, null, modifier = Modifier.size(14.dp), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(4.dp))
                    Text(text = report.locationAddress, style = MaterialTheme.typography.labelSmall)
                }
                Spacer(Modifier.height(12.dp))
                
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    val statusLabel = when(ReportModerationStatus.from(report.moderationStatus)) {
                        ReportModerationStatus.PENDING_APPROVAL -> stringResource(R.string.filter_pending)
                        ReportModerationStatus.APPROVED -> stringResource(R.string.filter_approved)
                        ReportModerationStatus.REJECTED -> stringResource(R.string.filter_rejected)
                    }
                    Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(8.dp)) {
                        Text(text = statusLabel, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                    
                    // Support / Upvote Button
                    OutlinedButton(
                        onClick = onUpvote,
                        shape = RoundedCornerShape(12.dp),
                        colors = if (hasUpvoted) ButtonDefaults.outlinedButtonColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)) else ButtonDefaults.outlinedButtonColors()
                    ) {
                        Icon(if (hasUpvoted) Icons.Default.ThumbUp else Icons.Default.ThumbUpOffAlt, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(stringResource(R.string.support_issue, report.upvotes.size), style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}
