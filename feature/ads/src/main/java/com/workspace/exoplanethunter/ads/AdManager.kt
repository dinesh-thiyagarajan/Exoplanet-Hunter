package com.workspace.exoplanethunter.ads

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

    /**
     * Call once from [Application.onCreate].
     *
     * @param context  Application context.
     * @param enabled  `true` to initialise the AdMob SDK and show ads.
     * @param unitId   Banner ad-unit ID to use throughout the app.
     */
    fun initialize(context: Context, enabled: Boolean, unitId: String) {
        adsEnabled = enabled
        adUnitId = unitId

        if (enabled && !isInitialized) {
            MobileAds.initialize(context) {}
            isInitialized = true
        }
    }
}
