package com.workspace.exoplanethunter.exoplanet.domain.usecase

import com.workspace.exoplanethunter.exoplanet.domain.repository.ExoplanetRepository

class GetDiscoveryMethodsUseCase(private val repository: ExoplanetRepository) {
    suspend operator fun invoke(): List<String> = repository.getDiscoveryMethods()
}
