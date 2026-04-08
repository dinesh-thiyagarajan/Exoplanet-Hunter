package com.app.exoplanethunter.analytics.domain.usecase

import com.app.exoplanethunter.analytics.domain.model.AnalyticsEvent
import com.app.exoplanethunter.analytics.domain.repository.AnalyticsRepository

class TrackEventUseCase(private val repository: AnalyticsRepository) {
    operator fun invoke(event: AnalyticsEvent) = repository.track(event)
}
