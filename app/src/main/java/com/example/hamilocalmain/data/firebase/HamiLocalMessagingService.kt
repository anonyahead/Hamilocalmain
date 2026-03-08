package com.example.hamilocalmain.data.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.hamilocalmain.MainActivity
import com.example.hamilocalmain.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * FCM push notification handler. Shows local notification for new orders, messages, and order status updates.
 */
class HamiLocalMessagingService : FirebaseMessagingService() {

    /**
     * Called when a message is received from Firebase Cloud Messaging.
     * Handles both notification payloads and data payloads to ensure the user is notified.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        // 1. Try to get info from the notification payload
        var title = remoteMessage.notification?.title
        var body = remoteMessage.notification?.body

        // 2. If notification payload is empty, fall back to data payload
        if (remoteMessage.data.isNotEmpty()) {
            if (title == null) title = remoteMessage.data["title"]
            if (body == null) body = remoteMessage.data["body"]
        }

        if (title != null || body != null) {
            showNotification(title ?: "Hami Local", body ?: "")
        }
    }

    /**
     * Creates and displays a local notification with the given title and body.
     * Sets up a Notification Channel for Android O and above.
     *
     * @param title The title text of the notification.
     * @param body The main body text of the notification.
     */
    private fun showNotification(title: String, body: String) {
        val channelId = "hami_local_notifications"
        val notificationManager = NotificationManagerCompat.from(this)

        // Create notification channel for Android O+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Hami Local Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            // Using Context.NOTIFICATION_SERVICE for better compatibility
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        // Intent to open MainActivity when notification is clicked
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 
            0, 
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            // Note: Launcher icon is used as requested. A vector drawable is preferred for notifications.
            .setSmallIcon(R.mipmap.ic_launcher) 
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)

        // Show the notification. Using a hash of the body to allow unique messages 
        // while preventing duplicates for the same message content.
        try {
            notificationManager.notify(body.hashCode(), notificationBuilder.build())
        } catch (e: SecurityException) {
            Log.e(TAG, "Notification permission not granted (Android 13+)", e)
        }
    }

    /**
     * Called when a new FCM registration token is generated.
     *
     * @param token The new token.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed token: $token")
    }

    companion object {
        private const val TAG = "HamiLocalMessaging"
    }
}
