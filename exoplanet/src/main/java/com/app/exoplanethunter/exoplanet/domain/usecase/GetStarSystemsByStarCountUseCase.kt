package com.app.exoplanethunter.exoplanet.domain.usecase

import com.app.exoplanethunter.exoplanet.domain.model.StarSystemSummary
import com.app.exoplanethunter.exoplanet.domain.repository.ExoplanetRepository
import kotlinx.coroutines.flow.Flow

class GetStarSystemsByStarCountUseCase(private val repository: ExoplanetRepository) {
    operator fun invoke(starCount: Int): Flow<List<StarSystemSummary>> =
        repository.getStarSystemsByStarCount(starCount)
}
