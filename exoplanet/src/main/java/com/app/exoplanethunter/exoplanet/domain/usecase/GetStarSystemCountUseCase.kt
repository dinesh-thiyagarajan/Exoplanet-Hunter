package com.app.exoplanethunter.exoplanet.domain.usecase

import com.app.exoplanethunter.exoplanet.domain.repository.ExoplanetRepository
import kotlinx.coroutines.flow.Flow

class GetStarSystemCountUseCase(private val repository: ExoplanetRepository) {
    operator fun invoke(): Flow<Int> = repository.getStarSystemCount()
}
