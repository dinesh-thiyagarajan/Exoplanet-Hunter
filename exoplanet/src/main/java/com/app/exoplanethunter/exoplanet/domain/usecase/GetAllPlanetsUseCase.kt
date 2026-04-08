package com.app.exoplanethunter.exoplanet.domain.usecase

import com.app.exoplanethunter.exoplanet.domain.model.Exoplanet
import com.app.exoplanethunter.exoplanet.domain.repository.ExoplanetRepository
import kotlinx.coroutines.flow.Flow

class GetAllPlanetsUseCase(private val repository: ExoplanetRepository) {
    operator fun invoke(): Flow<List<Exoplanet>> = repository.getAllPlanets()
}
