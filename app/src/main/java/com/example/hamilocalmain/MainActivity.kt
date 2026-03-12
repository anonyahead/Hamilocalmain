package com.example.hamilocalmain

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.hamilocalmain.ui.navigation.HamiLocalNavHost
import com.example.hamilocalmain.ui.theme.HamiLocalTheme
import com.example.hamilocalmain.ui.viewmodel.AuthViewModel
import com.example.hamilocalmain.ui.viewmodel.ChatViewModel
import com.example.hamilocalmain.ui.viewmodel.LocationViewModel
import com.example.hamilocalmain.ui.viewmodel.OrderViewModel
import com.example.hamilocalmain.ui.viewmodel.ProductViewModel

/**
 * Main entry point of the Hami Local application.
 * Manages the top-level UI state and navigation.
 */
class MainActivity : ComponentActivity() {
    
    private val authViewModel: AuthViewModel by viewModels()
    private val productViewModel: ProductViewModel by viewModels()
    private val orderViewModel: OrderViewModel by viewModels()
    private val chatViewModel: ChatViewModel by viewModels()
    private val locationViewModel: LocationViewModel by viewModels()

    /**
     * Initializes the activity, sets up window size classes, and launches the Compose UI.
     */
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val windowSizeClass = calculateWindowSizeClass(this)
        
        setContent {
            HamiLocalTheme {
                val navController = rememberNavController()
                
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    HamiLocalNavHost(
                        navController = navController,
                        authViewModel = authViewModel,
                        productViewModel = productViewModel,
                        orderViewModel = orderViewModel,
                        chatViewModel = chatViewModel,
                        locationViewModel = locationViewModel,
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}
