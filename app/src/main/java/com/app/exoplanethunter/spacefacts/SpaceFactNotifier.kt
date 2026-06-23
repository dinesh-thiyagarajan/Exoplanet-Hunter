package com.app.exoplanethunter.spacefacts

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
import android.Manifest
import com.app.exoplanethunter.MainActivity
import com.app.exoplanethunter.R

/** Builds the notification channel and posts the periodic space-fact notification. */
object SpaceFactNotifier {

    const val EXTRA_FACT_ID = "extra_space_fact_id"

    private const val CHANNEL_ID = "space_facts"
    private const val NOTIFICATION_ID = 4201

    /** Accent tint for the small icon / app name on the expanded notification (CosmicCyan). */
    private const val NOTIFICATION_ACCENT = 0xFF4DD0E1.toInt()

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.space_fact_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.space_fact_channel_description)
        }
        val manager = context.getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(channel)
    }

    fun show(context: Context, fact: SpaceFact) {
        // Respect the runtime POST_NOTIFICATIONS permission on Android 13+.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        ensureChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_FACT_ID, fact.id)
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            fact.id,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(NOTIFICATION_ACCENT)
            .setContentTitle(fact.title)
            .setContentText(fact.shortDescription)
            .setStyle(NotificationCompat.BigTextStyle().bigText(fact.shortDescription))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }
}
