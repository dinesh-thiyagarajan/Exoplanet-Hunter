package com.app.exoplanethunter.exoplanet.domain.usecase

import com.app.exoplanethunter.exoplanet.domain.model.Exoplanet
import com.app.exoplanethunter.exoplanet.domain.repository.ExoplanetRepository
import kotlinx.coroutines.flow.Flow

class SearchPlanetsUseCase(private val repository: ExoplanetRepository) {
    operator fun invoke(query: String): Flow<List<Exoplanet>> = repository.searchPlanets(query)
}
