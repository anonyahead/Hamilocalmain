package com.example.hamilocalmain

import android.app.Application
import com.google.firebase.FirebaseApp

/**
 * Application class for Hami Local.
 * Initializes Firebase services on startup.
 */
class HamiLocalApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
