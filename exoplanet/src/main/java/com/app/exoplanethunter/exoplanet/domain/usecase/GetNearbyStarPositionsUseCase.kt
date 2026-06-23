package com.app.exoplanethunter.exoplanet.domain.usecase

import com.app.exoplanethunter.exoplanet.domain.model.StarPosition
import com.app.exoplanethunter.exoplanet.domain.repository.ExoplanetRepository
import kotlinx.coroutines.flow.Flow

/**
 * Nearest host stars (those with full coordinates) for the 3D galaxy map,
 * ordered by distance so the view centers on our cosmic neighborhood.
 */
class GetNearbyStarPositionsUseCase(private val repository: ExoplanetRepository) {
    operator fun invoke(limit: Int = 400): Flow<List<StarPosition>> =
        repository.getNearbyStarPositions(limit)
}
