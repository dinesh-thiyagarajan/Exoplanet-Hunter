package com.workspace.exoplanethunter.exoplanet.domain.usecase

import com.workspace.exoplanethunter.exoplanet.domain.repository.ExoplanetRepository
import kotlinx.coroutines.flow.Flow

class GetMultiPlanetSystemsUseCase(private val repository: ExoplanetRepository) {
    operator fun invoke(): Flow<List<String>> = repository.getMultiPlanetSystems()
}
