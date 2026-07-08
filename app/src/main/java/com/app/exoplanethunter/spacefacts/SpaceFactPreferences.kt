package com.app.exoplanethunter.spacefacts

import android.content.Context

/**
 * Persists configuration for the periodic space-fact notification.
 *
 * The notification interval is stored in hours so it can be tuned freely (default 48h /
 * every two days). [shownFactIds] records which facts have already been notified so the
 * worker never repeats one until every fact has been shown, then the cycle resets.
 */
class SpaceFactPreferences(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** Server-side kill switch, mirrored from Remote Config on each launch. */
    var remoteNotificationsEnabled: Boolean
        get() = prefs.getBoolean(KEY_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_ENABLED, value).apply()

    /** The user's own in-app toggle (Settings screen). Never touched by Remote Config. */
    var userNotificationsEnabled: Boolean
        get() = prefs.getBoolean(KEY_USER_ENABLED, true)
        set(value) = prefs.edit().putBoolean(KEY_USER_ENABLED, value).apply()

    /** Effective state: notifications fire only when the user opted in AND the remote switch is on. */
    val notificationsEnabled: Boolean
        get() = remoteNotificationsEnabled && userNotificationsEnabled

    /** Notification interval in hours (default [DEFAULT_INTERVAL_HOURS]). */
    var intervalHours: Long
        get() = prefs.getLong(KEY_INTERVAL_HOURS, DEFAULT_INTERVAL_HOURS)
        set(value) = prefs.edit().putLong(KEY_INTERVAL_HOURS, value).apply()

    /** Ids of facts already shown in the current cycle. */
    val shownFactIds: Set<Int>
        get() = prefs.getStringSet(KEY_SHOWN_IDS, emptySet())
            .orEmpty()
            .mapNotNull { it.toIntOrNull() }
            .toSet()

    /** The most recently shown fact id, or -1 if none yet. */
    var lastShownId: Int
        get() = prefs.getInt(KEY_LAST_SHOWN_ID, -1)
        set(value) = prefs.edit().putInt(KEY_LAST_SHOWN_ID, value).apply()

    /** Record a fact id as shown in the current cycle. */
    fun markShown(id: Int) {
        val updated = shownFactIds.map { it.toString() }.toMutableSet()
        updated.add(id.toString())
        prefs.edit().putStringSet(KEY_SHOWN_IDS, updated).apply()
        lastShownId = id
    }

    /** Start a fresh cycle (clears the shown-id history). */
    fun resetShown() {
        prefs.edit().putStringSet(KEY_SHOWN_IDS, emptySet()).apply()
    }

    companion object {
        private const val PREFS_NAME = "space_facts_prefs"
        private const val KEY_ENABLED = "notifications_enabled"
        private const val KEY_USER_ENABLED = "user_notifications_enabled"
        private const val KEY_INTERVAL_HOURS = "interval_hours"
        private const val KEY_SHOWN_IDS = "shown_fact_ids"
        private const val KEY_LAST_SHOWN_ID = "last_shown_id"

        /** Default cadence: every two days. */
        const val DEFAULT_INTERVAL_HOURS = 48L
    }
}
