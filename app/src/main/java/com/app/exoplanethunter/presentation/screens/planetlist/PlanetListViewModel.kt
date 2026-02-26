package com.app.exoplanethunter.presentation.screens.planetlist

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
import com.app.exoplanethunter.exoplanet.domain.model.Exoplanet
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class PlanetListViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as ExoplanetApp
    private val getAllPlanetsUseCase = app.getAllPlanetsUseCase
    private val searchPlanetsUseCase = app.searchPlanetsUseCase
    private val filterPlanetsUseCase = app.filterPlanetsUseCase
    private val getDiscoveryMethodsUseCase = app.getDiscoveryMethodsUseCase

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

    private var searchJob: Job? = null

    init {
        loadPlanets()
        loadFilters()
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
                searchPlanetsUseCase(query).collectLatest { list ->
                    planets = list
                }
            }
        }
    }

    fun onFilterSelected(method: String?) {
        selectedFilter = method
        showHabitableOnly = false
        searchQuery = ""
        applyCurrentFilter()
    }

    fun onToggleHabitable() {
        showHabitableOnly = !showHabitableOnly
        selectedFilter = null
        searchQuery = ""
        applyCurrentFilter()
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

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as ExoplanetApp
                PlanetListViewModel(app)
            }
        }
    }
}
