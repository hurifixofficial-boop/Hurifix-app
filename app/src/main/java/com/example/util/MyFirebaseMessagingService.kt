package com.example.util

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "MyFirebaseMsgService"
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        try {
            Log.d(TAG, "Refreshed FCM token: $token")
        } catch (e: Exception) {
            Log.e(TAG, "Error handling new FCM token", e)
        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        try {
            Log.d(TAG, "From: ${remoteMessage.from}")
            if (remoteMessage.data.isNotEmpty()) {
                Log.d(TAG, "Message data payload: ${remoteMessage.data}")
            }
            remoteMessage.notification?.let {
                // Requirement 7: Suppress notifications when app is open in foreground
                if (AppLifecycleTracker.isAppInForeground) {
                    Log.d(TAG, "Suppressed FCM notification because app is open in foreground")
                    return
                }
                Log.d(TAG, "Message Notification Body: ${it.body}")
                NotificationHelper.showInstantNotification(
                    applicationContext,
                    it.title ?: "Hurifix Update",
                    it.body ?: "You have a new update"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling incoming FCM message", e)
        }
    }
}
