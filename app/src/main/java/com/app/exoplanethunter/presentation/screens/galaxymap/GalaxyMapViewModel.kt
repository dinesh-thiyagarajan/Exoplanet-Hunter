package com.app.exoplanethunter.presentation.screens.galaxymap

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.exoplanethunter.analytics.domain.model.AnalyticsEvent
import com.app.exoplanethunter.analytics.domain.usecase.TrackEventUseCase
import com.app.exoplanethunter.exoplanet.domain.model.StarPosition
import com.app.exoplanethunter.exoplanet.domain.usecase.GetNearbyStarPositionsUseCase
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class GalaxyMapViewModel(
    private val getNearbyStarPositions: GetNearbyStarPositionsUseCase,
    private val trackEvent: TrackEventUseCase
) : ViewModel() {

    var stars by mutableStateOf<List<StarPosition>>(emptyList())
        private set

    var isLoading by mutableStateOf(true)
        private set

    init {
        trackEvent(AnalyticsEvent.GalaxyMapScreenViewed)
        viewModelScope.launch {
            getNearbyStarPositions().collectLatest { list ->
                stars = list
                isLoading = false
            }
        }
    }

    fun onStarSelected() {
        trackEvent(AnalyticsEvent.GalaxyMapStarSelected)
    }
}
