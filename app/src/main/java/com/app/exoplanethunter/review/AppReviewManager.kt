package com.app.exoplanethunter.review

import android.app.Activity
import android.util.Log
import com.app.exoplanethunter.BuildConfig
import com.app.exoplanethunter.config.FeatureFlags
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.review.testing.FakeReviewManager

/**
 * Requests the Google Play in-app review dialog once a user has shown sustained engagement
 * (a minimum number of launches and days since first use). Asked at most once.
 *
 * Note: the Play API decides whether the dialog actually appears and is quota-limited, so a
 * successful flow may show nothing. We never gate critical UX on it.
 */
object AppReviewManager {

    private const val TAG = "AppReview"
    private const val MIN_LAUNCHES = 3
    private const val DAY_MILLIS = 24 * 60 * 60 * 1000L

    /** Request a review if engagement thresholds are met and we haven't asked before. */
    fun maybeRequestReview(activity: Activity) {
        if (!FeatureFlags.reviewEnabled) return

        val prefs = ReviewPreferences(activity)
        if (prefs.reviewRequested) return

        val daysSinceFirst = (System.currentTimeMillis() - prefs.firstLaunchMillis) / DAY_MILLIS
        if (prefs.launchCount < MIN_LAUNCHES || daysSinceFirst < FeatureFlags.reviewMinDays) return

        // In debug, FakeReviewManager always completes the flow without showing a real dialog.
        val manager = if (BuildConfig.DEBUG) FakeReviewManager(activity)
        else ReviewManagerFactory.create(activity)

        manager.requestReviewFlow().addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "requestReviewFlow failed: ${task.exception?.message}")
                return@addOnCompleteListener
            }
            // Asked once regardless of outcome — the API never reports whether the user rated.
            prefs.reviewRequested = true
            manager.launchReviewFlow(activity, task.result).addOnCompleteListener {
                Log.d(TAG, "in-app review flow finished")
            }
        }
    }
}
