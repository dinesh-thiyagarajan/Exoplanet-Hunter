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
import kotlin.math.abs
import kotlin.math.log10

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

    /** Unsorted list as delivered by the active filter/search, before [sortOption] is applied. */
    private var rawPlanets: List<Exoplanet> = emptyList()

    var sortOption by mutableStateOf(SortOption.DEFAULT)
        private set

    // --- Compare mode ---

    var compareMode by mutableStateOf(false)
        private set

    /** Up to two planets selected for comparison while in [compareMode]. */
    var selectedForCompare by mutableStateOf<List<Exoplanet>>(emptyList())
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
                updatePlanets(list)
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
                    updatePlanets(list)
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
                        updatePlanets(list)
                        isLoading = false
                    }
                }
                showLatestOnly -> {
                    filterPlanetsUseCase.latestDiscoveries().collectLatest { list ->
                        updatePlanets(list)
                        isLoading = false
                    }
                }
                minDiscoveryYear != null -> {
                    filterPlanetsUseCase.byMinDiscoveryYear(minDiscoveryYear!!).collectLatest { list ->
                        updatePlanets(list)
                        isLoading = false
                    }
                }
                selectedFilter != null -> {
                    filterPlanetsUseCase.byDiscoveryMethod(selectedFilter!!).collectLatest { list ->
                        updatePlanets(list)
                        isLoading = false
                    }
                }
                else -> {
                    getAllPlanetsUseCase().collectLatest { list ->
                        updatePlanets(list)
                        isLoading = false
                    }
                }
            }
        }
    }

    // -----------------------------------------------------------------------
    // Sorting
    // -----------------------------------------------------------------------

    /** Store the latest unsorted list and publish it through the active [sortOption]. */
    private fun updatePlanets(list: List<Exoplanet>) {
        rawPlanets = list
        planets = sortPlanets(list, sortOption)
    }

    fun onSortSelected(option: SortOption) {
        if (option == sortOption) return
        sortOption = option
        trackEvent(AnalyticsEvent.PlanetSortApplied(sortOption = option.name))
        planets = sortPlanets(rawPlanets, sortOption)
    }

    private fun sortPlanets(list: List<Exoplanet>, option: SortOption): List<Exoplanet> =
        when (option) {
            SortOption.DEFAULT -> list
            SortOption.NEAREST ->
                list.sortedWith(compareBy(nullsLast<Double>()) { it.distanceParsec })
            SortOption.LARGEST ->
                list.sortedByDescending { it.planetRadiusEarth ?: Double.NEGATIVE_INFINITY }
            SortOption.EARTH_LIKE ->
                list.sortedWith(compareBy(nullsLast<Double>()) { earthSimilarity(it) })
            SortOption.NEWEST -> list.sortedByDescending { it.discoveryYear }
            SortOption.NAME_AZ -> list.sortedBy { it.planetName.lowercase() }
        }

    /**
     * Rough "distance from Earth" penalty (lower = more Earth-like) based on radius and
     * equilibrium temperature. Returns null when neither is known so such planets sort last.
     */
    private fun earthSimilarity(planet: Exoplanet): Double? {
        val radius = planet.planetRadiusEarth
        val temp = planet.equilibriumTempK
        if (radius == null && temp == null) return null
        var penalty = 0.0
        if (radius != null && radius > 0.0) penalty += abs(log10(radius))
        if (temp != null) penalty += abs(temp - 255.0) / 255.0
        return penalty
    }

    // -----------------------------------------------------------------------
    // Compare mode
    // -----------------------------------------------------------------------

    fun toggleCompareMode() {
        compareMode = !compareMode
        if (compareMode) {
            trackEvent(AnalyticsEvent.CompareModeEntered)
        } else {
            selectedForCompare = emptyList()
        }
    }

    /** Report a comparison being launched for the two given planets. */
    fun trackComparison(planetA: Exoplanet, planetB: Exoplanet) {
        trackEvent(
            AnalyticsEvent.PlanetsCompared(
                planetAId = planetA.id,
                planetAName = planetA.planetName,
                planetBId = planetB.id,
                planetBName = planetB.planetName
            )
        )
    }

    fun exitCompareMode() {
        compareMode = false
        selectedForCompare = emptyList()
    }

    /** Toggle a planet's membership in the comparison set (capped at two). */
    fun onCompareSelect(planet: Exoplanet) {
        val current = selectedForCompare
        selectedForCompare = when {
            current.any { it.id == planet.id } -> current.filterNot { it.id == planet.id }
            current.size >= 2 -> current // already have two; ignore further taps
            else -> current + planet
        }
    }

    fun isSelectedForCompare(planet: Exoplanet): Boolean =
        selectedForCompare.any { it.id == planet.id }
}
