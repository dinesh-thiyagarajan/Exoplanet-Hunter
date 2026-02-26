package com.app.exoplanethunter.exoplanet.domain.usecase

import com.app.exoplanethunter.exoplanet.domain.model.Exoplanet
import com.app.exoplanethunter.exoplanet.domain.repository.ExoplanetRepository
import kotlinx.coroutines.flow.Flow

class FilterPlanetsUseCase(private val repository: ExoplanetRepository) {
    fun byDiscoveryMethod(method: String): Flow<List<Exoplanet>> =
        repository.getPlanetsByDiscoveryMethod(method)

    fun mostHabitable(limit: Int = 20): Flow<List<Exoplanet>> =
        repository.getMostHabitablePlanets(limit)
}
