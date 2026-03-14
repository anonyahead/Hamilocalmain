/**
 * Welcome screen. Initial entry point for non-logged in users.
 */
package com.example.hamilocalmain.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.hamilocalmain.ui.navigation.Routes
import com.example.hamilocalmain.ui.theme.HamiLocalTheme
import com.example.hamilocalmain.ui.theme.PrimaryGreen
import com.example.hamilocalmain.ui.theme.PrimaryGreenDark
import com.example.hamilocalmain.ui.theme.SecondaryOrange
import com.example.hamilocalmain.ui.theme.TextPrimary
import com.example.hamilocalmain.ui.theme.TextSecondary

@Preview(showBackground = true, device = "spec:width=1280dp,height=800dp,dpi=240") // Tablet
@Preview(showBackground = true, device = "spec:width=1920dp,height=1080dp,dpi=160") // Desktop
@Preview(showBackground = true) // Standard Phone
@Composable
fun WelcomeScreenPreview() {
    HamiLocalTheme {
        // You would need a dummy NavController
        WelcomeScreen(navController = rememberNavController())
    }
}

/**
 * Full-screen welcome view with branding and entry action.
 */
@Composable
fun WelcomeScreen(navController: NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(PrimaryGreen, PrimaryGreenDark)
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            WelcomeBranding()
            
            Spacer(modifier = Modifier.height(48.dp))
            
            GetStartedButton(onClick = {
                navController.navigate(Routes.LOGIN)
            })
        }
    }
}

/**
 * Displays the app logo emoji and title text.
 */
@Composable
private fun WelcomeBranding() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "🌾",
            fontSize = 100.sp
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Hami Local",
            style = MaterialTheme.typography.displayLarge,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = "हामी स्थानीय, हामी सशक्त",
            style = MaterialTheme.typography.bodyLarge,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Primary action button to proceed to login.
 */
@Composable
private fun GetStartedButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = SecondaryOrange
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Get Started",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = Color.White
            )
        }
    }
}
