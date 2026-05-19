package com.example.smartwastemanagementapp.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.smartwastemanagementapp.R
import com.example.smartwastemanagementapp.ui.components.ReportImage
import com.example.smartwastemanagementapp.ui.theme.*
import com.example.smartwastemanagementapp.viewmodel.WasteViewModel
import com.example.smartwastemanagementapp.util.LanguageManager

import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(
    onLogout: () -> Unit,
    viewModel: WasteViewModel,
    authViewModel: com.example.smartwastemanagementapp.viewmodel.AuthViewModel
) {
    val pending by viewModel.pendingReports.collectAsState()
    val reports by viewModel.reports.collectAsState()
    val isLoading by viewModel.isLoading
    val userProfile by authViewModel.userProfile
    val context = LocalContext.current
    var showExitDialog by remember { mutableStateOf(false) }

    BackHandler {
        showExitDialog = true
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Exit admin panel?") },
            text = { Text("You are in the admin console. Do you want to logout?") },
            confirmButton = {
                Button(onClick = {
                    showExitDialog = false
                    // Logout from Firebase
                    authViewModel.logout()
                    
                    // Also logout and revoke access from Google
                    val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
                    val googleSignInClient = GoogleSignIn.getClient(context, gso)
                    googleSignInClient.signOut().addOnCompleteListener {
                        googleSignInClient.revokeAccess().addOnCompleteListener {
                            onLogout()
                        }
                    }
                }) { Text("Logout") }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) { Text("Stay") }
            }
        )
    }

    val approvedCount = remember(reports) { 
        reports.count { com.example.smartwastemanagementapp.model.ReportModerationStatus.from(it.moderationStatus) == com.example.smartwastemanagementapp.model.ReportModerationStatus.APPROVED } 
    }
    val rejectedCount = remember(reports) { 
        reports.count { com.example.smartwastemanagementapp.model.ReportModerationStatus.from(it.moderationStatus) == com.example.smartwastemanagementapp.model.ReportModerationStatus.REJECTED } 
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(R.string.admin_console), fontWeight = FontWeight.ExtraBold)
                        Text(
                            text = "Moderator: ${userProfile?.name ?: "Admin"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                actions = {
                    val currentLang = LanguageManager.getSelectedLanguage(context)
                    IconButton(onClick = { LanguageManager.setLanguage(context, if (currentLang == "en") "hi" else "en") }) {
                        Icon(Icons.Default.Language, null, tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = { viewModel.fetchPendingReports() }) { Icon(Icons.Default.Refresh, null) }
                    IconButton(onClick = {
                        // Logout from Firebase
                        authViewModel.logout()
                        
                        // Also logout and revoke access from Google
                        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build()
                        val googleSignInClient = GoogleSignIn.getClient(context, gso)
                        googleSignInClient.signOut().addOnCompleteListener {
                            googleSignInClient.revokeAccess().addOnCompleteListener {
                                onLogout()
                            }
                        }
                    }) { Icon(Icons.AutoMirrored.Filled.ExitToApp, null, tint = MaterialTheme.colorScheme.error) }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(padding)) {
            Card(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(46.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Shield, null, tint = Color.White)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Admin Control Center", fontWeight = FontWeight.Bold)
                        Text("Review & approve citizen reports", style = MaterialTheme.typography.bodySmall)
                    }
                    OutlinedButton(onClick = { viewModel.fetchPendingReports() }) {
                        Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Sync")
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                AdminStatCard(stringResource(R.string.filter_pending), pending.size.toString(), Icons.Default.HourglassEmpty, MaterialTheme.colorScheme.tertiary, Modifier.weight(1f))
                AdminStatCard(stringResource(R.string.filter_approved), approvedCount.toString(), Icons.Default.CheckCircle, EcoGreen40, Modifier.weight(1f))
                AdminStatCard(stringResource(R.string.filter_rejected), rejectedCount.toString(), Icons.Default.Cancel, MaterialTheme.colorScheme.error, Modifier.weight(1f))
            }

            Card(modifier = Modifier.fillMaxWidth().padding(16.dp), shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f))) {
                Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(52.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary), contentAlignment = Alignment.Center) { Icon(Icons.Default.Dashboard, null, tint = Color.White) }
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(if (pending.isEmpty()) stringResource(R.string.all_clear) else stringResource(R.string.action_required), fontWeight = FontWeight.Bold)
                        Text(stringResource(R.string.reports_pending_review, pending.size.toString()), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Pending Review Queue", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }

            if (isLoading && pending.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
            } else if (pending.isEmpty()) {
                Column(modifier = Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    Icon(Icons.AutoMirrored.Filled.FactCheck, null, modifier = Modifier.size(80.dp).alpha(0.2f), tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(16.dp))
                    Text(stringResource(R.string.all_clear), style = MaterialTheme.typography.titleMedium)
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(pending, key = { it.id }) { report ->
                        ReportAdminCard(report = report, onApprove = { viewModel.approveReport(report.id) }, onReject = { viewModel.rejectReport(report.id) })
                    }
                }
            }
        }
    }
}

@Composable
fun ReportAdminCard(report: com.example.smartwastemanagementapp.model.WasteReport, onApprove: () -> Unit, onReject: () -> Unit) {
    val context = LocalContext.current
    val currentLang = LanguageManager.getSelectedLanguage(context)
    val displayDescription = if (currentLang == "hi" && report.descriptionHi.isNotBlank()) report.descriptionHi else report.description

    Card(shape = RoundedCornerShape(24.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(2.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f), modifier = Modifier.size(40.dp)) { Icon(Icons.Default.LocationOn, null, modifier = Modifier.padding(8.dp), tint = MaterialTheme.colorScheme.primary) }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(stringResource(R.string.address_label, report.locationAddress.ifBlank { "%.4f, %.4f".format(report.latitude, report.longitude) }), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                    if (report.priority > 0) {
                        Surface(color = MaterialTheme.colorScheme.errorContainer, shape = RoundedCornerShape(8.dp), modifier = Modifier.padding(top = 4.dp)) {
                            Text(stringResource(R.string.support_issue, report.priority), modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onErrorContainer, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
            Spacer(Modifier.height(12.dp))

            // Photo of the report - ensured to be shown
            if (report.imageUrl.isNotBlank()) {
                Card(modifier = Modifier.fillMaxWidth().height(220.dp), shape = RoundedCornerShape(16.dp), elevation = CardDefaults.cardElevation(4.dp)) {
                    ReportImage(
                        imageUrl = report.imageUrl,
                        contentDescription = stringResource(R.string.step_photo_title),
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(Modifier.height(12.dp))
            } else {
                Box(modifier = Modifier.fillMaxWidth().height(120.dp).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.ImageNotSupported, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(32.dp))
                        Spacer(Modifier.height(4.dp))
                        Text(stringResource(R.string.no_reports_yet), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            Text(displayDescription.ifBlank { stringResource(R.string.no_reports_yet) }, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(onClick = onApprove, modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = EcoGreen40)) {
                    Icon(Icons.Default.Check, null); Spacer(Modifier.width(8.dp)); Text(stringResource(R.string.approve))
                }
                OutlinedButton(onClick = onReject, modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                    Icon(Icons.Default.Close, null); Spacer(Modifier.width(8.dp)); Text(stringResource(R.string.reject))
                }
            }
        }
    }
}

@Composable
private fun AdminStatCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = color, modifier = Modifier.size(20.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
            Text(label, style = MaterialTheme.typography.labelSmall, color = color.copy(alpha = 0.8f))
        }
    }
}
