package com.app.exoplanethunter.analytics.domain.repository

import com.app.exoplanethunter.analytics.domain.model.AnalyticsEvent

interface AnalyticsRepository {
    fun track(event: AnalyticsEvent)
}
