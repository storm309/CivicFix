package com.example.smartwastemanagementapp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.smartwastemanagementapp.R
import com.example.smartwastemanagementapp.ui.theme.EcoGreen40
import com.example.smartwastemanagementapp.ui.theme.EcoGreen50
import com.example.smartwastemanagementapp.ui.theme.Teal40
import com.example.smartwastemanagementapp.viewmodel.AuthViewModel
import kotlin.math.roundToInt

@Composable
fun CompleteProfileScreen(viewModel: AuthViewModel, isEditMode: Boolean = false, onComplete: () -> Unit) {
    val context = LocalContext.current
    val profile = viewModel.userProfile.value
    var name by remember { mutableStateOf("") }
    var ageValue by remember { mutableFloatStateOf(25f) }
    var gender by remember { mutableStateOf("Male") }
    var phone by remember { mutableStateOf("") }

    // Password Change State
    var newPassword by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var showPasswordSection by remember { mutableStateOf(false) }

    LaunchedEffect(profile?.uid) {
        profile?.let {
            name = it.name
            ageValue = it.age.toFloatOrNull() ?: 25f
            gender = it.gender.ifBlank { "Male" }
            phone = it.phoneNumber
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(EcoGreen40, EcoGreen50, Teal40)))) {
        Card(modifier = Modifier.align(Alignment.Center).padding(24.dp), shape = RoundedCornerShape(32.dp)) {
            Column(modifier = Modifier.padding(24.dp).verticalScroll(rememberScrollState()), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(if (isEditMode) stringResource(R.string.my_profile) else stringResource(R.string.welcome_back), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold)
                Text(if (isEditMode) stringResource(R.string.edit_profile_desc) else stringResource(R.string.join_us), style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(top = 4.dp, bottom = 24.dp))
                
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text(stringResource(R.string.full_name)) }, leadingIcon = { Icon(Icons.Default.Person, null) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text(stringResource(R.string.phone)) }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp))
                Spacer(Modifier.height(20.dp))
                
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("${stringResource(R.string.age)}: ${ageValue.roundToInt()}", style = MaterialTheme.typography.labelLarge)
                    Slider(value = ageValue, onValueChange = { ageValue = it }, valueRange = 10f..90f)
                }
                Spacer(Modifier.height(20.dp))
                
                Text(stringResource(R.string.gender), style = MaterialTheme.typography.labelLarge, modifier = Modifier.align(Alignment.Start))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("Male", "Female", "Other").forEach { opt ->
                        val label = when(opt) {
                            "Male" -> stringResource(R.string.male)
                            "Female" -> stringResource(R.string.female)
                            else -> stringResource(R.string.other)
                        }
                        FilterChip(selected = gender == opt, onClick = { gender = opt }, label = { Text(label) })
                    }
                }
                Spacer(Modifier.height(32.dp))

                if (isEditMode) {
                    if (!showPasswordSection) {
                        OutlinedButton(
                            onClick = { showPasswordSection = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                        ) {
                            Icon(Icons.Default.Lock, null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(stringResource(R.string.reset_password_title))
                        }
                    } else {
                        Column(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(16.dp)).padding(16.dp)) {
                            Text(stringResource(R.string.enter_new_password), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            OutlinedTextField(
                                value = newPassword,
                                onValueChange = { newPassword = it },
                                label = { Text(stringResource(R.string.password)) },
                                leadingIcon = { Icon(Icons.Default.Lock, null) },
                                visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                        Icon(if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff, null)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            )
                            Spacer(Modifier.height(12.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextButton(onClick = { showPasswordSection = false; newPassword = "" }, modifier = Modifier.weight(1f)) {
                                    Text(stringResource(R.string.cancel))
                                }
                                Button(
                                    onClick = { 
                                        viewModel.changePassword(newPassword) {
                                            showPasswordSection = false
                                            newPassword = ""
                                            android.widget.Toast.makeText(context, context.getString(R.string.password_reset_success), android.widget.Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.weight(1.5f),
                                    shape = RoundedCornerShape(12.dp),
                                    enabled = newPassword.length >= 6
                                ) {
                                    Text(stringResource(R.string.confirm_reset))
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(24.dp))
                }

                if (viewModel.isLoading.value) CircularProgressIndicator()
                else Button(onClick = { viewModel.updateProfile(name, ageValue.roundToInt().toString(), gender, phone, onComplete) }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp), enabled = name.isNotBlank()) {
                    Text(if (isEditMode) stringResource(R.string.save_changes) else stringResource(R.string.finish_setup), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
