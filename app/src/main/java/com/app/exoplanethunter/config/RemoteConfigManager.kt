package com.app.exoplanethunter.config

import android.content.Context
import com.app.exoplanethunter.BuildConfig
import com.app.exoplanethunter.spacefacts.SpaceFactPreferences
import com.app.exoplanethunter.spacefacts.SpaceFactScheduler
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings

/**
 * Firebase Remote Config gate for the periodic space-fact notification.
 *
 * Two server-controlled values:
 *  - [KEY_NOTIFICATIONS_ENABLED] — turn the local notification on/off remotely.
 *  - [KEY_INTERVAL_HOURS] — how many hours between notifications.
 *
 * Remote values are mirrored into [SpaceFactPreferences] so the existing worker/scheduler
 * keep reading a single local source of truth; we just refresh it from the server on launch.
 */
object RemoteConfigManager {

    const val KEY_NOTIFICATIONS_ENABLED = "space_fact_notifications_enabled"
    const val KEY_INTERVAL_HOURS = "space_fact_interval_hours"

    // Minimum sane interval (also respects WorkManager's 15-minute periodic floor).
    private const val MIN_INTERVAL_HOURS = 1L

    fun initializeAndApply(context: Context) {
        val remoteConfig = FirebaseRemoteConfig.getInstance()

        val settings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(if (BuildConfig.DEBUG) 0 else 3600)
            .build()
        remoteConfig.setConfigSettingsAsync(settings)

        remoteConfig.setDefaultsAsync(
            mapOf(
                KEY_NOTIFICATIONS_ENABLED to true,
                KEY_INTERVAL_HOURS to SpaceFactPreferences.DEFAULT_INTERVAL_HOURS
            )
        )

        // Apply immediately from defaults / last activated values so scheduling isn't blocked
        // on the network, then re-apply once fresh server values are fetched.
        applyToPrefs(context, remoteConfig)
        remoteConfig.fetchAndActivate().addOnCompleteListener {
            applyToPrefs(context, remoteConfig)
        }
    }

    private fun applyToPrefs(context: Context, remoteConfig: FirebaseRemoteConfig) {
        val prefs = SpaceFactPreferences(context)
        prefs.notificationsEnabled = remoteConfig.getBoolean(KEY_NOTIFICATIONS_ENABLED)
        prefs.intervalHours = remoteConfig.getLong(KEY_INTERVAL_HOURS)
            .coerceAtLeast(MIN_INTERVAL_HOURS)
        SpaceFactScheduler.schedule(context)
    }
}
