package com.app.exoplanethunter.review

import android.content.Context

/** Tracks usage signals used to decide when to show the in-app review prompt. */
class ReviewPreferences(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** Epoch millis of the first app launch (0 until set). */
    var firstLaunchMillis: Long
        get() = prefs.getLong(KEY_FIRST_LAUNCH, 0L)
        set(value) = prefs.edit().putLong(KEY_FIRST_LAUNCH, value).apply()

    /** Number of times the app has been launched. */
    var launchCount: Int
        get() = prefs.getInt(KEY_LAUNCH_COUNT, 0)
        set(value) = prefs.edit().putInt(KEY_LAUNCH_COUNT, value).apply()

    /**
     * Epoch millis of the last review request (0 if never). Installs from before the 60-day
     * rule only stored "asked once"; for them the clock starts on first read, so they aren't
     * asked again straight after updating.
     */
    var lastRequestedMillis: Long
        get() {
            val stored = prefs.getLong(KEY_LAST_REQUESTED, 0L)
            if (stored == 0L && prefs.getBoolean(KEY_REVIEW_REQUESTED, false)) {
                val now = System.currentTimeMillis()
                prefs.edit().putLong(KEY_LAST_REQUESTED, now).remove(KEY_REVIEW_REQUESTED).apply()
                return now
            }
            return stored
        }
        set(value) = prefs.edit().putLong(KEY_LAST_REQUESTED, value).apply()

    /** Record an app launch; stamps the first-launch time on the very first call. */
    fun recordLaunch() {
        if (firstLaunchMillis == 0L) firstLaunchMillis = System.currentTimeMillis()
        launchCount += 1
    }

    companion object {
        private const val PREFS_NAME = "review_prefs"
        private const val KEY_FIRST_LAUNCH = "first_launch_millis"
        private const val KEY_LAUNCH_COUNT = "launch_count"
        private const val KEY_REVIEW_REQUESTED = "review_requested" // legacy, read once to migrate
        private const val KEY_LAST_REQUESTED = "last_requested_millis"
    }
}
