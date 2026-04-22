package com.example.smartwastemanagementapp.ui.screens

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import com.example.smartwastemanagementapp.viewmodel.WasteViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.io.File

@SuppressLint("MissingPermission")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportWasteScreen(
    onSuccess: () -> Unit,
    onBack: () -> Unit,
    viewModel: WasteViewModel
) {
    val context      = LocalContext.current
    val focusManager = LocalFocusManager.current

    var description   by remember { mutableStateOf("") }
    var imageUri      by remember { mutableStateOf<Uri?>(null) }
    var location      by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var locationError by remember { mutableStateOf(false) }

    val aiDescription by viewModel.aiDescription
    val moderationResult by viewModel.imageModeration
    val isAnalyzing   by viewModel.isAnalyzing
    val isLoading     by viewModel.isLoading
    val errorMsg      by viewModel.error

    // Auto-fill description from AI
    LaunchedEffect(aiDescription) {
        aiDescription?.let {
            description = it
            viewModel.clearAiDescription()
        }
    }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val tempUri = remember {
        val file = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    val cameraLauncher  = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) imageUri = tempUri
    }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { imageUri = it }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val locGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                         permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (locGranted) {
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener { loc ->
                    if (loc != null) {
                        location = loc.latitude to loc.longitude
                        locationError = false
                    } else {
                        locationError = true
                    }
                }
                .addOnFailureListener { locationError = true }
        } else {
            locationError = true
        }
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    // Helper: load bitmap for Gemini
    fun loadBitmap(uri: Uri): Bitmap? = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(
                ImageDecoder.createSource(context.contentResolver, uri)
            ) { decoder, _, _ -> decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE }
                .copy(Bitmap.Config.ARGB_8888, false)
        } else {
            @Suppress("DEPRECATION")
            android.provider.MediaStore.Images.Media
                .getBitmap(context.contentResolver, uri)
                ?.copy(Bitmap.Config.ARGB_8888, false)
        }
    } catch (_: Exception) { null }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Report Waste Issue", fontWeight = FontWeight.ExtraBold)
                        Text(
                            "Help keep your city clean",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
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
                .background(MaterialTheme.colorScheme.background)
                .systemBarsPadding()
                .imePadding()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(12.dp))

            // ── Step 1: Photo ─────────────────────────────────────
            SectionHeader("1", "Photo Evidence", "Optional – AI can analyse it")

            Spacer(Modifier.height(10.dp))

            Card(
                modifier  = Modifier.fillMaxWidth().height(220.dp),
                shape     = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                colors    = CardDefaults.cardColors(
                    containerColor = if (imageUri != null)
                        MaterialTheme.colorScheme.surface
                    else
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (imageUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(imageUri),
                            contentDescription = "Captured image",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        // Retake overlay
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 12.dp)
                                .background(
                                    Color.Black.copy(alpha = 0.45f),
                                    RoundedCornerShape(24.dp)
                                )
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            TextButton(onClick = { cameraLauncher.launch(tempUri) }) {
                                Icon(Icons.Default.CameraAlt, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Retake", color = Color.White, style = MaterialTheme.typography.labelLarge)
                            }
                            TextButton(onClick = { galleryLauncher.launch("image/*") }) {
                                Icon(Icons.Default.PhotoLibrary, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Change", color = Color.White, style = MaterialTheme.typography.labelLarge)
                            }
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(64.dp)
                                    .background(
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    Icons.Default.CameraAlt, null,
                                    modifier = Modifier.size(32.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(
                                "Add a photo of the issue",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedButton(
                                    onClick = { cameraLauncher.launch(tempUri) },
                                    shape   = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Camera")
                                }
                                OutlinedButton(
                                    onClick = { galleryLauncher.launch("image/*") },
                                    shape   = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.PhotoLibrary, null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("Gallery")
                                }
                            }
                        }
                    }
                }
            }

            // ── AI Analyse button ─────────────────────────────────
            AnimatedVisibility(
                visible = imageUri != null,
                enter   = fadeIn() + expandVertically()
            ) {
                Spacer(Modifier.height(10.dp))
                if (isAnalyzing) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape    = RoundedCornerShape(14.dp),
                        color    = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(10.dp))
                            Text("Gemini AI is analyzing the image…", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                } else {
                    Button(
                        onClick = {
                            imageUri?.let { uri ->
                                val bmp = loadBitmap(uri)
                                if (bmp != null) viewModel.analyzeWasteImage(bmp)
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape    = RoundedCornerShape(14.dp),
                        colors   = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor   = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    ) {
                        Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("✨ Analyze with Gemini AI", fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            moderationResult?.let { moderation ->
                Spacer(Modifier.height(10.dp))
                val isUnsafe = moderation.score < 0.60 || moderation.label.equals("unsafe", ignoreCase = true)
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    color = if (isUnsafe) {
                        MaterialTheme.colorScheme.errorContainer
                    } else {
                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                    }
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = if (isUnsafe) "AI safety check: blocked" else "AI safety check: passed",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isUnsafe) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Confidence ${(moderation.score * 100).toInt()}% • ${moderation.reason}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            // ── Step 2: Description ───────────────────────────────
            SectionHeader("2", "Describe the Issue", "What type of waste? How severe?")

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value       = description,
                onValueChange = { description = it },
                label       = { Text("What's the problem?") },
                placeholder = { Text("E.g. Large pile of garbage dumped near the park entrance…") },
                modifier    = Modifier.fillMaxWidth().height(130.dp),
                shape       = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction    = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedLabelColor    = MaterialTheme.colorScheme.primary
                )
            )

            // char count
            Text(
                text  = "${description.length} characters",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.align(Alignment.End).padding(top = 4.dp)
            )

            Spacer(Modifier.height(20.dp))

            // ── Step 3: Location ──────────────────────────────────
            SectionHeader("3", "Location", "Auto-detected from GPS")

            Spacer(Modifier.height(10.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(16.dp),
                color    = when {
                    locationError  -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                    location != null -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    else           -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val locIcon  = if (locationError) Icons.Default.LocationOff else Icons.Default.LocationOn
                    val locColor = when {
                        locationError   -> MaterialTheme.colorScheme.error
                        location != null -> MaterialTheme.colorScheme.primary
                        else            -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    Icon(locIcon, null, tint = locColor, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = when {
                                locationError   -> "Location unavailable"
                                location != null -> "Location captured ✓"
                                else            -> "Fetching GPS location…"
                            },
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = locColor
                        )
                        if (location != null) {
                            Text(
                                text  = "Lat: ${"%.5f".format(location!!.first)}, Lng: ${"%.5f".format(location!!.second)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else if (!locationError) {
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp).height(2.dp),
                                color    = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // Retry button if error
                    if (locationError) {
                        Spacer(Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                locationError = false
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        ) {
                            Icon(Icons.Default.Refresh, "Retry location", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            // Error message
            errorMsg?.let { err ->
                Spacer(Modifier.height(10.dp))
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape    = RoundedCornerShape(12.dp),
                    color    = MaterialTheme.colorScheme.errorContainer
                ) {
                    Row(modifier = Modifier.padding(12.dp, 10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Error, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(err, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // ── Submit ────────────────────────────────────────────
            if (isLoading) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.height(8.dp))
                    Text("Submitting your report…", style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                Button(
                    onClick = {
                        location?.let { loc ->
                            viewModel.submitReport(
                                description = description,
                                imageUri    = imageUri,
                                latitude    = loc.first,
                                longitude   = loc.second,
                                onSuccess   = onSuccess
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape    = RoundedCornerShape(16.dp),
                    enabled  = description.isNotBlank() && location != null && !locationError,
                    colors   = ButtonDefaults.buttonColors(
                        containerColor         = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Icon(Icons.Default.Send, null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Submit Report", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                }

                if (location == null && !locationError) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "⏳ Waiting for GPS location before submitting…",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionHeader(number: String, title: String, subtitle: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(number, color = Color.White, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(10.dp))
        Column {
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
