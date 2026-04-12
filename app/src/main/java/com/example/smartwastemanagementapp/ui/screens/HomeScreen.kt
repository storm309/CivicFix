package com.example.smartwastemanagementapp.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartwastemanagementapp.ui.theme.*
import com.example.smartwastemanagementapp.viewmodel.AuthViewModel
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onReportWaste: () -> Unit,
    onViewReports: () -> Unit,
    onViewMap:     () -> Unit,
    onLogout:      () -> Unit,
    authViewModel: AuthViewModel
) {
    val user        = authViewModel.userProfile.value
    val scrollState = rememberScrollState()

    var showLogoutDialog by remember { mutableStateOf(false) }

    // Animated entrance
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    val greeting = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when { hour < 12 -> "Good Morning" ; hour < 17 -> "Good Afternoon" ; else -> "Good Evening" }
    }
    val greetingEmoji = remember {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        when { hour < 12 -> "☀️" ; hour < 17 -> "🌤️" ; else -> "🌙" }
    }

    // Logout confirmation dialog
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            icon    = { Icon(Icons.AutoMirrored.Filled.ExitToApp, null, tint = MaterialTheme.colorScheme.error) },
            title   = { Text("Logout?", fontWeight = FontWeight.Bold) },
            text    = { Text("Are you sure you want to logout from CivicFix?") },
            confirmButton = {
                Button(
                    onClick = { authViewModel.logout(); onLogout() },
                    colors  = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Logout") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showLogoutDialog = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick           = onReportWaste,
                icon              = { Icon(Icons.Default.Add, null) },
                text              = { Text("Report Issue", fontWeight = FontWeight.Bold) },
                containerColor    = MaterialTheme.colorScheme.primary,
                contentColor      = Color.White,
                modifier          = Modifier.shadow(8.dp, RoundedCornerShape(16.dp))
            )
        }
    ) { scaffoldPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(scaffoldPadding)
                .verticalScroll(scrollState)
        ) {
            // ── Hero Banner ──────────────────────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(EcoGreen40, EcoGreen50, Teal40)))
            ) {
                // Decorative circles
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = 50.dp, y = (-50).dp)
                        .alpha(0.08f)
                        .background(Color.White, RoundedCornerShape(50))
                )

                Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 52.dp, bottom = 24.dp)) {
                    // Top row: greeting + logout
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text  = "$greeting $greetingEmoji",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.85f)
                            )
                            Text(
                                text  = user?.name?.ifBlank { "Citizen" } ?: "Citizen",
                                style = MaterialTheme.typography.headlineSmall.copy(
                                    fontWeight = FontWeight.ExtraBold
                                ),
                                color = Color.White
                            )
                        }
                        // Avatar + logout
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Avatar circle
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text  = user?.name?.firstOrNull()?.uppercase() ?: "U",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(Modifier.width(8.dp))
                            IconButton(onClick = { showLogoutDialog = true }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ExitToApp,
                                    contentDescription = "Logout",
                                    tint = Color.White.copy(alpha = 0.9f)
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // User info chips
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        InfoChip(icon = Icons.Default.Person, text = user?.gender ?: "N/A")
                        InfoChip(icon = Icons.Default.DateRange, text = "Age ${user?.age ?: "N/A"}")
                        InfoChip(icon = Icons.Default.Phone, text = user?.phoneNumber?.ifBlank { "N/A" } ?: "N/A")
                    }
                }
            }

            // ── Stats strip ──────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StatCard("Reports", "🗂️", EcoGreen40, modifier = Modifier.weight(1f), onClick = onViewReports)
                StatCard("Pending", "⏳", MaterialTheme.colorScheme.tertiary, modifier = Modifier.weight(1f), onClick = onViewReports)
                StatCard("Fixed", "✅", Teal40, modifier = Modifier.weight(1f), onClick = onViewReports)
            }

            Spacer(Modifier.height(24.dp))

            // ── Section title ─────────────────────────────────────
            Text(
                text     = "Quick Actions",
                style    = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 20.dp),
                color    = MaterialTheme.colorScheme.onBackground
            )

            Spacer(Modifier.height(14.dp))

            // ── Action cards (simple, no destructuring) ───────────
            AnimatedVisibility(
                visible = visible,
                enter   = fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 2 }
            ) {
                ActionCard(
                    title    = "Report Waste Issue",
                    subtitle = "Snap a photo & report waste problems near you",
                    icon     = Icons.Default.Add,
                    gradient = Brush.linearGradient(listOf(EcoGreen40, EcoGreen60)),
                    onClick  = onReportWaste,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
            Spacer(Modifier.height(14.dp))

            AnimatedVisibility(
                visible = visible,
                enter   = fadeIn(tween(300, delayMillis = 120)) +
                          slideInVertically(tween(300, delayMillis = 120)) { it / 2 }
            ) {
                ActionCard(
                    title    = "View All Reports",
                    subtitle = "Track status of submitted civic complaints",
                    icon     = Icons.AutoMirrored.Filled.List,
                    gradient = Brush.linearGradient(listOf(Teal40, Color(0xFF00BFA5))),
                    onClick  = onViewReports,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
            Spacer(Modifier.height(14.dp))

            AnimatedVisibility(
                visible = visible,
                enter   = fadeIn(tween(300, delayMillis = 240)) +
                          slideInVertically(tween(300, delayMillis = 240)) { it / 2 }
            ) {
                ActionCard(
                    title    = "Waste Map",
                    subtitle = "See waste hotspots & reported locations on map",
                    icon     = Icons.Default.Map,
                    gradient = Brush.linearGradient(listOf(Color(0xFF1565C0), Color(0xFF42A5F5))),
                    onClick  = onViewMap,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
            }
            Spacer(Modifier.height(14.dp))

            // ── Tips section ──────────────────────────────────────
            Spacer(Modifier.height(8.dp))
            Text(
                text     = "Civic Tips 💡",
                style    = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TipCard("Report early", "Issues resolved 3x faster", "⚡", Modifier.weight(1f))
                TipCard("Add photos", "AI auto-fills description", "📷", Modifier.weight(1f))
            }

            Spacer(Modifier.height(88.dp)) // FAB clearance
        }
    }
}

// ── Reusable sub-composables ─────────────────────────────────

@Composable
private fun InfoChip(icon: ImageVector, text: String) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = Color.White.copy(alpha = 0.18f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = Color.White, modifier = Modifier.size(13.dp))
            Spacer(Modifier.width(4.dp))
            Text(text, style = MaterialTheme.typography.labelSmall, color = Color.White)
        }
    }
}

@Composable
private fun StatCard(label: String, emoji: String, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        onClick   = onClick,
        modifier  = modifier,
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(emoji, fontSize = 20.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                text  = label,
                style = MaterialTheme.typography.labelMedium,
                color = color
            )
        }
    }
}

@Composable
private fun ActionCard(
    title: String, subtitle: String, icon: ImageVector,
    gradient: Brush, onClick: () -> Unit, modifier: Modifier = Modifier
) {
    Card(
        onClick   = onClick,
        modifier  = modifier.fillMaxWidth().height(100.dp),
        shape     = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
        ) {
            // Decorative circle
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .align(Alignment.CenterEnd)
                    .offset(x = 20.dp)
                    .alpha(0.15f)
                    .background(Color.White, CircleShape)
            )
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = Color.White, modifier = Modifier.size(28.dp))
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text  = title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Text(
                        text  = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f),
                        maxLines = 2
                    )
                }
                Icon(Icons.Default.ChevronRight, null, tint = Color.White.copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
private fun TipCard(title: String, subtitle: String, emoji: String, modifier: Modifier = Modifier) {
    Card(
        modifier  = modifier,
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(emoji, fontSize = 22.sp)
            Spacer(Modifier.height(6.dp))
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
