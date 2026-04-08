package com.app.exoplanethunter.analytics.di

import com.app.exoplanethunter.analytics.data.repository.FirebaseAnalyticsRepository
import com.app.exoplanethunter.analytics.domain.repository.AnalyticsRepository
import com.app.exoplanethunter.analytics.domain.usecase.TrackEventUseCase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val analyticsModule = module {
    single<AnalyticsRepository> { FirebaseAnalyticsRepository(androidContext()) }
    factory { TrackEventUseCase(get()) }
}
