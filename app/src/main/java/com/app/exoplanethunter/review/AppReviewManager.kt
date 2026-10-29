package com.app.exoplanethunter.review

import android.app.Activity
import android.util.Log
import com.app.exoplanethunter.BuildConfig
import com.app.exoplanethunter.config.FeatureFlags
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.review.testing.FakeReviewManager

/**
 * Requests the Google Play in-app review dialog once a user has shown sustained engagement
 * (a few launches and [FeatureFlags.reviewIntervalDays] of use, 60 by default), then again at
 * most once every [FeatureFlags.reviewIntervalDays].
 *
 * Note: the Play API decides whether the dialog actually appears and is quota-limited, so a
 * successful flow may show nothing. We never gate critical UX on it.
 */
object AppReviewManager {

    private const val TAG = "AppReview"
    private const val MIN_LAUNCHES = 3
    private const val DAY_MILLIS = 24 * 60 * 60 * 1000L

    /** Set when another prompt (an app update) was shown this session; then we don't ask. */
    @Volatile
    private var heldThisSession = false

    fun holdForThisSession() {
        heldThisSession = true
    }

    /**
     * Request a review if engagement thresholds are met and we haven't asked recently. Called
     * from the planet detail screen once someone has spent a moment with a planet.
     */
    fun maybeRequestReview(activity: Activity) {
        if (!FeatureFlags.reviewEnabled || heldThisSession) return

        val prefs = ReviewPreferences(activity)
        val due = isReviewDue(
            launchCount = prefs.launchCount,
            firstLaunchMillis = prefs.firstLaunchMillis,
            lastRequestedMillis = prefs.lastRequestedMillis,
            nowMillis = System.currentTimeMillis(),
            intervalDays = FeatureFlags.reviewIntervalDays,
        )
        if (!due) return

        // In debug, FakeReviewManager always completes the flow without showing a real dialog.
        val manager = if (BuildConfig.DEBUG) FakeReviewManager(activity)
        else ReviewManagerFactory.create(activity)

        manager.requestReviewFlow().addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "requestReviewFlow failed: ${task.exception?.message}")
                return@addOnCompleteListener
            }
            // Counts as asked regardless of outcome — the API never reports whether the user rated.
            prefs.lastRequestedMillis = System.currentTimeMillis()
            manager.launchReviewFlow(activity, task.result).addOnCompleteListener {
                Log.d(TAG, "in-app review flow finished")
            }
        }
    }

    /**
     * Whether to ask now: at least [MIN_LAUNCHES] launches and [intervalDays] since install, and
     * either never asked or the last request was at least [intervalDays] ago. Pure, so it's
     * unit-tested.
     */
    fun isReviewDue(
        launchCount: Int,
        firstLaunchMillis: Long,
        lastRequestedMillis: Long,
        nowMillis: Long,
        intervalDays: Int,
    ): Boolean {
        val daysSinceFirst = (nowMillis - firstLaunchMillis) / DAY_MILLIS
        if (launchCount < MIN_LAUNCHES || daysSinceFirst < intervalDays) return false
        if (lastRequestedMillis == 0L) return true
        return (nowMillis - lastRequestedMillis) / DAY_MILLIS >= intervalDays
    }
}
