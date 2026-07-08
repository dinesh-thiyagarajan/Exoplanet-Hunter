package com.app.exoplanethunter.config

import android.content.Context
import com.app.exoplanethunter.BuildConfig
import com.app.exoplanethunter.ads.AdManager
import com.app.exoplanethunter.spacefacts.SpaceFactPreferences
import com.app.exoplanethunter.spacefacts.SpaceFactScheduler
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings

/**
 * Firebase Remote Config gate for the periodic space-fact notification.
 *
 * Server-controlled values:
 *  - [KEY_NOTIFICATIONS_ENABLED] — turn the local notification on/off remotely.
 *  - [KEY_INTERVAL_HOURS] — how many hours between notifications.
 *  - [KEY_COMPARE_ENABLED] — show/hide the planet-compare feature remotely.
 *  - [KEY_ADS_ENABLED] — turn ads on/off remotely (fail-closed: off until the server says on).
 *
 * Notification values are mirrored into [SpaceFactPreferences] so the existing worker/scheduler
 * keep reading a single local source of truth; the compare flag is published to [FeatureFlags]
 * for the UI to observe. Values are refreshed from the server on launch.
 */
object RemoteConfigManager {

    const val KEY_NOTIFICATIONS_ENABLED = "space_fact_notifications_enabled"
    const val KEY_INTERVAL_HOURS = "space_fact_interval_hours"
    const val KEY_COMPARE_ENABLED = "compare_feature_enabled"
    const val KEY_REVIEW_ENABLED = "in_app_review_enabled"
    const val KEY_REVIEW_MIN_DAYS = "in_app_review_min_days"
    const val KEY_ADS_ENABLED = "ads_enabled"

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
                KEY_INTERVAL_HOURS to SpaceFactPreferences.DEFAULT_INTERVAL_HOURS,
                KEY_COMPARE_ENABLED to true,
                KEY_REVIEW_ENABLED to true,
                KEY_REVIEW_MIN_DAYS to 3L,
                KEY_ADS_ENABLED to false
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

        FeatureFlags.setCompareEnabled(remoteConfig.getBoolean(KEY_COMPARE_ENABLED))
        FeatureFlags.reviewEnabled = remoteConfig.getBoolean(KEY_REVIEW_ENABLED)
        FeatureFlags.reviewMinDays = remoteConfig.getLong(KEY_REVIEW_MIN_DAYS).toInt().coerceAtLeast(0)

        // Fail-closed: ads stay off (in-app default false) until the server enables them.
        // A cached activated value applies immediately on later launches, before any fetch.
        AdManager.setAdsEnabled(context, remoteConfig.getBoolean(KEY_ADS_ENABLED))
    }
}
