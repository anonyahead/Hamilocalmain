package com.example.hamilocalmain.ui.screens.shared

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.hamilocalmain.ui.navigation.Routes
import com.example.hamilocalmain.ui.theme.Error
import com.example.hamilocalmain.ui.theme.PrimaryGreen
import com.example.hamilocalmain.ui.theme.TextSecondary
import com.example.hamilocalmain.ui.viewmodel.AuthState
import com.example.hamilocalmain.ui.viewmodel.AuthViewModel
import com.example.hamilocalmain.ui.viewmodel.CurrencyViewModel
import com.example.hamilocalmain.ui.viewmodel.supportedCurrencies

/**
 * Settings screen for user profile management and app preferences.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    currencyViewModel: CurrencyViewModel
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }
    val authState by authViewModel.authState.collectAsState()
    val selectedCurrency by currencyViewModel.selectedCurrency.collectAsState()
    var showCurrencyPicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Profile Info Card
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = currentUser?.name ?: "User Name",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = currentUser?.phone ?: "Phone Number",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        SuggestionChip(
                            onClick = { },
                            label = { Text(currentUser?.userType?.name ?: "CONSUMER") }
                        )
                    }
                }
            }

            // Edit Profile button
            Button(
                onClick = { navController.navigate(Routes.PROFILE_SETUP) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Edit Profile")
            }

            // Currency Picker Card
            Card(modifier = Modifier.fillMaxWidth(), onClick = { showCurrencyPicker = true }) {
                Row(Modifier.padding(16.dp).fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(selectedCurrency.symbol, fontSize = 24.sp)
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("Currency")
                            Text("${selectedCurrency.name} (${selectedCurrency.code})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                }
            }

            // Delete Account Button
            OutlinedButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Error)
            ) {
                Icon(Icons.Default.DeleteForever, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Delete My Account")
            }

            Spacer(modifier = Modifier.weight(1f))

            // Logout Button
            Button(
                onClick = {
                    authViewModel.logout()
                    navController.navigate(Routes.WELCOME) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Error)
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Logout")
            }
        }

        if (showCurrencyPicker) {
            AlertDialog(
                onDismissRequest = { showCurrencyPicker = false },
                title = { Text("Select Currency") },
                text = {
                    LazyColumn {
                        items(supportedCurrencies) { currency ->
                            Row(
                                modifier = Modifier.fillMaxWidth()
                                    .clickable {
                                        currencyViewModel.setCurrency(currency)
                                        showCurrencyPicker = false
                                    }
                                    .padding(vertical = 12.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(currency.symbol, fontSize = 20.sp, modifier = Modifier.width(40.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(currency.name, fontWeight = FontWeight.Bold)
                                    Text(currency.code, style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary)
                                }
                                if (selectedCurrency.code == currency.code) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = PrimaryGreen)
                                }
                            }
                        }
                    }
                },
                confirmButton = {}
            )
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete Account?") },
                text = {
                    Text("This will permanently delete your account and all your data. This cannot be undone.")
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteDialog = false
                            authViewModel.deleteAccount(
                                onSuccess = {
                                    navController.navigate(Routes.WELCOME) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                },
                                onError = { /* error already shown via authState */ }
                            )
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = Error)
                    ) {
                        if (authState is AuthState.Loading) {
                            CircularProgressIndicator(modifier = Modifier.size(16.dp))
                        } else {
                            Text("Yes, Delete Everything")
                        }
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
