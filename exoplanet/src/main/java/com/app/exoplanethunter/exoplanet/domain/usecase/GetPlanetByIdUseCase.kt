package com.app.exoplanethunter.exoplanet.domain.usecase

import com.app.exoplanethunter.exoplanet.domain.model.Exoplanet
import com.app.exoplanethunter.exoplanet.domain.repository.ExoplanetRepository

class GetPlanetByIdUseCase(private val repository: ExoplanetRepository) {
    suspend operator fun invoke(id: Long): Exoplanet? = repository.getPlanetById(id)
}
