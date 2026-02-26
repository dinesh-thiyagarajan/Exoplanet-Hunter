package com.workspace.exoplanethunter.domain.usecase

import com.workspace.exoplanethunter.domain.model.Exoplanet
import com.workspace.exoplanethunter.domain.repository.ExoplanetRepository
import kotlinx.coroutines.flow.Flow

class GetAllPlanetsUseCase(private val repository: ExoplanetRepository) {
    operator fun invoke(): Flow<List<Exoplanet>> = repository.getAllPlanets()
}
