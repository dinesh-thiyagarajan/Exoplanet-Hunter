package com.workspace.exoplanethunter.exoplanet.domain.usecase

import com.workspace.exoplanethunter.exoplanet.domain.model.Exoplanet
import com.workspace.exoplanethunter.exoplanet.domain.repository.ExoplanetRepository
import kotlinx.coroutines.flow.Flow

class GetAllPlanetsUseCase(private val repository: ExoplanetRepository) {
    operator fun invoke(): Flow<List<Exoplanet>> = repository.getAllPlanets()
}
