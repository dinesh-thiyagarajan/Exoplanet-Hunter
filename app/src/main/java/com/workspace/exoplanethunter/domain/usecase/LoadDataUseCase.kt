package com.workspace.exoplanethunter.domain.usecase

import com.workspace.exoplanethunter.domain.repository.ExoplanetRepository

class LoadDataUseCase(private val repository: ExoplanetRepository) {
    suspend operator fun invoke() = repository.loadDataIfNeeded()
}
