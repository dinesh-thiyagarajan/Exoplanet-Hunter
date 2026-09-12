package com.app.exoplanethunter.exoplanet.domain.usecase

import com.app.exoplanethunter.exoplanet.domain.repository.ExoplanetRepository

class ToggleFavoriteUseCase(private val repository: ExoplanetRepository) {
    /** @return true when the planet is a favorite after the toggle. */
    suspend operator fun invoke(planetName: String): Boolean = repository.toggleFavorite(planetName)
}
