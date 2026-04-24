package com.app.exoplanethunter.exoplanet.domain.usecase

import com.app.exoplanethunter.exoplanet.domain.model.StarSystemSummary
import com.app.exoplanethunter.exoplanet.domain.repository.ExoplanetRepository
import kotlinx.coroutines.flow.Flow

class GetAllStarSystemsUseCase(private val repository: ExoplanetRepository) {
    operator fun invoke(): Flow<List<StarSystemSummary>> = repository.getAllStarSystems()
}
