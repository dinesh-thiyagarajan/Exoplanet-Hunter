package com.app.exoplanethunter.presentation.screens.favorites

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.exoplanethunter.analytics.domain.model.AnalyticsEvent
import com.app.exoplanethunter.analytics.domain.usecase.TrackEventUseCase
import com.app.exoplanethunter.exoplanet.domain.model.Exoplanet
import com.app.exoplanethunter.exoplanet.domain.usecase.GetFavoritePlanetsUseCase
import com.app.exoplanethunter.exoplanet.domain.usecase.ToggleFavoriteUseCase
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val getFavoritePlanetsUseCase: GetFavoritePlanetsUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val trackEvent: TrackEventUseCase
) : ViewModel() {

    var planets by mutableStateOf<List<Exoplanet>>(emptyList())
        private set

    var isLoading by mutableStateOf(true)
        private set

    init {
        trackEvent(AnalyticsEvent.FavoritesScreenViewed)
        loadFavorites()
    }

    private fun loadFavorites() {
        viewModelScope.launch {
            getFavoritePlanetsUseCase().collectLatest { list ->
                planets = list
                isLoading = false
            }
        }
    }

    fun toggleFavorite(planet: Exoplanet) {
        // Everything shown here is already a favorite, so this always removes it.
        trackEvent(AnalyticsEvent.PlanetUnfavorited(planet.id, planet.planetName))
        viewModelScope.launch {
            toggleFavoriteUseCase(planet.planetName)
        }
    }
}
