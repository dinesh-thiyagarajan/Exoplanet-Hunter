package com.app.exoplanethunter.presentation.screens.planetlist

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.exoplanethunter.analytics.domain.model.AnalyticsEvent
import com.app.exoplanethunter.analytics.domain.usecase.TrackEventUseCase
import com.app.exoplanethunter.exoplanet.domain.model.Exoplanet
import com.app.exoplanethunter.exoplanet.domain.usecase.FilterPlanetsUseCase
import com.app.exoplanethunter.exoplanet.domain.usecase.GetAllPlanetsUseCase
import com.app.exoplanethunter.exoplanet.domain.usecase.GetDiscoveryMethodsUseCase
import com.app.exoplanethunter.exoplanet.domain.usecase.GetFavoriteNamesUseCase
import com.app.exoplanethunter.exoplanet.domain.usecase.SearchPlanetsUseCase
import com.app.exoplanethunter.exoplanet.domain.usecase.ToggleFavoriteUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PlanetListViewModel(
    private val getAllPlanetsUseCase: GetAllPlanetsUseCase,
    private val searchPlanetsUseCase: SearchPlanetsUseCase,
    private val filterPlanetsUseCase: FilterPlanetsUseCase,
    private val getDiscoveryMethodsUseCase: GetDiscoveryMethodsUseCase,
    private val getFavoriteNamesUseCase: GetFavoriteNamesUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val trackEvent: TrackEventUseCase
) : ViewModel() {

    var favoriteNames by mutableStateOf<Set<String>>(emptySet())
        private set

    var planets by mutableStateOf<List<Exoplanet>>(emptyList())
        private set

    var isLoading by mutableStateOf(true)
        private set

    var searchQuery by mutableStateOf("")
        private set

    var discoveryMethods by mutableStateOf<List<String>>(emptyList())
        private set

    var selectedFilter by mutableStateOf<String?>(null)
        private set

    var showHabitableOnly by mutableStateOf(false)
        private set

    var showLatestOnly by mutableStateOf(false)
        private set

    var minDiscoveryYear by mutableStateOf<Int?>(null)
        private set

    private var searchJob: Job? = null

    init {
        trackEvent(AnalyticsEvent.PlanetListScreenViewed)
        loadPlanets()
        loadFilters()
        observeFavorites()
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            getFavoriteNamesUseCase().collectLatest { favoriteNames = it }
        }
    }

    fun toggleFavorite(planet: Exoplanet) {
        val wasFavorite = planet.planetName in favoriteNames
        trackEvent(
            if (wasFavorite) AnalyticsEvent.PlanetUnfavorited(planet.id, planet.planetName)
            else AnalyticsEvent.PlanetFavorited(planet.id, planet.planetName)
        )
        viewModelScope.launch {
            toggleFavoriteUseCase(planet.planetName)
        }
    }

    private fun loadPlanets() {
        viewModelScope.launch {
            isLoading = true
            getAllPlanetsUseCase().collectLatest { list ->
                planets = list
                isLoading = false
            }
        }
    }

    private fun loadFilters() {
        viewModelScope.launch {
            discoveryMethods = getDiscoveryMethodsUseCase()
        }
    }

    fun onSearchQueryChanged(query: String) {
        searchQuery = query
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300) // debounce
            if (query.isBlank()) {
                applyCurrentFilter()
            } else {
                trackEvent(AnalyticsEvent.PlanetSearched(query))
                searchPlanetsUseCase(query).collectLatest { list ->
                    planets = list
                }
            }
        }
    }

    fun onFilterSelected(method: String?) {
        // Only a no-op if this exact discovery-method filter is active AND no other
        // filter (habitable / latest / year) is overriding the list. Otherwise tapping
        // "All" (method == null) while Habitable is active would early-return and do nothing.
        val alreadyActive = selectedFilter == method &&
            !showHabitableOnly && !showLatestOnly && minDiscoveryYear == null
        if (alreadyActive) return
        selectedFilter = method
        showHabitableOnly = false
        showLatestOnly = false
        minDiscoveryYear = null
        searchQuery = ""
        trackEvent(
            AnalyticsEvent.PlanetFilterApplied(
                filterType = "discovery_method",
                filterValue = method ?: "all"
            )
        )
        applyCurrentFilter()
    }

    fun onToggleHabitable() {
        showHabitableOnly = !showHabitableOnly
        selectedFilter = null
        searchQuery = ""
        trackEvent(
            AnalyticsEvent.PlanetFilterApplied(
                filterType = "habitable",
                filterValue = showHabitableOnly.toString()
            )
        )
        applyCurrentFilter()
    }

    fun onToggleLatest() {
        showLatestOnly = !showLatestOnly
        selectedFilter = null
        showHabitableOnly = false
        minDiscoveryYear = null
        searchQuery = ""
        trackEvent(
            AnalyticsEvent.PlanetFilterApplied(
                filterType = "latest_discoveries",
                filterValue = showLatestOnly.toString()
            )
        )
        applyCurrentFilter()
    }

    fun onMinYearChanged(year: Int?) {
        minDiscoveryYear = year
        selectedFilter = null
        showHabitableOnly = false
        showLatestOnly = false
        searchQuery = ""
        trackEvent(
            AnalyticsEvent.PlanetFilterApplied(
                filterType = "min_discovery_year",
                filterValue = year?.toString() ?: "all"
            )
        )
        applyCurrentFilter()
    }

    fun trackPlanetClicked(planet: Exoplanet) {
        trackEvent(
            AnalyticsEvent.PlanetClicked(
                planetId = planet.id,
                planetName = planet.planetName,
                discoveryMethod = planet.discoveryMethod
            )
        )
    }

    private fun applyCurrentFilter() {
        viewModelScope.launch {
            isLoading = true
            when {
                showHabitableOnly -> {
                    filterPlanetsUseCase.mostHabitable(50).collectLatest { list ->
                        planets = list
                        isLoading = false
                    }
                }
                showLatestOnly -> {
                    filterPlanetsUseCase.latestDiscoveries().collectLatest { list ->
                        planets = list
                        isLoading = false
                    }
                }
                minDiscoveryYear != null -> {
                    filterPlanetsUseCase.byMinDiscoveryYear(minDiscoveryYear!!).collectLatest { list ->
                        planets = list
                        isLoading = false
                    }
                }
                selectedFilter != null -> {
                    filterPlanetsUseCase.byDiscoveryMethod(selectedFilter!!).collectLatest { list ->
                        planets = list
                        isLoading = false
                    }
                }
                else -> {
                    getAllPlanetsUseCase().collectLatest { list ->
                        planets = list
                        isLoading = false
                    }
                }
            }
        }
    }
}
