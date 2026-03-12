package com.example.hamilocalmain

import android.app.Application
import com.google.firebase.FirebaseApp

/**
 * Application class for Hami Local.
 * Initializes Firebase services on startup.
 * Used as the base class for maintaining global application state.
 */
class HamiLocalApplication : Application() {

    // ==================== LIFECYCLE ====================

    /**
     * Called when the application is starting, before any activity, service, 
     * or receiver objects (excluding content providers) have been created.
     */
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
