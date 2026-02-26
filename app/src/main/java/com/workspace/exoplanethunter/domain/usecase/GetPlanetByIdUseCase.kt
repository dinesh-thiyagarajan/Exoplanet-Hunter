package com.workspace.exoplanethunter.domain.usecase

import com.workspace.exoplanethunter.domain.model.Exoplanet
import com.workspace.exoplanethunter.domain.repository.ExoplanetRepository

class GetPlanetByIdUseCase(private val repository: ExoplanetRepository) {
    suspend operator fun invoke(id: Long): Exoplanet? = repository.getPlanetById(id)
}
