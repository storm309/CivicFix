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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartwastemanagementapp.R
import com.example.smartwastemanagementapp.ui.theme.*
import com.example.smartwastemanagementapp.viewmodel.AuthViewModel
import com.example.smartwastemanagementapp.util.LanguageManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability

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

    var passwordVisible by remember { mutableStateOf(false) }
    val scrollState     = rememberScrollState()
    var isGoogleLoading by remember { mutableStateOf(false) }
    var googleError by remember { mutableStateOf<String?>(null) }
    
    var showResetDialog by remember { mutableStateOf(false) }
    var resetStep by remember { mutableIntStateOf(1) } // 1: Email, 2: Code+Pass
    var resetEmail by remember { mutableStateOf("") }
    var resetCode by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != android.app.Activity.RESULT_OK) {
            isGoogleLoading = false
            googleError = "Sign-in cancelled"
            return@rememberLauncherForActivityResult
        }

        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val idToken = account.idToken.orEmpty()
            if (idToken.isNotBlank()) {
                viewModel.signInWithGoogleToken(idToken) {
                    isGoogleLoading = false
                    onLoginSuccess()
                }
            }
        } catch (e: ApiException) {
            isGoogleLoading = false
            googleError = "Google Sign-In failed"
        }
    }

    // Reset Password Dialog (Enhanced 2-Step)
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text(stringResource(R.string.reset_password_title)) },
            text = {
                Column {
                    if (resetStep == 1) {
                        Text(stringResource(R.string.reset_password_msg))
                        Spacer(Modifier.height(16.dp))
                        OutlinedTextField(
                            value = resetEmail,
                            onValueChange = { resetEmail = it },
                            label = { Text(stringResource(R.string.email_address)) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    } else {
                        Text(stringResource(R.string.enter_reset_code))
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = resetCode,
                            onValueChange = { resetCode = it },
                            label = { Text("Reset Code") },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(Modifier.height(16.dp))
                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = { newPassword = it },
                            label = { Text(stringResource(R.string.enter_new_password)) },
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                    
                    viewModel.error.value?.let { err ->
                        Text(err, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp))
                    }
                }
            },
            confirmButton = {
                if (viewModel.isLoading.value) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Button(
                        onClick = {
                            if (resetStep == 1) {
                                viewModel.resetPassword(resetEmail) {
                                    resetStep = 2
                                }
                            } else {
                                viewModel.confirmReset(resetCode, newPassword) {
                                    showResetDialog = false
                                    resetStep = 1
                                    android.widget.Toast.makeText(context, context.getString(R.string.password_reset_success), android.widget.Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                    ) { Text(if (resetStep == 1) stringResource(R.string.send_reset_link) else stringResource(R.string.confirm_reset)) }
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showResetDialog = false
                    resetStep = 1
                }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Gradient header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.38f)
                .background(Brush.verticalGradient(listOf(EcoGreen40, EcoGreen50, Teal40)))
        )

        // Language Toggle at top right
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            val currentLang = LanguageManager.getSelectedLanguage(context)
            TextButton(
                onClick = {
                    val newLang = if (currentLang == "en") "hi" else "en"
                    LanguageManager.setLanguage(context, newLang)
                },
                colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
            ) {
                Icon(Icons.Default.Language, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text(if (currentLang == "en") "हिन्दी" else "English")
            }
        }

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
                    contentDescription = stringResource(R.string.app_logo),
                    modifier = Modifier.padding(12.dp)
                )
            }

            Spacer(Modifier.height(12.dp))

            Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineMedium, color = Color.White, fontWeight = FontWeight.Bold)
            Text(stringResource(R.string.login_to_account), color = Color.White.copy(0.8f))

            Spacer(Modifier.height(32.dp))

            Card(
                modifier = Modifier.padding(horizontal = 24.dp).fillMaxWidth(),
                shape = RoundedCornerShape(32.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
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
                            Text(stringResource(R.string.email_mode), modifier = Modifier.padding(vertical = 12.dp))
                        }
                        Tab(selected = isOtpMode, onClick = { isOtpMode = true }) {
                            Text(stringResource(R.string.otp_mode), modifier = Modifier.padding(vertical = 12.dp))
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    AnimatedContent(targetState = isOtpMode, label = "mode") { mode ->
                        if (!mode) {
                            Column {
                                OutlinedTextField(
                                    value = email,
                                    onValueChange = { email = it },
                                    label = { Text(stringResource(R.string.email_address)) },
                                    leadingIcon = { Icon(Icons.Default.Email, null) },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Email,
                                        autoCorrect = false
                                    )
                                )
                                Spacer(Modifier.height(12.dp))
                                OutlinedTextField(
                                    value = password,
                                    onValueChange = { password = it },
                                    label = { Text(stringResource(R.string.password)) },
                                    leadingIcon = { Icon(Icons.Default.Lock, null) },
                                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                    trailingIcon = {
                                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                            Icon(if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null)
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                                )
                                
                                TextButton(
                                    onClick = { 
                                        resetEmail = email
                                        showResetDialog = true 
                                    },
                                    modifier = Modifier.align(Alignment.End)
                                ) {
                                    Text(stringResource(R.string.forgot_password), style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        } else {
                            Column {
                                if (!isOtpSent) {
                                    OutlinedTextField(
                                        value = phone,
                                        onValueChange = { if (it.length <= 10) phone = it },
                                        label = { Text(stringResource(R.string.phone)) },
                                        prefix = { Text("+91 ") },
                                        leadingIcon = { Icon(Icons.Default.Phone, null) },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(14.dp),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
                                    )
                                } else {
                                    Text(stringResource(R.string.otp_sent, phone), style = MaterialTheme.typography.labelMedium)
                                    Spacer(Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = otpCode,
                                        onValueChange = { if (it.length <= 6) otpCode = it },
                                        label = { Text(stringResource(R.string.enter_otp)) },
                                        leadingIcon = { Icon(Icons.Default.VpnKey, null) },
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(14.dp),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                    )
                                    TextButton(onClick = { isOtpSent = false }, modifier = Modifier.align(Alignment.End)) {
                                        Text(stringResource(R.string.change_number))
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
                            Text(if (!isOtpMode) stringResource(R.string.login_button) else if (!isOtpSent) stringResource(R.string.send_otp) else stringResource(R.string.verify_otp))
                        }

                        LaunchedEffect(viewModel.isLoggedIn.value, isOtpMode) {
                            if (isOtpMode && viewModel.isLoggedIn.value) {
                                onLoginSuccess()
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(Modifier.height(16.dp))

                    Column(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = {
                                if (!isInternetAvailable(context)) {
                                    googleError = context.getString(R.string.error_no_internet)
                                    return@Button
                                }

                                val playServicesStatus = GoogleApiAvailability.getInstance()
                                    .isGooglePlayServicesAvailable(context)
                                if (playServicesStatus != ConnectionResult.SUCCESS) {
                                    googleError = "Google Play Services unavailable"
                                    return@Button
                                }

                                isGoogleLoading = true
                                googleError = null
                                val gso = com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN)
                                    .requestIdToken(com.example.smartwastemanagementapp.BuildConfig.GOOGLE_WEB_CLIENT_ID)
                                    .requestEmail()
                                    .build()
                                val googleSignInClient = GoogleSignIn.getClient(context, gso)
                                googleSignInLauncher.launch(googleSignInClient.signInIntent)
                            },
                            modifier = Modifier.fillMaxWidth().height(54.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                            elevation = ButtonDefaults.buttonElevation(2.dp),
                            enabled = !isGoogleLoading
                        ) {
                            if (isGoogleLoading) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.primary)
                            } else {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(painter = painterResource(R.drawable.ic_gmail), contentDescription = null, tint = Color.Unspecified, modifier = Modifier.size(24.dp))
                                    Spacer(Modifier.width(12.dp))
                                    Text("Google", fontWeight = FontWeight.Medium)
                                }
                            }
                        }

                        googleError?.let { err ->
                            Text(err, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 8.dp).align(Alignment.CenterHorizontally))
                        }

                        Spacer(Modifier.height(24.dp))

                        TextButton(
                            onClick = onNavigateToSignup,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Text(stringResource(R.string.dont_have_account))
                        }
                    }
                }
            }
        }
    }
}

private fun isInternetAvailable(context: android.content.Context): Boolean {
    val cm = context.getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
    return cm.activeNetworkInfo?.isConnected == true
}