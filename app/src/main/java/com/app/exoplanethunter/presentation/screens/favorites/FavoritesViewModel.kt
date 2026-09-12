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
        viewModelScope.launch {
            // Everything listed here is already a favorite, so this normally removes it — but log
            // the toggle's actual result rather than assuming, so a double tap can't be miscounted.
            val isNowFavorite = toggleFavoriteUseCase(planet.planetName)
            trackEvent(
                if (isNowFavorite) AnalyticsEvent.PlanetFavorited(planet.id, planet.planetName)
                else AnalyticsEvent.PlanetUnfavorited(planet.id, planet.planetName)
            )
        }
    }
}
