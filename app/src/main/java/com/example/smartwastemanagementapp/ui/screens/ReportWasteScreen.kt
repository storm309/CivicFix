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
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import coil.compose.rememberAsyncImagePainter
import com.example.smartwastemanagementapp.R
import com.example.smartwastemanagementapp.ui.theme.EcoGreen40
import com.example.smartwastemanagementapp.viewmodel.WasteViewModel
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.maps.android.compose.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    var manualAddress by remember { mutableStateOf("") }
    var imageUri      by remember { mutableStateOf<Uri?>(null) }
    var location      by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var locationError by remember { mutableStateOf(false) }
    var locationAccuracy by remember { mutableStateOf<Float?>(null) }
    var isRefreshingLocation by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    fun isUserAuthenticated(): Boolean {
        return com.google.firebase.auth.FirebaseAuth.getInstance().currentUser != null
    }

    val aiDescription by viewModel.aiDescription
    val aiDescriptionHi by viewModel.aiDescriptionHi
    val moderationResult by viewModel.imageModeration
    val isAnalyzing   by viewModel.isAnalyzing
    val isLoading     by viewModel.isLoading
    val errorMsg      by viewModel.error

    LaunchedEffect(aiDescription, aiDescriptionHi) {
        val currentLang = com.example.smartwastemanagementapp.util.LanguageManager.getSelectedLanguage(context)
        if (currentLang == "hi" && aiDescriptionHi != null) {
            description = aiDescriptionHi!!
        } else if (aiDescription != null) {
            description = aiDescription!!
        }
    }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    fun fetchAddress(lat: Double, lng: Double) {
        try {
            val geocoder = android.location.Geocoder(context, java.util.Locale.getDefault())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                geocoder.getFromLocation(lat, lng, 1) { addresses ->
                    if (addresses.isNotEmpty()) {
                        val addr = addresses[0]
                        manualAddress = "${addr.getAddressLine(0)}, ${addr.locality ?: ""}".trim().trimEnd(',')
                    }
                }
            } else {
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(lat, lng, 1)
                if (!addresses.isNullOrEmpty()) {
                    val addr = addresses[0]
                    manualAddress = "${addr.getAddressLine(0)}, ${addr.locality ?: ""}".trim().trimEnd(',')
                }
            }
        } catch (e: Exception) { }
    }

    fun refreshLocation(onDone: (Boolean, Pair<Double, Double>?) -> Unit = { _, _ -> }) {
        isRefreshingLocation = true
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
            .addOnSuccessListener { loc ->
                isRefreshingLocation = false
                if (loc != null) {
                    val latLng = loc.latitude to loc.longitude
                    location = latLng
                    locationAccuracy = if (loc.hasAccuracy()) loc.accuracy else null
                    locationError = false
                    fetchAddress(loc.latitude, loc.longitude)
                    onDone(true, latLng)
                } else {
                    locationError = true
                    onDone(false, null)
                }
            }
            .addOnFailureListener {
                isRefreshingLocation = false
                locationError = true
                onDone(false, null)
            }
    }

    val tempUri = remember {
        val file = File(context.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg")
        FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    }

    val cameraLauncher  = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            imageUri = tempUri
            refreshLocation()
        }
    }
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { imageUri = it }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val locGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                         permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (locGranted) refreshLocation() else locationError = true
    }

    LaunchedEffect(Unit) {
        permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION))
    }

    fun loadBitmap(uri: Uri): Bitmap? = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri)) { decoder, _, _ -> decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE }
                .copy(Bitmap.Config.ARGB_8888, false)
        } else {
            @Suppress("DEPRECATION")
            android.provider.MediaStore.Images.Media.getBitmap(context.contentResolver, uri)?.copy(Bitmap.Config.ARGB_8888, false)
        }
    } catch (_: Exception) { null }

    if (showSuccessDialog) {
        AlertDialog(
            onDismissRequest = { showSuccessDialog = false; onSuccess() },
            title = { 
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, null, tint = EcoGreen40, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.report_submitted_title)) 
                }
            },
            text = {
                Column {
                    Text(stringResource(R.string.report_submitted_msg))
                    Spacer(Modifier.height(12.dp))
                    Surface(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(stringResource(R.string.pending_approval_status), color = MaterialTheme.colorScheme.tertiary, fontWeight = FontWeight.ExtraBold)
                            Spacer(Modifier.height(4.dp))
                            Text(stringResource(R.string.status_note), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showSuccessDialog = false; onSuccess() }, shape = RoundedCornerShape(12.dp)) {
                    Text(stringResource(R.string.ok))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(stringResource(R.string.report_waste_title), fontWeight = FontWeight.ExtraBold)
                        Text(stringResource(R.string.app_tagline), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
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
            SectionHeader("1", stringResource(R.string.step_photo_title), stringResource(R.string.step_photo_desc))
            Spacer(Modifier.height(10.dp))

            Card(
                modifier  = Modifier.fillMaxWidth().height(220.dp),
                shape     = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                colors    = CardDefaults.cardColors(containerColor = if (imageUri != null) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    if (imageUri != null) {
                        Image(painter = rememberAsyncImagePainter(imageUri), contentDescription = "Captured image", modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                        Row(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp).background(Color.Black.copy(alpha = 0.45f), RoundedCornerShape(24.dp)).padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            TextButton(onClick = { cameraLauncher.launch(tempUri) }) {
                                Icon(Icons.Default.CameraAlt, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(stringResource(R.string.retake_photo), color = Color.White)
                            }
                            TextButton(onClick = { galleryLauncher.launch("image/*") }) {
                                Icon(Icons.Default.PhotoLibrary, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(stringResource(R.string.change_photo), color = Color.White)
                            }
                        }
                    } else {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            Box(modifier = Modifier.size(64.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(32.dp), tint = MaterialTheme.colorScheme.primary)
                            }
                            Text(stringResource(R.string.step_photo_desc), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                OutlinedButton(onClick = { cameraLauncher.launch(tempUri) }, shape = RoundedCornerShape(12.dp)) {
                                    Icon(Icons.Default.CameraAlt, null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(stringResource(R.string.camera))
                                }
                                OutlinedButton(onClick = { galleryLauncher.launch("image/*") }, shape = RoundedCornerShape(12.dp)) {
                                    Icon(Icons.Default.PhotoLibrary, null, modifier = Modifier.size(16.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text(stringResource(R.string.gallery))
                                }
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(visible = imageUri != null, enter = fadeIn() + expandVertically()) {
                Spacer(Modifier.height(10.dp))
                if (isAnalyzing) {
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(modifier = Modifier.size(32.dp))
                        Spacer(Modifier.height(10.dp))
                        Text(stringResource(R.string.ai_analyzing), color = MaterialTheme.colorScheme.primary)
                        Text(stringResource(R.string.ai_detecting), style = MaterialTheme.typography.labelSmall)
                    }
                } else {
                    Button(onClick = { imageUri?.let { uri -> loadBitmap(uri)?.let { viewModel.analyzeWasteImage(it) } } }, modifier = Modifier.fillMaxWidth().height(50.dp), shape = RoundedCornerShape(14.dp), colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)) {
                        Icon(Icons.Default.AutoAwesome, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(stringResource(R.string.analyze_ai), fontWeight = FontWeight.SemiBold)
                    }
                }
            }

            if (aiDescription != null && !isAnalyzing) {
                Spacer(Modifier.height(10.dp))
                Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp), color = EcoGreen40.copy(alpha = 0.12f), border = BorderStroke(1.dp, EcoGreen40.copy(alpha = 0.3f))) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, null, tint = EcoGreen40, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(10.dp))
                        Text(stringResource(R.string.ai_auto_filled), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            SectionHeader("2", stringResource(R.string.step_desc_title), stringResource(R.string.step_desc_desc))
            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(stringResource(R.string.whats_the_issue)) },
                placeholder = { Text(stringResource(R.string.describe_waste_hint)) },
                modifier = Modifier.fillMaxWidth().height(130.dp),
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
            )

            Spacer(Modifier.height(20.dp))
            SectionHeader("3", stringResource(R.string.step_loc_title), stringResource(R.string.step_loc_desc))
            Spacer(Modifier.height(10.dp))

            Card(modifier = Modifier.fillMaxWidth().height(200.dp), shape = RoundedCornerShape(16.dp)) {
                Box {
                    val cameraPositionState = rememberCameraPositionState { position = com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(com.google.android.gms.maps.model.LatLng(location?.first ?: 20.5937, location?.second ?: 78.9629), if (location != null) 15f else 5f) }
                    var initialLocationSet by remember { mutableStateOf(false) }
                    LaunchedEffect(location) { if (location != null && !initialLocationSet) { cameraPositionState.animate(com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(com.google.android.gms.maps.model.LatLng(location!!.first, location!!.second), 15f)); initialLocationSet = true } }
                    GoogleMap(modifier = Modifier.fillMaxSize(), cameraPositionState = cameraPositionState, onMapClick = { latLng -> location = latLng.latitude to latLng.longitude; fetchAddress(latLng.latitude, latLng.longitude) }, uiSettings = MapUiSettings(zoomControlsEnabled = false, myLocationButtonEnabled = false)) {
                        location?.let { Marker(state = MarkerState(com.google.android.gms.maps.model.LatLng(it.first, it.second)), title = "Waste Location") }
                    }
                    Surface(modifier = Modifier.align(Alignment.TopEnd).padding(8.dp), shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)) {
                        Text(stringResource(R.string.tap_map_hint), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            OutlinedTextField(value = manualAddress, onValueChange = { manualAddress = it }, label = { Text(stringResource(R.string.manual_address)) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))

            Spacer(Modifier.height(12.dp))
            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), color = if (locationError) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f) else if (location != null) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(if (locationError) Icons.Default.LocationOff else Icons.Default.LocationOn, null, tint = if (locationError) MaterialTheme.colorScheme.error else if (location != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = if (locationError) stringResource(R.string.location_unavailable) else if (location != null) stringResource(R.string.location_captured) else stringResource(R.string.fetching_location), fontWeight = FontWeight.SemiBold)
                        if (location != null) Text(text = "Lat: ${"%.6f".format(location!!.first)}, Lng: ${"%.6f".format(location!!.second)}", style = MaterialTheme.typography.bodySmall)
                    }
                    IconButton(onClick = { refreshLocation() }) { Icon(Icons.Default.Refresh, null) }
                }
            }

            Spacer(Modifier.height(28.dp))
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = {
                        if (!isUserAuthenticated()) return@Button
                        location?.let { loc ->
                            viewModel.submitReport(context, description, aiDescriptionHi ?: "", manualAddress, imageUri, loc.first, loc.second, locationAccuracy ?: 0f) { showSuccessDialog = true }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    enabled = description.isNotBlank() && location != null && isUserAuthenticated()
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, null)
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.submit_report), fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun SectionHeader(number: String, title: String, subtitle: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(28.dp).background(MaterialTheme.colorScheme.primary, CircleShape), contentAlignment = Alignment.Center) { Text(number, color = Color.White, fontWeight = FontWeight.Bold) }
        Spacer(Modifier.width(10.dp))
        Column { Text(title, fontWeight = FontWeight.Bold); Text(subtitle, style = MaterialTheme.typography.bodySmall) }
    }
}
