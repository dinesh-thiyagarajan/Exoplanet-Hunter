package com.workspace.exoplanethunter.exoplanet.domain.usecase

import com.workspace.exoplanethunter.exoplanet.domain.repository.ExoplanetRepository

class LoadDataUseCase(private val repository: ExoplanetRepository) {
    suspend operator fun invoke() = repository.loadDataIfNeeded()
}
