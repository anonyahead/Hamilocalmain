package com.example.hamilocalmain.ui.screens.onboarding

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.hamilocalmain.data.model.UserType
import com.example.hamilocalmain.ui.navigation.Routes
import com.example.hamilocalmain.ui.theme.PrimaryGreen
import com.example.hamilocalmain.ui.viewmodel.AuthState
import com.example.hamilocalmain.ui.viewmodel.AuthViewModel
import com.example.hamilocalmain.ui.viewmodel.LocationViewModel

/**
 * Screen for new users to set up their profile after phone verification.
 * Allows entering name, address, and selecting a role (Farmer or Consumer).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSetupScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    locationViewModel: LocationViewModel
) {
    var name by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf<UserType?>(null) }
    var addressInput by remember { mutableStateOf("") }
    
    val authState by authViewModel.authState.collectAsState()
    val detectedAddress by locationViewModel.currentAddress.collectAsState()
    val context = LocalContext.current

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            locationViewModel.requestLocation(context)
        }
    }

    // Sync address input with detected address if location is picked
    LaunchedEffect(detectedAddress) {
        if (detectedAddress.isNotEmpty() && detectedAddress != "Address not found") {
            addressInput = detectedAddress
        }
    }

    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            authViewModel.resetState()
            if (selectedRole == UserType.FARMER) {
                navController.navigate(Routes.FARMER_DASHBOARD) {
                    popUpTo(Routes.WELCOME) { inclusive = true }
                }
            } else {
                navController.navigate(Routes.CONSUMER_HOME) {
                    popUpTo(Routes.WELCOME) { inclusive = true }
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Set Up Profile") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Name Input
            TextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Role Selection
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "I am a...",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    RoleCard(
                        role = "Farmer",
                        description = "Sell your harvest directly",
                        emoji = "🌾",
                        isSelected = selectedRole == UserType.FARMER,
                        onClick = { selectedRole = UserType.FARMER },
                        modifier = Modifier.weight(1f)
                    )
                    RoleCard(
                        role = "Consumer",
                        description = "Buy fresh local produce",
                        emoji = "🛒",
                        isSelected = selectedRole == UserType.CONSUMER,
                        onClick = { selectedRole = UserType.CONSUMER },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Address Input
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextField(
                    value = addressInput,
                    onValueChange = { addressInput = it },
                    label = { Text("Address (City/District)") },
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = {
                        IconButton(onClick = { 
                            when {
                                ContextCompat.checkSelfPermission(
                                    context, Manifest.permission.ACCESS_FINE_LOCATION
                                ) == PackageManager.PERMISSION_GRANTED -> {
                                    locationViewModel.requestLocation(context)
                                }
                                else -> {
                                    locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                }
                            }
                        }) {
                            Icon(Icons.Default.LocationOn, contentDescription = "Pick My Location", tint = PrimaryGreen)
                        }
                    }
                )
                TextButton(
                    onClick = { 
                        when {
                            ContextCompat.checkSelfPermission(
                                context, Manifest.permission.ACCESS_FINE_LOCATION
                            ) == PackageManager.PERMISSION_GRANTED -> {
                                locationViewModel.requestLocation(context)
                            }
                            else -> {
                                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            }
                        }
                    },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Icon(Icons.Default.LocationOn, contentDescription = null)
                    Spacer(Modifier.width(4.dp))
                    Text("Pick My Location", color = PrimaryGreen)
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Submit Button
            Button(
                onClick = {
                    if (name.isNotBlank() && selectedRole != null) {
                        authViewModel.saveProfile(name, selectedRole!!, addressInput)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = name.isNotBlank() && selectedRole != null && addressInput.isNotBlank() && authState !is AuthState.Loading,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
            ) {
                if (authState is AuthState.Loading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Complete Setup", style = MaterialTheme.typography.titleMedium)
                }
            }

            if (authState is AuthState.Error) {
                Text(
                    text = (authState as AuthState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
}

/**
 * A selectable card for choosing a user role.
 */
@Composable
private fun RoleCard(
    role: String,
    description: String,
    emoji: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant
        ),
        border = if (isSelected) BorderStroke(2.dp, PrimaryGreen) else null
    ) {
        Box(modifier = Modifier.padding(12.dp)) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = PrimaryGreen,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(20.dp)
                )
            }
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = emoji, style = MaterialTheme.typography.headlineMedium)
                Text(text = role, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}
