package com.example.hamilocalmain

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.example.hamilocalmain.ui.navigation.HamiLocalNavHost
import com.example.hamilocalmain.ui.theme.HamiLocalTheme
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Main Activity for Hami Local. Single entry point. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseAuth.getInstance()
            .firebaseAuthSettings
            .forceRecaptchaFlowForTesting(true)
        
        lifecycleScope.launch {
            delay(100)
            setContent {
                HamiLocalTheme {
                    HamiLocalNavHost()
                }
            }
        }
    }
}
