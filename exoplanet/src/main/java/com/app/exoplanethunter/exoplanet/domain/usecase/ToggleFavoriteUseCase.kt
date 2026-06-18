package com.app.exoplanethunter.exoplanet.domain.usecase

import com.app.exoplanethunter.exoplanet.domain.repository.ExoplanetRepository

class ToggleFavoriteUseCase(private val repository: ExoplanetRepository) {
    suspend operator fun invoke(planetName: String) = repository.toggleFavorite(planetName)
}
