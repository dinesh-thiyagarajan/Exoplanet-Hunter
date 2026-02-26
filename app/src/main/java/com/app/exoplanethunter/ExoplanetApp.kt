package com.app.exoplanethunter

import android.app.Application
import com.app.exoplanethunter.ads.AdManager
import com.app.exoplanethunter.di.appModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class ExoplanetApp : Application() {

    override fun onCreate() {
        super.onCreate()

        AdManager.initialize(
            context = this,
            enabled = BuildConfig.ADS_ENABLED,
            unitId = BuildConfig.ADMOB_AD_UNIT_ID
        )

        startKoin {
            androidLogger()
            androidContext(this@ExoplanetApp)
            modules(appModules)
        }
    }
}
