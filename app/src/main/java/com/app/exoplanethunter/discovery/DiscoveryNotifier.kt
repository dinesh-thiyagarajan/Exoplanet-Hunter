package com.app.exoplanethunter.discovery

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
import com.app.exoplanethunter.MainActivity
import com.app.exoplanethunter.R

/** Builds the notification channel and posts the "new worlds" notification. */
object DiscoveryNotifier {

    private const val CHANNEL_ID = "new_discoveries"
    private const val NOTIFICATION_ID = 4202

    /** Accent tint for the small icon / app name on the expanded notification (CosmicCyan). */
    private const val NOTIFICATION_ACCENT = 0xFF4DD0E1.toInt()

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.discovery_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = context.getString(R.string.discovery_channel_description)
        }
        context.getSystemService(NotificationManager::class.java)
            ?.createNotificationChannel(channel)
    }

    /**
     * Post a notification for [newPlanetNames].
     *
     * Copy is deliberately accurate: a handful of additions really are freshly confirmed worlds,
     * but a large batch means the local catalog was simply behind, so we say "added" rather than
     * implying NASA confirmed hundreds of planets overnight.
     */
    fun show(context: Context, newPlanetNames: List<String>) {
        if (newPlanetNames.isEmpty()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        ensureChannel(context)

        val count = newPlanetNames.size
        val isBulk = count > DiscoveryPreferences.BULK_UPDATE_THRESHOLD

        val title = when {
            isBulk -> context.getString(R.string.discovery_title_bulk, count)
            count == 1 -> context.getString(R.string.discovery_title_single)
            else -> context.getString(R.string.discovery_title_multiple, count)
        }

        // Name a few of the new worlds — concrete names are far more enticing than a bare count.
        val sample = newPlanetNames.take(3).joinToString(", ")
        val body = when {
            count == 1 -> newPlanetNames.first()
            count <= 3 -> sample
            else -> context.getString(R.string.discovery_body_more, sample, count - 3)
        }

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            NOTIFICATION_ID,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setColor(NOTIFICATION_ACCENT)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }
}
