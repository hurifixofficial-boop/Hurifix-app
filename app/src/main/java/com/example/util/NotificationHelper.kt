package com.example.util

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.CustomerJobEntity
import com.example.data.model.JobStatus

object NotificationHelper {

    private const val CHANNEL_ORDERS = "hurifix_order_alerts"
    private const val CHANNEL_MESSAGES = "hurifix_message_reminders"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val orderChannel = NotificationChannel(
                CHANNEL_ORDERS,
                "Order Alerts & Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts for orders pending or processing for over 1 hour"
                enableVibration(true)
            }

            val messageChannel = NotificationChannel(
                CHANNEL_MESSAGES,
                "WhatsApp Message Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Reminders for pending WhatsApp messages"
            }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            manager?.createNotificationChannel(orderChannel)
            manager?.createNotificationChannel(messageChannel)
        }
    }

    fun checkAndTriggerReminders(context: Context, jobs: List<CustomerJobEntity>) {
        // Requirement 7: jab phone me app khula ho or foreground me use ho rha ho tab us phone me koi bhi notification naa aay
        if (AppLifecycleTracker.isAppInForeground) {
            return
        }

        createNotificationChannels(context)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val now = System.currentTimeMillis()
        val oneHourMs = 60 * 60 * 1000L

        jobs.forEach { job ->
            val elapsedFromCreation = now - job.createdAt

            // 1. Pending for more than 1 hour
            if (job.status == JobStatus.PENDING.name && elapsedFromCreation >= oneHourMs) {
                val hours = (elapsedFromCreation / oneHourMs).toInt()
                sendNotification(
                    context = context,
                    id = (job.id * 10 + 1).toInt(),
                    channelId = CHANNEL_ORDERS,
                    title = "⏳ Pending Order Alert (#${job.id})",
                    message = "Order for ${job.customerName} has been pending for $hours hr(s). Please assign a technician."
                )
            }

            // 2. Processing for more than 1 hour
            if (job.status == JobStatus.PROCESSING.name && elapsedFromCreation >= oneHourMs) {
                val hours = (elapsedFromCreation / oneHourMs).toInt()
                sendNotification(
                    context = context,
                    id = (job.id * 10 + 2).toInt(),
                    channelId = CHANNEL_ORDERS,
                    title = "⚙️ Processing Order Follow-up (#${job.id})",
                    message = "Work assigned to ${job.assignedExpertName ?: "Expert"} has been in progress for $hours hr(s). Check status."
                )
            }

            // 3. Unsent WhatsApp message dismissed "Later" > 1 hour
            val dismissedAt = job.assignMessageLaterDismissedAt
            if (dismissedAt != null && (now - dismissedAt) >= oneHourMs && (!job.isExpertNotified || !job.isCustomerNotifiedOnAssign)) {
                sendNotification(
                    context = context,
                    id = (job.id * 10 + 3).toInt(),
                    channelId = CHANNEL_MESSAGES,
                    title = "📲 WhatsApp Message Reminder (#${job.id})",
                    message = "WhatsApp notification for order #${job.id} (${job.customerName}) is still pending to be sent!"
                )
            }
        }
    }

    private fun sendNotification(
        context: Context,
        id: Int,
        channelId: String,
        title: String,
        message: String
    ) {
        // Requirement 7: Suppress notifications when app is actively open/in-foreground
        if (AppLifecycleTracker.isAppInForeground) {
            return
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        try {
            NotificationManagerCompat.from(context).notify(id, builder.build())
        } catch (_: SecurityException) {
        }
    }

    fun showInstantNotification(context: Context, title: String, message: String, channelId: String = CHANNEL_ORDERS) {
        createNotificationChannels(context)
        val id = System.currentTimeMillis().toInt()
        sendNotification(context, id, channelId, title, message)
    }
}
