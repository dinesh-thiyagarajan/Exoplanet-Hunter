package com.workspace.exoplanethunter.exoplanet.domain.usecase

import com.workspace.exoplanethunter.exoplanet.domain.repository.ExoplanetRepository
import kotlinx.coroutines.flow.Flow

class GetStarSystemsByStarCountUseCase(private val repository: ExoplanetRepository) {
    operator fun invoke(starCount: Int): Flow<List<String>> =
        repository.getStarSystemsByStarCount(starCount)
}
