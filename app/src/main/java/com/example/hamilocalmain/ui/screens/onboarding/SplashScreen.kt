/**
 * Splash screen. 2-second delay then routes based on Firebase auth state.
 */
package com.example.hamilocalmain.ui.screens.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.hamilocalmain.data.model.UserType
import com.example.hamilocalmain.ui.navigation.Routes
import com.example.hamilocalmain.ui.theme.PrimaryGreen
import com.example.hamilocalmain.ui.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    navController: NavController,
    authViewModel: AuthViewModel
) {
    val currentUser by authViewModel.currentUser.collectAsState()

    LaunchedEffect(Unit) {
        delay(2000)
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        if (firebaseUser == null) {
            navController.navigate(Routes.WELCOME) {
                popUpTo(Routes.SPLASH) { inclusive = true }
            }
        } else {
            // Wait up to 3 more seconds for the profile to load from Firestore
            var waited = 0
            while (currentUser == null && waited < 3000) {
                delay(200)
                waited += 200
            }
            when (currentUser?.userType) {
                UserType.FARMER -> navController.navigate(Routes.FARMER_DASHBOARD) {
                    popUpTo(Routes.SPLASH) { inclusive = true }
                }
                UserType.CONSUMER -> navController.navigate(Routes.CONSUMER_HOME) {
                    popUpTo(Routes.SPLASH) { inclusive = true }
                }
                else -> navController.navigate(Routes.PROFILE_SETUP) {
                    popUpTo(Routes.SPLASH) { inclusive = true }
                }
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🌾",
                fontSize = 80.sp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Hami Local",
                style = MaterialTheme.typography.displayMedium,
                color = PrimaryGreen,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
