package com.app.exoplanethunter.discovery

import android.content.Context

/**
 * Persists configuration for the background "new worlds confirmed" check.
 *
 * Mirrors [com.app.exoplanethunter.spacefacts.SpaceFactPreferences]: a user-facing toggle plus a
 * remote kill switch, so notifications can be disabled server-side without an app update.
 */
class DiscoveryPreferences(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** Server-side kill switch, mirrored from Remote Config on each launch. */
    var remoteDiscoveryEnabled: Boolean
        get() = prefs.getBoolean(KEY_REMOTE_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_REMOTE_ENABLED, value).apply()

    /** The user's own in-app toggle (Settings screen). Never touched by Remote Config. */
    var userDiscoveryEnabled: Boolean
        get() = prefs.getBoolean(KEY_USER_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_USER_ENABLED, value).apply()

    /** Effective state: checks run only when the user opted in AND the remote switch is on. */
    val discoveryEnabled: Boolean
        get() = remoteDiscoveryEnabled && userDiscoveryEnabled

    /** How often the background catalog check runs, in hours. */
    var intervalHours: Long
        get() = prefs.getLong(KEY_INTERVAL_HOURS, DEFAULT_INTERVAL_HOURS)
        set(value) = prefs.edit().putLong(KEY_INTERVAL_HOURS, value).apply()

    companion object {
        private const val PREFS_NAME = "discovery_prefs"
        private const val KEY_REMOTE_ENABLED = "remote_discovery_enabled"
        private const val KEY_USER_ENABLED = "user_discovery_enabled"
        private const val KEY_INTERVAL_HOURS = "discovery_interval_hours"

        /** Check the archive once a day; NASA confirms planets in irregular batches. */
        const val DEFAULT_INTERVAL_HOURS = 24L

        /**
         * Delay before the first background check on a fresh install. The bundled catalog snapshot
         * is already current enough at install time, and this keeps us off the network during the
         * user's first session.
         */
        const val FIRST_RUN_DELAY_HOURS = 12L

        /**
         * Above this many new planets the sync is a catch-up against a stale bundled snapshot
         * rather than a genuine batch of fresh confirmations, so the copy is worded differently.
         */
        const val BULK_UPDATE_THRESHOLD = 25
    }
}
