package com.workspace.exoplanethunter.domain.usecase

import com.workspace.exoplanethunter.domain.repository.ExoplanetRepository

class GetDiscoveryMethodsUseCase(private val repository: ExoplanetRepository) {
    suspend operator fun invoke(): List<String> = repository.getDiscoveryMethods()
}
