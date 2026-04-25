package com.example.smartwastemanagementapp.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartwastemanagementapp.R
import com.example.smartwastemanagementapp.ui.theme.*
import com.example.smartwastemanagementapp.viewmodel.AuthViewModel
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

                        // Monitor Login Success for OTP
                        LaunchedEffect(viewModel.isLoggedIn.value) {
                            if (viewModel.isLoggedIn.value) {
                                onLoginSuccess()
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                val credentialManager = CredentialManager.create(context)
                                val googleIdOption = GetGoogleIdOption.Builder()
                                    .setFilterByAuthorizedAccounts(false)
                                    .setServerClientId("923838274395-5m3f2n1p6n9m8v7b4v5n3n2p1n0m9v8b.apps.googleusercontent.com")
                                    .setAutoSelectEnabled(true)
                                    .build()

                                val request = GetCredentialRequest.Builder()
                                    .addCredentialOption(googleIdOption)
                                    .build()

                                scope.launch {
                                    try {
                                        val result = credentialManager.getCredential(context = context, request = request)
                                        handleGoogleSignInResult(result, viewModel, onLoginSuccess)
                                    } catch (e: GetCredentialException) {
                                        // Handle cancellation or error
                                    }
                                }
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Google")
                        }
                        OutlinedButton(
                            onClick = { isOtpMode = true },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("OTP")
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
    onLoginSuccess: () -> Unit
) {
    val credential = result.credential
    if (credential is com.google.android.libraries.identity.googleid.GoogleIdTokenCredential) {
        val idToken = credential.idToken
        viewModel.signInWithGoogleToken(idToken, onLoginSuccess)
    }
}
