package com.app.exoplanethunter.presentation.screens.spacefact

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.app.exoplanethunter.analytics.domain.model.AnalyticsEvent
import com.app.exoplanethunter.analytics.domain.usecase.TrackEventUseCase
import com.app.exoplanethunter.spacefacts.SpaceFact
import com.app.exoplanethunter.spacefacts.SpaceFacts

class SpaceFactDetailViewModel(
    private val trackEvent: TrackEventUseCase
) : ViewModel() {

    var fact by mutableStateOf<SpaceFact?>(null)
        private set

    private var openTracked = false

    fun load(factId: Int) {
        val loaded = SpaceFacts.byId(factId)
        fact = loaded
        if (loaded != null && !openTracked) {
            openTracked = true
            trackEvent(AnalyticsEvent.SpaceFactOpened(loaded.id, loaded.title))
        }
    }

    fun onSourceOpened() {
        fact?.let { trackEvent(AnalyticsEvent.SpaceFactSourceOpened(it.id, it.title)) }
    }
}
