package com.example.smartwastemanagementapp.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartwastemanagementapp.R
import com.example.smartwastemanagementapp.ui.theme.*
import com.example.smartwastemanagementapp.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToSignup: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    var isOtpMode by remember { mutableStateOf(false) }
    var email     by remember { mutableStateOf("") }
    var password  by remember { mutableStateOf("") }
    var phone     by remember { mutableStateOf("") }
    var otpCode   by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val activity = context as? android.app.Activity
    val scope = rememberCoroutineScope()

    var passwordVisible by remember { mutableStateOf(false) }
    val scrollState     = rememberScrollState()
    var isGoogleLoading by remember { mutableStateOf(false) }
    var googleError by remember { mutableStateOf<String?>(null) }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != android.app.Activity.RESULT_OK) {
            isGoogleLoading = false
            googleError = "Sign-in cancelled - Please select a Google account"
            return@rememberLauncherForActivityResult
        }

        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken.orEmpty()
            if (idToken.isBlank()) {
                isGoogleLoading = false
                googleError = "Google token not received. Check Firebase Web Client ID setup."
            } else {
                viewModel.signInWithGoogleToken(idToken) {
                    isGoogleLoading = false
                    onLoginSuccess()
                }
            }
        } catch (e: ApiException) {
            isGoogleLoading = false
            googleError = when (e.statusCode) {
                7 -> "Network error - Check WiFi/mobile data"
                10 -> "Developer error - SHA-1 or Web Client ID mismatch"
                12500 -> "Google Sign-In setup issue in Firebase Console"
                12501 -> "Sign-in cancelled"
                else -> "Google Sign-In failed (code ${e.statusCode})"
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Gradient header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.38f)
                .background(Brush.verticalGradient(listOf(EcoGreen40, EcoGreen50, Teal40)))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .systemBarsPadding()
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(48.dp))

            Surface(
                modifier = Modifier.size(80.dp),
                shape = RoundedCornerShape(20.dp),
                shadowElevation = 12.dp
            ) {
                Image(
                    painter = painterResource(R.drawable.app_logo),
                    contentDescription = null,
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(Modifier.height(12.dp))

            Text("CivicFix", style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold)
            Text("Login to your account", color = Color.White.copy(0.8f))

            Spacer(Modifier.height(32.dp))

            Card(
                modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    // Mode Switcher
                    TabRow(
                        selectedTabIndex = if (isOtpMode) 1 else 0,
                        containerColor = Color.Transparent,
                        contentColor = MaterialTheme.colorScheme.primary,
                        divider = {},
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[if (isOtpMode) 1 else 0]),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    ) {
                        Tab(selected = !isOtpMode, onClick = { isOtpMode = false }) {
                            Text("Email", modifier = Modifier.padding(vertical = 12.dp))
                        }
                        Tab(selected = isOtpMode, onClick = { isOtpMode = true }) {
                            Text("Phone / OTP", modifier = Modifier.padding(vertical = 12.dp))
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    AnimatedContent(targetState = isOtpMode, label = "mode") { mode ->
                        if (!mode) {
                            // Email Mode
                            Column {
                                OutlinedTextField(
                                    value = email,
                                    onValueChange = { email = it },
                                    label = { Text("Email") },
                                    leadingIcon = { Icon(Icons.Default.Email, null) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                Spacer(Modifier.height(12.dp))
                                OutlinedTextField(
                                    value = password,
                                    onValueChange = { password = it },
                                    label = { Text("Password") },
                                    leadingIcon = { Icon(Icons.Default.Lock, null) },
                                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    trailingIcon = {
                                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                            Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null)
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp)
                                )
                            }
                        } else {
                            // OTP Mode
                            Column {
                                if (!isOtpSent) {
                                    OutlinedTextField(
                                        value = phone,
                                        onValueChange = { if (it.length <= 10) phone = it },
                                        label = { Text("Phone Number") },
                                        prefix = { Text("+91 ") },
                                        leadingIcon = { Icon(Icons.Default.Phone, null) },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(14.dp),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                                    )
                                } else {
                                    Text("OTP sent to +91 $phone", style = MaterialTheme.typography.labelMedium)
                                    Spacer(Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = otpCode,
                                        onValueChange = { if (it.length <= 6) otpCode = it },
                                        label = { Text("Enter 6-digit OTP") },
                                        leadingIcon = { Icon(Icons.Default.VpnKey, null) },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(14.dp),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                    )
                                    TextButton(onClick = { isOtpSent = false }, modifier = Modifier.align(Alignment.End)) {
                                        Text("Change Number")
                                    }
                                }
                            }
                        }
                    }

                    viewModel.error.value?.let { err ->
                        Text(err, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
                    }

                    Spacer(Modifier.height(24.dp))

                    if (viewModel.isLoading.value) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    } else {
                        Button(
                            onClick = {
                                if (!isOtpMode) {
                                    viewModel.login(email, password, onLoginSuccess)
                                } else {
                                    if (!isOtpSent) {
                                        if (activity != null) {
                                            viewModel.startOtp(phone, activity)
                                            isOtpSent = true
                                        }
                                    } else {
                                        viewModel.verifyOtp(otpCode)
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(54.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(if (!isOtpMode) "Login" else if (!isOtpSent) "Send OTP" else "Verify OTP")
                        }

                        // OTP flow has no callback, so navigate after auth only in OTP mode.
                        LaunchedEffect(viewModel.isLoggedIn.value, isOtpMode) {
                            if (isOtpMode && viewModel.isLoggedIn.value) {
                                onLoginSuccess()
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(Modifier.height(16.dp))

                    LaunchedEffect(viewModel.error.value) {
                        if (isGoogleLoading && viewModel.error.value != null) {
                            googleError = viewModel.error.value
                            isGoogleLoading = false
                        }
                    }

                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Gmail Sign-In Button
                        Button(
                            onClick = {
                                // Check internet first
                                if (!isInternetAvailable(context)) {
                                    googleError = "❌ No internet connection found. Turn on WiFi or mobile data."
                                    return@Button
                                }

                                val playServicesStatus = GoogleApiAvailability.getInstance()
                                    .isGooglePlayServicesAvailable(context)
                                if (playServicesStatus != ConnectionResult.SUCCESS) {
                                    googleError = "Google Play Services unavailable. Update Play Services and retry."
                                    if (activity != null && GoogleApiAvailability.getInstance().isUserResolvableError(playServicesStatus)) {
                                        GoogleApiAvailability.getInstance()
                                            .getErrorDialog(activity, playServicesStatus, 1001)
                                            ?.show()
                                    }
                                    return@Button
                                }

                                isGoogleLoading = true
                                googleError = null

                                val webClientId = resolveGoogleWebClientId(context, viewModel.getGoogleClientId())
                                if (webClientId.isBlank()) {
                                    isGoogleLoading = false
                                    googleError = "Google Sign-In setup missing. Add a valid Web Client ID."
                                    return@Button
                                }
                                
                                try {
                                    val credentialManager = CredentialManager.create(context)
                                    val googleIdOption = GetGoogleIdOption.Builder()
                                        .setFilterByAuthorizedAccounts(false)
                                        .setServerClientId(webClientId)
                                        .setAutoSelectEnabled(false)
                                        .build()

                                    val request = GetCredentialRequest.Builder()
                                        .addCredentialOption(googleIdOption)
                                        .build()

                                    scope.launch {
                                        try {
                                            android.util.Log.d("GmailLogin", "Requesting credential...")
                                            val result = credentialManager.getCredential(context = context, request = request)
                                            android.util.Log.d("GmailLogin", "Credential received, handling result...")
                                            handleGoogleSignInResult(
                                                result = result,
                                                viewModel = viewModel,
                                                onLoginSuccess = {
                                                    isGoogleLoading = false
                                                    onLoginSuccess()
                                                },
                                                onError = {
                                                    isGoogleLoading = false
                                                    googleError = it
                                                }
                                            )
                                        } catch (e: NoCredentialException) {
                                            android.util.Log.w("GmailLogin", "No credential via Credential Manager, falling back: ${e.message}")
                                            launchLegacyGoogleAccountPicker(
                                                context = context,
                                                webClientId = webClientId,
                                                launcher = googleSignInLauncher
                                            )
                                        } catch (e: GetCredentialException) {
                                            android.util.Log.e("GmailLogin", "GetCredentialException: ${e.message}")
                                            googleError = when {
                                                e.message?.contains("no_credentials") == true ||
                                                e.message?.contains("NoCredentialException") == true ||
                                                e.message?.contains("No credentials available") == true ->
                                                    null
                                                e.message?.contains("cancelled") == true -> "Sign-in cancelled - Try again"
                                                e.message?.contains("disabled") == true -> "Google Play Services not available"
                                                e.message?.contains("NullPointerException") == true -> "❌ No account linked. Add Gmail in Settings → Accounts"
                                                e.message?.contains("invalid") == true -> "Invalid request - Try again"
                                                e.message?.contains("network") == true || e.message?.contains("IOException") == true -> "❌ Network error - Check internet"
                                                else -> "Error: ${e.message?.take(60) ?: "Sign-in failed"}"
                                            }

                                            if (googleError == null) {
                                                launchLegacyGoogleAccountPicker(
                                                    context = context,
                                                    webClientId = webClientId,
                                                    launcher = googleSignInLauncher
                                                )
                                            } else {
                                                isGoogleLoading = false
                                            }
                                        } catch (e: Exception) {
                                            android.util.Log.e("GmailLogin", "General Exception: ${e.message}")
                                            isGoogleLoading = false
                                            googleError = "Error: ${e.localizedMessage?.take(50) ?: "Unknown error"}"
                                        }
                                    }
                                } catch (e: Exception) {
                                    isGoogleLoading = false
                                    googleError = "Setup error: Check all permissions are granted"
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF1F2937),
                                contentColor = Color.White
                            ),
                            enabled = !isGoogleLoading
                        ) {
                            if (isGoogleLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Signing in...")
                            } else {
                                // Gmail icon (using generic icon + text)
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = "Gmail",
                                    modifier = Modifier.size(20.dp),
                                    tint = Color(0xFFEA4335)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Sign in with Gmail", fontWeight = FontWeight.SemiBold)
                            }
                        }

                        // Error message with retry and clear instructions
                        if (googleError != null) {
                            Spacer(Modifier.height(8.dp))
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(2.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color(0xFFEF5350))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = "⚠️ Sign-in Error",
                                        color = Color(0xFFD32F2F),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = googleError!!,
                                        color = Color(0xFFB71C1C),
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp
                                    )
                                    Spacer(Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        Text(
                                            text = "💡 Tap button again to retry",
                                            color = Color(0xFFC62828),
                                            fontSize = 10.sp,
                                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(Modifier.height(12.dp))

                        // OTP Button
                        OutlinedButton(
                            onClick = { isOtpMode = true },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Sign in with OTP", fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    TextButton(onClick = onNavigateToSignup, modifier = Modifier.fillMaxWidth()) {
                        Text("New here? Create an account", color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

private fun handleGoogleSignInResult(
    result: GetCredentialResponse,
    viewModel: AuthViewModel,
    onLoginSuccess: () -> Unit,
    onError: (String) -> Unit
) {
    try {
        val credential = result.credential

        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val idToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
            if (idToken.isNotBlank()) {
                viewModel.signInWithGoogleToken(idToken, onLoginSuccess)
            } else {
                onError("Google token not received. Try again.")
            }
        } else {
            android.util.Log.w("GmailLogin", "Unexpected credential type: ${credential?.javaClass?.simpleName}")
            onError("Unexpected Google credential response. Try again.")
        }
    } catch (e: Exception) {
        android.util.Log.e("GmailLogin", "Error handling sign-in result: ${e.message}")
        onError("Could not parse Google credential. Check setup and retry.")
    }
}

private fun launchLegacyGoogleAccountPicker(
    context: android.content.Context,
    webClientId: String,
    launcher: androidx.activity.result.ActivityResultLauncher<android.content.Intent>
) {
    val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .requestIdToken(webClientId)
        .build()

    val client = GoogleSignIn.getClient(context, options)
    client.signOut().addOnCompleteListener {
        launcher.launch(client.signInIntent)
    }
}

private fun resolveGoogleWebClientId(context: android.content.Context, fallback: String): String {
    val generatedId = try {
        val resId = context.resources.getIdentifier("default_web_client_id", "string", context.packageName)
        if (resId != 0) context.getString(resId) else ""
    } catch (_: Exception) {
        ""
    }

    return when {
        generatedId.isNotBlank() -> generatedId
        fallback.contains("apps.googleusercontent.com") -> fallback
        else -> ""
    }
}

private fun isInternetAvailable(context: android.content.Context): Boolean {
    try {
        val connectivityManager = context.getSystemService(android.content.Context.CONNECTIVITY_SERVICE) 
            as android.net.ConnectivityManager
        
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return when {
            capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_WIFI) -> true
            capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            capabilities.hasTransport(android.net.NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    } catch (e: Exception) {
        return false
    }
}

