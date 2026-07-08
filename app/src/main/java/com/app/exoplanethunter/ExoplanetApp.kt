package com.app.exoplanethunter

import android.app.Application
import com.app.exoplanethunter.ads.AdManager
import com.app.exoplanethunter.config.RemoteConfigManager
import com.app.exoplanethunter.di.appModules
import com.app.exoplanethunter.spacefacts.SpaceFactNotifier
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class ExoplanetApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Ads stay off until Remote Config enables them ("ads_enabled", default false),
        // applied by RemoteConfigManager below.
        AdManager.initialize(
            unitId = BuildConfig.ADMOB_AD_UNIT_ID,
            interstitialId = BuildConfig.ADMOB_INTERSTITIAL_AD_UNIT_ID
        )

        startKoin {
            androidLogger()
            androidContext(this@ExoplanetApp)
            modules(appModules)
        }

        // Periodic space-fact notifications — interval & on/off driven by Remote Config.
        SpaceFactNotifier.ensureChannel(this)
        RemoteConfigManager.initializeAndApply(this)
    }
}
