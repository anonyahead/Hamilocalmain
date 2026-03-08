package com.example.hamilocalmain

import android.app.Application
import com.google.firebase.FirebaseApp

/**
 * Application class. Initializes Firebase on app startup.
 */
class HamiLocalApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
