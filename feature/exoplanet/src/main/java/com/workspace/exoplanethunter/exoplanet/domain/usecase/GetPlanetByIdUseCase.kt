package com.workspace.exoplanethunter.exoplanet.domain.usecase

import com.workspace.exoplanethunter.exoplanet.domain.model.Exoplanet
import com.workspace.exoplanethunter.exoplanet.domain.repository.ExoplanetRepository

class GetPlanetByIdUseCase(private val repository: ExoplanetRepository) {
    suspend operator fun invoke(id: Long): Exoplanet? = repository.getPlanetById(id)
}
