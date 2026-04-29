package com.example.smartwastemanagementapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.smartwastemanagementapp.R
import com.example.smartwastemanagementapp.ui.theme.EcoGreen40
import com.example.smartwastemanagementapp.ui.theme.EcoGreen50
import com.example.smartwastemanagementapp.ui.theme.Teal40
import com.example.smartwastemanagementapp.viewmodel.AuthViewModel
import kotlin.math.roundToInt

@Composable
fun CompleteProfileScreen(viewModel: AuthViewModel, isEditMode: Boolean = false, onComplete: () -> Unit) {
    val profile = viewModel.userProfile.value
    var name by remember { mutableStateOf("") }
    var ageValue by remember { mutableFloatStateOf(25f) }
    var gender by remember { mutableStateOf("Male") }
    var phone by remember { mutableStateOf("") }

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
                if (viewModel.isLoading.value) CircularProgressIndicator()
                else Button(onClick = { viewModel.updateProfile(name, ageValue.roundToInt().toString(), gender, phone, onComplete) }, modifier = Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp), enabled = name.isNotBlank()) {
                    Text(if (isEditMode) stringResource(R.string.save_changes) else stringResource(R.string.finish_setup), fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
