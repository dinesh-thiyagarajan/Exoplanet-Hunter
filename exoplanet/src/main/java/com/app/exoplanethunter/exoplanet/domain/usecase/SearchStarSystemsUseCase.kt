package com.app.exoplanethunter.exoplanet.domain.usecase

import com.app.exoplanethunter.exoplanet.domain.model.StarSystemSummary
import com.app.exoplanethunter.exoplanet.domain.repository.ExoplanetRepository
import kotlinx.coroutines.flow.Flow

class SearchStarSystemsUseCase(private val repository: ExoplanetRepository) {
    operator fun invoke(query: String): Flow<List<StarSystemSummary>> = repository.searchStarSystems(query)
}
