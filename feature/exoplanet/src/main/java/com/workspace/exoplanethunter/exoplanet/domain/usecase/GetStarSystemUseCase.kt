package com.workspace.exoplanethunter.exoplanet.domain.usecase

import com.workspace.exoplanethunter.exoplanet.domain.model.StarSystem
import com.workspace.exoplanethunter.exoplanet.domain.repository.ExoplanetRepository

class GetStarSystemUseCase(private val repository: ExoplanetRepository) {
    suspend operator fun invoke(hostName: String): StarSystem? = repository.getStarSystem(hostName)
}
