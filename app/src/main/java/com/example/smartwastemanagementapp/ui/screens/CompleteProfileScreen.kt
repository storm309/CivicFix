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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.smartwastemanagementapp.ui.theme.EcoGreen40
import com.example.smartwastemanagementapp.ui.theme.EcoGreen50
import com.example.smartwastemanagementapp.ui.theme.Teal40
import com.example.smartwastemanagementapp.viewmodel.AuthViewModel
import kotlin.math.roundToInt

@Composable
fun CompleteProfileScreen(
    viewModel: AuthViewModel,
    onComplete: () -> Unit
) {
    var name     by remember { mutableStateOf("") }
    var ageValue by remember { mutableFloatStateOf(25f) }
    var gender   by remember { mutableStateOf("Male") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(EcoGreen40, EcoGreen50, Teal40)))
    ) {
        Card(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(24.dp),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Welcome to CivicFix!",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold)
                )
                Text(
                    "Tell us a bit about yourself to get started",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                )

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Your Full Name") },
                    leadingIcon = { Icon(Icons.Default.Person, null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(Modifier.height(20.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Age: ${ageValue.roundToInt()} years", style = MaterialTheme.typography.labelLarge)
                    Slider(
                        value = ageValue,
                        onValueChange = { ageValue = it },
                        valueRange = 10f..90f
                    )
                }

                Spacer(Modifier.height(20.dp))

                Text("Gender", style = MaterialTheme.typography.labelLarge, modifier = Modifier.align(Alignment.Start))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Male", "Female", "Other").forEach { opt ->
                        FilterChip(
                            selected = gender == opt,
                            onClick = { gender = opt },
                            label = { Text(opt) }
                        )
                    }
                }

                Spacer(Modifier.height(32.dp))

                if (viewModel.isLoading.value) {
                    CircularProgressIndicator()
                } else {
                    Button(
                        onClick = { viewModel.updateProfile(name, ageValue.roundToInt().toString(), gender, onComplete) },
                        modifier = Modifier.fillMaxWidth().height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        enabled = name.isNotBlank()
                    ) {
                        Text("Finish Setup", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
