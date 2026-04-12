package com.example.smartwastemanagementapp.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.smartwastemanagementapp.R
import com.example.smartwastemanagementapp.ui.theme.*
import com.example.smartwastemanagementapp.viewmodel.AuthViewModel

@Composable
fun SignupScreen(
    onSignupSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    var name           by remember { mutableStateOf("") }
    var email          by remember { mutableStateOf("") }
    var age            by remember { mutableStateOf("") }
    // Phone always starts with +91; user only enters the 10 digits after
    var phoneDigits    by remember { mutableStateOf("") }
    var gender         by remember { mutableStateOf("Male") }
    var password       by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val scrollState  = rememberScrollState()
    val focusManager = LocalFocusManager.current

    val cardAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(600, easing = FastOutSlowInEasing),
        label = "card_alpha"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // ── Gradient header ──────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.30f)
                .background(Brush.verticalGradient(listOf(EcoGreen40, EcoGreen50, Teal40)))
        ) {
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .offset(x = (-30).dp, y = (-30).dp)
                    .alpha(0.1f)
                    .background(Color.White, RoundedCornerShape(50))
            )
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .align(Alignment.BottomEnd)
                    .offset(x = 20.dp, y = 30.dp)
                    .alpha(0.08f)
                    .background(Color.White, RoundedCornerShape(50))
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .systemBarsPadding()
                .imePadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(36.dp))

            // Logo
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .shadow(elevation = 12.dp, shape = RoundedCornerShape(20.dp))
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.app_logo),
                    contentDescription = "Logo",
                    modifier = Modifier.size(52.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text  = "CivicFix",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-0.5).sp
                ),
                color = Color.White
            )

            Spacer(Modifier.height(24.dp))

            // ── Form card ─────────────────────────────────────────
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .alpha(cardAlpha),
                shape     = RoundedCornerShape(28.dp),
                elevation = CardDefaults.cardElevation(12.dp),
                colors    = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Create Account",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        "Join us to report & fix civic issues",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp, bottom = 20.dp)
                    )

                    // Full Name
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name") },
                        leadingIcon = { Icon(Icons.Default.Person, null, tint = MaterialTheme.colorScheme.primary) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text, imeAction = ImeAction.Next),
                        colors = fieldColors()
                    )

                    Spacer(Modifier.height(12.dp))

                    // Email
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it.trim() },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Default.Email, null, tint = MaterialTheme.colorScheme.primary) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                        colors = fieldColors()
                    )

                    Spacer(Modifier.height(12.dp))

                    // Age + Phone row
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Age
                        OutlinedTextField(
                            value = age,
                            onValueChange = { if (it.length <= 3 && it.all(Char::isDigit)) age = it },
                            label = { Text("Age") },
                            leadingIcon = { Icon(Icons.Default.DateRange, null, tint = MaterialTheme.colorScheme.primary) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                            colors = fieldColors()
                        )

                        // Phone – +91 prefix locked, user types 10 digits
                        OutlinedTextField(
                            value = phoneDigits,
                            onValueChange = { input ->
                                val digits = input.filter(Char::isDigit)
                                if (digits.length <= 10) phoneDigits = digits
                            },
                            label = { Text("Phone") },
                            leadingIcon = {
                                Text(
                                    text = "+91",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(start = 12.dp)
                                )
                            },
                            placeholder = { Text("XXXXXXXXXX") },
                            modifier = Modifier.weight(2f),
                            shape = RoundedCornerShape(14.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                            colors = fieldColors()
                        )
                    }

                    Spacer(Modifier.height(12.dp))

                    // Gender selection chips
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            "Gender",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            listOf("Male", "Female", "Other").forEach { opt ->
                                val selected = gender == opt
                                FilterChip(
                                    selected = selected,
                                    onClick  = { gender = opt },
                                    label    = { Text(opt) },
                                    leadingIcon = if (selected) {
                                        { Icon(Icons.Default.Check, null, modifier = Modifier.size(16.dp)) }
                                    } else null,
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor     = MaterialTheme.colorScheme.onPrimaryContainer,
                                        selectedLeadingIconColor = MaterialTheme.colorScheme.primary
                                    )
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Password
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, null, tint = MaterialTheme.colorScheme.primary) },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = null
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                        colors = fieldColors()
                    )

                    // Password strength hint
                    if (password.isNotEmpty()) {
                        Spacer(Modifier.height(6.dp))
                        val strength = when {
                            password.length >= 8 && password.any(Char::isDigit) && password.any(Char::isUpperCase) -> "Strong 💪"
                            password.length >= 6 -> "Medium"
                            else -> "Weak"
                        }
                        val strengthColor = when (strength) {
                            "Strong 💪" -> MaterialTheme.colorScheme.primary
                            "Medium"    -> MaterialTheme.colorScheme.tertiary
                            else        -> MaterialTheme.colorScheme.error
                        }
                        Text(
                            text  = "Password strength: $strength",
                            style = MaterialTheme.typography.labelSmall,
                            color = strengthColor
                        )
                    }

                    // Error banner
                    viewModel.error.value?.let { err ->
                        Spacer(Modifier.height(8.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape    = RoundedCornerShape(10.dp),
                            color    = MaterialTheme.colorScheme.errorContainer
                        ) {
                            Text(
                                text     = err,
                                color    = MaterialTheme.colorScheme.error,
                                style    = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(12.dp, 8.dp)
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    if (viewModel.isLoading.value) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    } else {
                        Button(
                            onClick = {
                                viewModel.signUp(
                                    name     = name,
                                    email    = email,
                                    age      = age,
                                    phone    = "+91$phoneDigits",
                                    gender   = gender,
                                    pass     = password,
                                    onSuccess = onSignupSuccess
                                )
                            },
                            modifier = Modifier.fillMaxWidth().height(54.dp),
                            shape    = RoundedCornerShape(14.dp),
                            colors   = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                        ) {
                            Text("Create Account", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(Modifier.height(16.dp))

                    TextButton(onClick = onNavigateToLogin) {
                        Text(
                            "Already have an account? ",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            "Login",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor   = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
    focusedLabelColor    = MaterialTheme.colorScheme.primary
)
