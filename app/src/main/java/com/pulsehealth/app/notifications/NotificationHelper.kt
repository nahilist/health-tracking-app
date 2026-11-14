package com.pulsehealth.app.notifications

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
import com.pulsehealth.app.MainActivity
import com.pulsehealth.app.R
import java.util.Locale

object NotificationHelper {

    const val CHANNEL_ID = "pulse_hydration_reminders"
    const val NOTIFICATION_ID = 1001
    const val ACTION_QUICK_ADD_WATER = "com.pulsehealth.app.ACTION_QUICK_ADD_WATER"
    const val EXTRA_AMOUNT_ML = "extra_amount_ml"

    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Hydration Reminders"
            val descriptionText = "Reminders to log and drink water throughout the day"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            notificationManager?.createNotificationChannel(channel)
        }
    }

    fun showHydrationReminder(
        context: Context,
        currentWaterLiters: Double,
        goalWaterLiters: Double
    ) {
        createNotificationChannel(context)

        // Verify notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                return
            }
        }

        // Open app intent
        val contentIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            0,
            contentIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Action: Quick Add 250ml
        val quickAddIntent = Intent(context, HydrationActionReceiver::class.java).apply {
            action = ACTION_QUICK_ADD_WATER
            putExtra(EXTRA_AMOUNT_ML, 250)
        }
        val quickAddPendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            quickAddIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val formattedCurrent = String.format(Locale.getDefault(), "%.1f", currentWaterLiters)
        val formattedGoal = String.format(Locale.getDefault(), "%.1f", goalWaterLiters)

        val message = if (currentWaterLiters < goalWaterLiters) {
            "You've logged ${formattedCurrent}L of your ${formattedGoal}L daily goal. Drink a glass of water!"
        } else {
            "Great job! You've reached your daily goal of ${formattedGoal}L. Keep staying refreshed!"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_water)
            .setContentTitle("Hydration Check-in")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .addAction(
                R.drawable.ic_notification_water,
                "+250 ml",
                quickAddPendingIntent
            )
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    fun cancelReminder(context: Context) {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
    }
}
