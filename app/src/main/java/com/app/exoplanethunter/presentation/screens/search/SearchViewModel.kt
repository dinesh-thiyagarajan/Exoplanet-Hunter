package com.app.exoplanethunter.presentation.screens.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.exoplanethunter.analytics.domain.model.AnalyticsEvent
import com.app.exoplanethunter.analytics.domain.usecase.TrackEventUseCase
import com.app.exoplanethunter.exoplanet.domain.model.Exoplanet
import com.app.exoplanethunter.exoplanet.domain.model.StarSystemSummary
import com.app.exoplanethunter.exoplanet.domain.usecase.GetFavoriteNamesUseCase
import com.app.exoplanethunter.exoplanet.domain.usecase.SearchPlanetsUseCase
import com.app.exoplanethunter.exoplanet.domain.usecase.SearchStarSystemsUseCase
import com.app.exoplanethunter.exoplanet.domain.usecase.ToggleFavoriteUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

enum class SearchTab { Planets, Systems }

/** Search across planets and star systems, with the last few queries remembered. */
class SearchViewModel(
    private val searchPlanets: SearchPlanetsUseCase,
    private val searchStarSystems: SearchStarSystemsUseCase,
    getFavoriteNames: GetFavoriteNamesUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val trackEvent: TrackEventUseCase,
    private val recentSearches: RecentSearches,
) : ViewModel() {

    var query by mutableStateOf("")
        private set
    var planets by mutableStateOf<List<Exoplanet>>(emptyList())
        private set
    var systems by mutableStateOf<List<StarSystemSummary>>(emptyList())
        private set
    var tab by mutableStateOf(SearchTab.Planets)
        private set
    var recent by mutableStateOf(recentSearches.load())
        private set
    var favoriteNames by mutableStateOf<Set<String>>(emptySet())
        private set
    /** True once results for the current query have arrived. */
    var hasResults by mutableStateOf(false)
        private set

    private var searchJob: Job? = null

    init {
        viewModelScope.launch { getFavoriteNames().collect { favoriteNames = it } }
    }

    fun onQueryChanged(value: String) {
        query = value
        searchJob?.cancel()
        hasResults = false
        if (value.isBlank()) {
            planets = emptyList(); systems = emptyList()
            return
        }
        searchJob = viewModelScope.launch {
            delay(DEBOUNCE_MILLIS)
            trackEvent(AnalyticsEvent.PlanetSearched(value.trim()))
            combine(searchPlanets(value.trim()), searchStarSystems(value.trim())) { p, s -> p to s }
                .collect { (p, s) ->
                    planets = p
                    systems = s
                    hasResults = true
                    // Jump to star systems when only they match, e.g. "TRAPPIST".
                    if (p.isEmpty() && s.isNotEmpty()) tab = SearchTab.Systems
                    else if (s.isEmpty() && p.isNotEmpty()) tab = SearchTab.Planets
                }
        }
    }

    fun onTabSelected(value: SearchTab) {
        tab = value
    }

    /** Call when the user opens a result, so the query joins the recent list. */
    fun rememberQuery() {
        if (query.isNotBlank()) recent = recentSearches.add(query)
    }

    fun onRecentSelected(value: String) {
        onQueryChanged(value)
    }

    fun clearRecent() {
        recentSearches.clear()
        recent = emptyList()
    }

    fun toggleFavorite(planet: Exoplanet) {
        viewModelScope.launch {
            val isNowFavorite = toggleFavoriteUseCase(planet.planetName)
            trackEvent(
                if (isNowFavorite) AnalyticsEvent.PlanetFavorited(planet.id, planet.planetName)
                else AnalyticsEvent.PlanetUnfavorited(planet.id, planet.planetName)
            )
        }
    }

    private companion object {
        const val DEBOUNCE_MILLIS = 250L
    }
}
