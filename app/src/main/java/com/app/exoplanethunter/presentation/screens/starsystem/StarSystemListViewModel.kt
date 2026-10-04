package com.app.exoplanethunter.presentation.screens.starsystem

import androidx.annotation.StringRes
import com.app.exoplanethunter.R
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.exoplanethunter.analytics.domain.model.AnalyticsEvent
import com.app.exoplanethunter.analytics.domain.usecase.TrackEventUseCase
import com.app.exoplanethunter.exoplanet.domain.model.StarSystemSummary
import com.app.exoplanethunter.exoplanet.domain.usecase.GetAllStarSystemsUseCase
import com.app.exoplanethunter.exoplanet.domain.usecase.GetMultiPlanetSystemsUseCase
import com.app.exoplanethunter.exoplanet.domain.usecase.GetStarSystemsByStarCountUseCase
import com.app.exoplanethunter.exoplanet.domain.usecase.SearchStarSystemsUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/** Filters available on the Star System list screen. */
enum class StarSystemFilter(@StringRes val labelRes: Int) {
    All(R.string.filter_all),
    SingleStar(R.string.star_system_filter_single),
    Binary(R.string.star_system_multiplicity_binary),
    Trinary(R.string.star_system_multiplicity_trinary),
    MultiPlanet(R.string.star_system_filter_multi)
}

class StarSystemListViewModel(
    private val getAllStarSystemsUseCase: GetAllStarSystemsUseCase,
    private val searchStarSystemsUseCase: SearchStarSystemsUseCase,
    private val getMultiPlanetSystemsUseCase: GetMultiPlanetSystemsUseCase,
    private val getStarSystemsByStarCountUseCase: GetStarSystemsByStarCountUseCase,
    private val trackEvent: TrackEventUseCase
) : ViewModel() {

    var starSystems by mutableStateOf<List<StarSystemSummary>>(emptyList())
        private set

    var isLoading by mutableStateOf(true)
        private set

    var searchQuery by mutableStateOf("")
        private set

    var selectedFilter by mutableStateOf(StarSystemFilter.All)
        private set

    /** Size of the full catalogue, for the "Showing 12 of 4,554" count while filtered or searching. */
    var totalSystemCount by mutableIntStateOf(0)
        private set

    /**
     * Bumped whenever a new filter or search produces a fresh result list, so the screen can
     * jump back to the top instead of keeping the old list's scroll position.
     */
    var scrollResetCount by mutableIntStateOf(0)
        private set

    private var resetScrollOnNextResult = false

    /** The one live result-list collector; replaced (not stacked) on every filter or search change. */
    private var listJob: Job? = null

    init {
        trackEvent(AnalyticsEvent.StarSystemListScreenViewed)
        observeTotalCount()
        loadSystems()
    }

    private fun observeTotalCount() {
        viewModelScope.launch {
            getAllStarSystemsUseCase().collectLatest { totalSystemCount = it.size }
        }
    }

    private fun loadSystems() {
        listJob?.cancel()
        listJob = viewModelScope.launch {
            isLoading = true
            collectResults(currentFilterFlow())
        }
    }

    private fun currentFilterFlow(): Flow<List<StarSystemSummary>> = when (selectedFilter) {
        StarSystemFilter.All -> getAllStarSystemsUseCase()
        StarSystemFilter.SingleStar -> getStarSystemsByStarCountUseCase(1)
        StarSystemFilter.Binary -> getStarSystemsByStarCountUseCase(2)
        StarSystemFilter.Trinary -> getStarSystemsByStarCountUseCase(3)
        StarSystemFilter.MultiPlanet -> getMultiPlanetSystemsUseCase()
    }

    private suspend fun collectResults(flow: Flow<List<StarSystemSummary>>) {
        flow.collectLatest { list ->
            starSystems = list
            isLoading = false
            if (resetScrollOnNextResult) {
                resetScrollOnNextResult = false
                scrollResetCount++
            }
        }
    }

    fun onFilterSelected(filter: StarSystemFilter) {
        if (selectedFilter == filter) return
        selectedFilter = filter
        searchQuery = ""
        trackEvent(AnalyticsEvent.StarSystemFilterApplied(filter = filter.name))
        resetScrollOnNextResult = true
        loadSystems()
    }

    fun onSearchQueryChanged(query: String) {
        searchQuery = query
        listJob?.cancel()
        listJob = viewModelScope.launch {
            delay(300) // debounce
            resetScrollOnNextResult = true
            collectResults(if (query.isBlank()) currentFilterFlow() else searchStarSystemsUseCase(query))
        }
    }

    fun trackSystemClicked(system: StarSystemSummary) {
        trackEvent(AnalyticsEvent.StarSystemClicked(hostName = system.hostName))
    }

    // Keep for backward compat but delegate to new filter API
    val showMultiPlanetOnly: Boolean
        get() = selectedFilter == StarSystemFilter.MultiPlanet

    fun onToggleMultiPlanet() {
        onFilterSelected(
            if (selectedFilter == StarSystemFilter.MultiPlanet) StarSystemFilter.All
            else StarSystemFilter.MultiPlanet
        )
    }
}
