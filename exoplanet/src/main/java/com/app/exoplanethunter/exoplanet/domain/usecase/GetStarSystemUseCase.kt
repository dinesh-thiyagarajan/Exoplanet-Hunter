package com.app.exoplanethunter.exoplanet.domain.usecase

import com.app.exoplanethunter.exoplanet.domain.model.StarSystem
import com.app.exoplanethunter.exoplanet.domain.repository.ExoplanetRepository

class GetStarSystemUseCase(private val repository: ExoplanetRepository) {
    suspend operator fun invoke(systemId: Long): StarSystem? = repository.getStarSystem(systemId)
}
