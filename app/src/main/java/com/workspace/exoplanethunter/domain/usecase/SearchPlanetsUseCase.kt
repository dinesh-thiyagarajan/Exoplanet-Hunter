package com.workspace.exoplanethunter.domain.usecase

import com.workspace.exoplanethunter.domain.model.Exoplanet
import com.workspace.exoplanethunter.domain.repository.ExoplanetRepository
import kotlinx.coroutines.flow.Flow

class SearchPlanetsUseCase(private val repository: ExoplanetRepository) {
    operator fun invoke(query: String): Flow<List<Exoplanet>> = repository.searchPlanets(query)
}
