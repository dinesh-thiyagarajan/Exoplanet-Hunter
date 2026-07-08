package com.app.exoplanethunter.ads

import android.content.Context
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object AdManager {

    private var isInitialized = false

    private val _adsEnabled = MutableStateFlow(false)

    /**
     * Whether ads are enabled, as an observable flow so composables react when the
     * value changes at runtime. Ads are off until Remote Config enables them via
     * [setAdsEnabled] (fail-closed default).
     */
    val adsEnabledFlow: StateFlow<Boolean> = _adsEnabled.asStateFlow()

    /** Current snapshot of [adsEnabledFlow] for non-compose callers. */
    val adsEnabled: Boolean
        get() = _adsEnabled.value

    /** The banner ad-unit ID (read from local.properties → BuildConfig). */
    var adUnitId: String = ""
        private set

    /** The interstitial ad-unit ID (read from local.properties → BuildConfig). */
    var interstitialUnitId: String = ""
        private set

    /**
     * Call once from [Application.onCreate] to configure the ad-unit IDs. Ads remain
     * disabled until [setAdsEnabled] is called with `true` (driven by Remote Config).
     *
     * @param unitId          Banner ad-unit ID to use throughout the app.
     * @param interstitialId  Interstitial ad-unit ID (blank disables interstitials).
     */
    fun initialize(
        unitId: String,
        interstitialId: String = ""
    ) {
        adUnitId = unitId
        interstitialUnitId = interstitialId
    }

    /**
     * Enable or disable ads at runtime. Safe to call repeatedly; the AdMob SDK is
     * initialised lazily the first time ads become enabled.
     */
    fun setAdsEnabled(context: Context, enabled: Boolean) {
        _adsEnabled.value = enabled

        if (enabled && !isInitialized) {
            MobileAds.initialize(context.applicationContext) {}
            isInitialized = true
            if (interstitialUnitId.isNotBlank()) {
                InterstitialAdController.preload(context.applicationContext)
            }
        }
    }
}
