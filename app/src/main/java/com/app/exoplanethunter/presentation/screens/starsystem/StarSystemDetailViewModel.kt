package com.app.exoplanethunter.presentation.screens.starsystem

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.exoplanethunter.analytics.domain.model.AnalyticsEvent
import com.app.exoplanethunter.analytics.domain.usecase.TrackEventUseCase
import com.app.exoplanethunter.exoplanet.domain.model.StarSystem
import com.app.exoplanethunter.exoplanet.domain.usecase.GetStarSystemUseCase
import kotlinx.coroutines.launch

class StarSystemDetailViewModel(
    private val getStarSystemUseCase: GetStarSystemUseCase,
    private val trackEvent: TrackEventUseCase
) : ViewModel() {

    var starSystem by mutableStateOf<StarSystem?>(null)
        private set

    var isLoading by mutableStateOf(true)
        private set

    fun loadSystem(hostName: String) {
        viewModelScope.launch {
            isLoading = true
            starSystem = getStarSystemUseCase(hostName)
            trackEvent(AnalyticsEvent.StarSystemDetailScreenViewed(hostName = hostName))
            isLoading = false
        }
    }
}
