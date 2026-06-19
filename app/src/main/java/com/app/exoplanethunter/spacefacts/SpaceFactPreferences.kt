package com.app.exoplanethunter.spacefacts

import android.content.Context

/**
 * Persists configuration for the periodic space-fact notification.
 *
 * The notification interval is stored in hours so it can be tuned freely (default 48h /
 * every two days). [lastShownIndex] lets the worker rotate through [SpaceFacts] in order
 * rather than repeating randomly.
 */
class SpaceFactPreferences(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** Whether the periodic fact notification is enabled. */
    var notificationsEnabled: Boolean
        get() = prefs.getBoolean(KEY_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_ENABLED, value).apply()

    /** Notification interval in hours (default [DEFAULT_INTERVAL_HOURS]). */
    var intervalHours: Long
        get() = prefs.getLong(KEY_INTERVAL_HOURS, DEFAULT_INTERVAL_HOURS)
        set(value) = prefs.edit().putLong(KEY_INTERVAL_HOURS, value).apply()

    /** Index of the last fact shown, used to advance to the next one. */
    var lastShownIndex: Int
        get() = prefs.getInt(KEY_LAST_INDEX, -1)
        set(value) = prefs.edit().putInt(KEY_LAST_INDEX, value).apply()

    companion object {
        private const val PREFS_NAME = "space_facts_prefs"
        private const val KEY_ENABLED = "notifications_enabled"
        private const val KEY_INTERVAL_HOURS = "interval_hours"
        private const val KEY_LAST_INDEX = "last_shown_index"

        /** Default cadence: every two days. */
        const val DEFAULT_INTERVAL_HOURS = 48L
    }
}
