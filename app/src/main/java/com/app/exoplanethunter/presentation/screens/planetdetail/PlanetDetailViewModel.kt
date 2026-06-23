package com.app.exoplanethunter.presentation.screens.planetdetail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.app.exoplanethunter.analytics.domain.model.AnalyticsEvent
import com.app.exoplanethunter.analytics.domain.usecase.TrackEventUseCase
import com.app.exoplanethunter.exoplanet.domain.model.Exoplanet
import com.app.exoplanethunter.exoplanet.domain.model.HabitabilityInsight
import com.app.exoplanethunter.exoplanet.domain.usecase.GetFavoriteNamesUseCase
import com.app.exoplanethunter.exoplanet.domain.usecase.GetPlanetByIdUseCase
import com.app.exoplanethunter.exoplanet.domain.usecase.ToggleFavoriteUseCase
import com.app.exoplanethunter.ml.GetHabitabilityInsightUseCase
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/** Everything the planet-detail UI needs to render. Lets the screen be stateless/previewable. */
data class PlanetDetailUiState(
    val isLoading: Boolean = true,
    val planet: Exoplanet? = null,
    val insight: HabitabilityInsight? = null,
    val isFavorite: Boolean = false,
)

class PlanetDetailViewModel(
    private val getPlanetByIdUseCase: GetPlanetByIdUseCase,
    private val getHabitabilityInsightUseCase: GetHabitabilityInsightUseCase,
    private val getFavoriteNamesUseCase: GetFavoriteNamesUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val trackEvent: TrackEventUseCase
) : ViewModel() {

    var planet by mutableStateOf<Exoplanet?>(null)
        private set

    var insight by mutableStateOf<HabitabilityInsight?>(null)
        private set

    var isLoading by mutableStateOf(true)
        private set

    private var favoriteNames by mutableStateOf<Set<String>>(emptySet())

    val isFavorite: Boolean
        get() = planet?.planetName?.let { it in favoriteNames } ?: false

    /** State-backed snapshot consumed by the stateless content composable. */
    val uiState: PlanetDetailUiState
        get() = PlanetDetailUiState(
            isLoading = isLoading,
            planet = planet,
            insight = insight,
            isFavorite = isFavorite,
        )

    init {
        observeFavorites()
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            getFavoriteNamesUseCase().collectLatest { favoriteNames = it }
        }
    }

    fun toggleFavorite() {
        val current = planet ?: return
        val wasFavorite = current.planetName in favoriteNames
        trackEvent(
            if (wasFavorite) AnalyticsEvent.PlanetUnfavorited(current.id, current.planetName)
            else AnalyticsEvent.PlanetFavorited(current.id, current.planetName)
        )
        viewModelScope.launch {
            toggleFavoriteUseCase(current.planetName)
        }
    }

    fun loadPlanet(id: Long) {
        viewModelScope.launch {
            isLoading = true
            val loadedPlanet = getPlanetByIdUseCase(id)
            planet = loadedPlanet
            loadedPlanet?.let {
                trackEvent(AnalyticsEvent.PlanetDetailScreenViewed)
                insight = getHabitabilityInsightUseCase(it)
            }
            isLoading = false
        }
    }
}
