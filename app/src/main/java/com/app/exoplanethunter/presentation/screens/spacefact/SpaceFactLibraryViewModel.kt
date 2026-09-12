package com.app.exoplanethunter.presentation.screens.spacefact

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.app.exoplanethunter.analytics.domain.model.AnalyticsEvent
import com.app.exoplanethunter.analytics.domain.usecase.TrackEventUseCase
import com.app.exoplanethunter.spacefacts.SpaceFact
import com.app.exoplanethunter.spacefacts.SpaceFactProvider

/**
 * Backs the browsable space-fact library.
 *
 * The catalog of facts previously existed only behind a periodic notification, so most users never
 * saw any of it. This exposes the whole set in-app, with a simple case-insensitive filter over
 * title and teaser.
 */
class SpaceFactLibraryViewModel(
    spaceFactProvider: SpaceFactProvider,
    trackEvent: TrackEventUseCase
) : ViewModel() {

    private val allFacts: List<SpaceFact> = spaceFactProvider.all()

    var query by mutableStateOf("")
        private set

    val totalCount: Int = allFacts.size

    val facts: List<SpaceFact>
        get() {
            val q = query.trim()
            if (q.isEmpty()) return allFacts
            return allFacts.filter {
                it.title.contains(q, ignoreCase = true) ||
                    it.shortDescription.contains(q, ignoreCase = true)
            }
        }

    init {
        trackEvent(AnalyticsEvent.SpaceFactLibraryViewed)
    }

    fun onQueryChange(value: String) {
        query = value
    }
}
