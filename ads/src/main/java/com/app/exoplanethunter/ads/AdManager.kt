package com.app.exoplanethunter.ads

import android.content.Context
import com.google.android.gms.ads.MobileAds

object AdManager {

    private var isInitialized = false

    /** Whether ads are enabled (read from local.properties → BuildConfig). */
    var adsEnabled: Boolean = false
        private set

    /** The banner ad-unit ID (read from local.properties → BuildConfig). */
    var adUnitId: String = ""
        private set

    /** The interstitial ad-unit ID (read from local.properties → BuildConfig). */
    var interstitialUnitId: String = ""
        private set

    /**
     * Call once from [Application.onCreate].
     *
     * @param context         Application context.
     * @param enabled         `true` to initialise the AdMob SDK and show ads.
     * @param unitId          Banner ad-unit ID to use throughout the app.
     * @param interstitialId  Interstitial ad-unit ID (blank disables interstitials).
     */
    fun initialize(
        context: Context,
        enabled: Boolean,
        unitId: String,
        interstitialId: String = ""
    ) {
        adsEnabled = enabled
        adUnitId = unitId
        interstitialUnitId = interstitialId

        if (enabled && !isInitialized) {
            MobileAds.initialize(context) {}
            isInitialized = true
            if (interstitialId.isNotBlank()) {
                InterstitialAdController.preload(context)
            }
        }
    }
}
