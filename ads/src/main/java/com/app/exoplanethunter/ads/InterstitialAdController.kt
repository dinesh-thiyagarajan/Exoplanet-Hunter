package com.app.exoplanethunter.ads

import android.app.Activity
import android.content.Context
import android.os.SystemClock
import android.util.Log
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback

/**
 * Loads and shows AdMob interstitial ads with a simple time-based frequency cap so
 * users aren't bombarded. An interstitial is shown only at deliberate transition points
 * (e.g. before opening the planet comparison) and at most once per [MIN_INTERVAL_MS].
 *
 * If ads are disabled, no interstitial unit is configured, or none is loaded/within the
 * cooldown window, [maybeShow] falls through and simply invokes the continuation.
 */
object InterstitialAdController {

    private const val TAG = "InterstitialAd"

    /** Minimum gap between two interstitials (3 minutes). */
    private const val MIN_INTERVAL_MS = 3 * 60 * 1000L

    private var ad: InterstitialAd? = null
    private var isLoading = false
    private var lastShownElapsed = 0L

    /** Preload an interstitial so it's ready when [maybeShow] is called. */
    fun preload(context: Context) {
        if (!AdManager.adsEnabled) {
            Log.d(TAG, "preload skipped: ads disabled")
            return
        }
        val unitId = AdManager.interstitialUnitId
        if (unitId.isBlank()) {
            Log.d(TAG, "preload skipped: no interstitial unit id configured")
            return
        }
        if (ad != null || isLoading) {
            Log.d(TAG, "preload skipped: already loaded=${ad != null} loading=$isLoading")
            return
        }

        Log.d(TAG, "preload: requesting interstitial for unit $unitId")
        isLoading = true
        InterstitialAd.load(
            context.applicationContext,
            unitId,
            AdRequest.Builder().build(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(loaded: InterstitialAd) {
                    Log.d(TAG, "onAdLoaded: interstitial ready")
                    ad = loaded
                    isLoading = false
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    Log.w(TAG, "onAdFailedToLoad: code=${error.code} message=${error.message}")
                    ad = null
                    isLoading = false
                }
            }
        )
    }

    /**
     * Show an interstitial if one is ready and the cooldown has elapsed, otherwise proceed
     * immediately. [onContinue] is always invoked exactly once — after the ad is dismissed,
     * or right away when no ad is shown.
     */
    fun maybeShow(activity: Activity?, onContinue: () -> Unit) {
        val current = ad
        val now = SystemClock.elapsedRealtime()
        val withinCooldown = lastShownElapsed != 0L && now - lastShownElapsed < MIN_INTERVAL_MS

        if (activity == null || !AdManager.adsEnabled || current == null || withinCooldown) {
            Log.d(
                TAG,
                "maybeShow: skipping (activity=${activity != null}, " +
                    "adsEnabled=${AdManager.adsEnabled}, adReady=${current != null}, " +
                    "withinCooldown=$withinCooldown) -> continuing without ad"
            )
            // Nothing to show right now — keep one warming up for next time.
            if (activity != null) preload(activity)
            onContinue()
            return
        }

        Log.d(TAG, "maybeShow: showing interstitial")

        var continued = false
        val proceedOnce = {
            if (!continued) {
                continued = true
                onContinue()
            }
        }

        current.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                ad = null
                lastShownElapsed = SystemClock.elapsedRealtime()
                preload(activity)
                proceedOnce()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                ad = null
                preload(activity)
                proceedOnce()
            }
        }
        current.show(activity)
    }
}
