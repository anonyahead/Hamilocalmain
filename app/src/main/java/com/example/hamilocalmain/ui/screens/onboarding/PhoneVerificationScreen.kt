package com.example.hamilocalmain.ui.screens.onboarding

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.hamilocalmain.ui.theme.SecondaryOrange
import com.example.hamilocalmain.ui.viewmodel.AuthState
import com.example.hamilocalmain.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.delay

/**
 * Screen for verifying the OTP code sent via SMS.
 * Features a 6-digit auto-advancing input field and a countdown timer for resending OTP.
 */
@Composable
fun PhoneVerificationScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    phone: String
) {
    var otpCode by remember { mutableStateOf(List(6) { "" }) }
    val authState by authViewModel.authState.collectAsState()
    var timerSeconds by remember { mutableIntStateOf(60) }

    LaunchedEffect(Unit) {
        while (timerSeconds > 0) {
            delay(1000L)
            timerSeconds--
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Verify OTP",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = "Sent to +977 $phone",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        OtpInputField(
            otpCode = otpCode,
            onOtpChange = { index, value ->
                if (value.length <= 1) {
                    val newOtp = otpCode.toMutableList()
                    newOtp[index] = value
                    otpCode = newOtp
                }
            }
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = { authViewModel.verifyOtp(otpCode.joinToString(""), "") }, // VerificationId should be handled via ViewModel state
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = SecondaryOrange),
            enabled = otpCode.all { it.isNotEmpty() } && authState !is AuthState.Loading
        ) {
            if (authState is AuthState.Loading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text("Verify", style = MaterialTheme.typography.titleMedium)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (timerSeconds > 0) {
            Text(
                text = "Resend code in ${timerSeconds}s",
                style = MaterialTheme.typography.bodySmall
            )
        } else {
            TextButton(onClick = { 
                authViewModel.sendOtp("+977$phone")
                timerSeconds = 60
            }) {
                Text("Resend OTP", color = SecondaryOrange)
            }
        }
    }
}

/**
 * A custom OTP input field consisting of 6 individual digit boxes.
 */
@Composable
private fun OtpInputField(
    otpCode: List<String>,
    onOtpChange: (Int, String) -> Unit
) {
    val focusRequesters = remember { List(6) { FocusRequester() } }

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        otpCode.forEachIndexed { index, digit ->
            BasicTextField(
                value = digit,
                onValueChange = { 
                    onOtpChange(index, it)
                    if (it.isNotEmpty() && index < 5) {
                        focusRequesters[index + 1].requestFocus()
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .focusRequester(focusRequesters[index])
                    .border(1.dp, Color.Gray, MaterialTheme.shapes.small),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                decorationBox = { innerTextField ->
                    Box(contentAlignment = Alignment.Center) {
                        if (digit.isEmpty()) Text("", color = Color.LightGray)
                        innerTextField()
                    }
                },
                textStyle = LocalTextStyle.current.copy(
                    textAlign = TextAlign.Center,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
    
    // Initial focus
    LaunchedEffect(Unit) {
        focusRequesters[0].requestFocus()
    }
}
