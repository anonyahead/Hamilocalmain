package com.example.hamilocalmain.ui.screens.onboarding

import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.hamilocalmain.ui.navigation.Routes
import com.example.hamilocalmain.ui.theme.PrimaryGreen
import com.example.hamilocalmain.ui.theme.SecondaryOrange
import com.example.hamilocalmain.ui.theme.TextPrimary
import com.example.hamilocalmain.ui.theme.TextSecondary
import com.example.hamilocalmain.ui.viewmodel.AuthState
import com.example.hamilocalmain.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    var phoneNumber by remember { mutableStateOf("") }
    var selectedCountryCode by remember { mutableStateOf("+977") }
    var showCountryPicker by remember { mutableStateOf(false) }
    
    val authState by authViewModel.authState.collectAsState()
    val context = LocalContext.current as ComponentActivity

    val countryCodes = listOf(
        "🇳🇵 Nepal" to "+977",
        "🇮🇳 India" to "+91",
        "🇺🇸 USA" to "+1",
        "🇬🇧 UK" to "+44",
        "🇦🇺 Australia" to "+61",
        "🇨🇦 Canada" to "+1",
        "🇦🇪 UAE" to "+971",
        "🇸🇦 Saudi Arabia" to "+966",
        "🇶🇦 Qatar" to "+974",
        "🇯🇵 Japan" to "+81",
        "🇩🇪 Germany" to "+49",
        "🇫🇷 France" to "+33",
        "🇧🇷 Brazil" to "+55",
        "🇿🇦 South Africa" to "+27",
        "🇳🇬 Nigeria" to "+234",
        "🇵🇰 Pakistan" to "+92",
        "🇧🇩 Bangladesh" to "+880",
        "🇱🇰 Sri Lanka" to "+94",
        "🇲🇾 Malaysia" to "+60",
        "🇸🇬 Singapore" to "+65",
        "🇰🇷 South Korea" to "+82",
        "🇨🇳 China" to "+86",
        "🇷🇺 Russia" to "+7",
        "🇮🇹 Italy" to "+39",
        "🇪🇸 Spain" to "+34",
        "🇳🇿 New Zealand" to "+64",
        "🇵🇭 Philippines" to "+63",
        "🇮🇩 Indonesia" to "+62",
        "🇹🇭 Thailand" to "+66",
        "🇲🇽 Mexico" to "+52"
    )

    LaunchedEffect(authState) {
        if (authState is AuthState.CodeSent) {
            authViewModel.resetState()
            navController.navigate(Routes.phoneVerification(phoneNumber))
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Create Account") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "🌾",
                    fontSize = 80.sp
                )
                
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Create your account",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Register with your phone number",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Phone number row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = PrimaryGreen,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Country code selector
                    Box {
                        Row(
                            modifier = Modifier
                                .clickable { showCountryPicker = true }
                                .padding(end = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedCountryCode,
                                color = PrimaryGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Select country code",
                                tint = PrimaryGreen
                            )
                        }

                        DropdownMenu(
                            expanded = showCountryPicker,
                            onDismissRequest = { showCountryPicker = false },
                            modifier = Modifier.height(300.dp)
                        ) {
                            countryCodes.forEach { (country, code) ->
                                DropdownMenuItem(
                                    text = { Text("$country  $code") },
                                    onClick = {
                                        selectedCountryCode = code
                                        showCountryPicker = false
                                    }
                                )
                            }
                        }
                    }

                    // Divider between code and number
                    Text(
                        text = "|",
                        color = TextSecondary,
                        modifier = Modifier.padding(end = 8.dp)
                    )

                    // Phone number input
                    BasicTextField(
                        value = phoneNumber,
                        onValueChange = { if (it.length <= 10) phoneNumber = it },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        textStyle = TextStyle(
                            color = TextPrimary,
                            fontSize = 16.sp
                        ),
                        decorationBox = { innerTextField ->
                            if (phoneNumber.isEmpty()) {
                                Text("Phone Number", color = TextSecondary)
                            }
                            innerTextField()
                        }
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { 
                        authViewModel.sendOtp(
                            phoneNumber = "$selectedCountryCode$phoneNumber",
                            activity = context
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SecondaryOrange),
                    enabled = phoneNumber.isNotEmpty() && authState !is AuthState.Loading,
                    shape = MaterialTheme.shapes.medium
                ) {
                    if (authState is AuthState.Loading) {
                        CircularProgressIndicator(
                            color = Color.White, 
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Send OTP to Register", style = MaterialTheme.typography.titleMedium)
                    }
                }

                if (authState is AuthState.Error) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = (authState as AuthState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text("Already have an account? ", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "Login",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SecondaryOrange,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { 
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }
}
