package com.app.exoplanethunter.presentation.screens.starsystem

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.app.exoplanethunter.ExoplanetApp
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/** Filters available on the Star System list screen. */
enum class StarSystemFilter(val label: String) {
    All("All"),
    SingleStar("Single Star"),
    Binary("Binary"),
    Trinary("Trinary"),
    MultiPlanet("Multi-Planet")
}

class StarSystemListViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as ExoplanetApp
    private val getAllStarSystemsUseCase = app.getAllStarSystemsUseCase
    private val searchStarSystemsUseCase = app.searchStarSystemsUseCase
    private val getMultiPlanetSystemsUseCase = app.getMultiPlanetSystemsUseCase
    private val getStarSystemsByStarCountUseCase = app.getStarSystemsByStarCountUseCase

    var starSystems by mutableStateOf<List<String>>(emptyList())
        private set

    var isLoading by mutableStateOf(true)
        private set

    var searchQuery by mutableStateOf("")
        private set

    var selectedFilter by mutableStateOf(StarSystemFilter.All)
        private set

    private var loadJob: Job? = null
    private var searchJob: Job? = null

    init {
        loadSystems()
    }

    private fun loadSystems() {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            isLoading = true
            val flow = when (selectedFilter) {
                StarSystemFilter.All -> getAllStarSystemsUseCase()
                StarSystemFilter.SingleStar -> getStarSystemsByStarCountUseCase(1)
                StarSystemFilter.Binary -> getStarSystemsByStarCountUseCase(2)
                StarSystemFilter.Trinary -> getStarSystemsByStarCountUseCase(3)
                StarSystemFilter.MultiPlanet -> getMultiPlanetSystemsUseCase()
            }
            flow.collectLatest { list ->
                starSystems = list
                isLoading = false
            }
        }
    }

    fun onFilterSelected(filter: StarSystemFilter) {
        if (selectedFilter == filter) return
        selectedFilter = filter
        searchQuery = ""
        loadSystems()
    }

    fun onSearchQueryChanged(query: String) {
        searchQuery = query
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300) // debounce
            if (query.isBlank()) {
                loadSystems()
            } else {
                searchStarSystemsUseCase(query).collectLatest { list ->
                    starSystems = list
                    isLoading = false
                }
            }
        }
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

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as ExoplanetApp
                StarSystemListViewModel(app)
            }
        }
    }
}
