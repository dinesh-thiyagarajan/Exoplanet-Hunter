package com.app.exoplanethunter.exoplanet.domain.usecase

import com.app.exoplanethunter.exoplanet.domain.model.Statistics
import com.app.exoplanethunter.exoplanet.domain.repository.ExoplanetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class GetStatisticsUseCase(private val repository: ExoplanetRepository) {
    operator fun invoke(): Flow<Statistics> = combine(
        repository.getPlanetCount(),
        repository.getStarSystemCount(),
        repository.getDiscoveryMethodCounts(),
        repository.getDiscoveryYearCounts(),
        repository.getSizeDistribution()
    ) { planets, systems, methods, years, sizes ->
        Statistics(
            totalPlanets = planets,
            totalSystems = systems,
            methodCounts = methods,
            yearCounts = years,
            sizeDistribution = sizes
        )
    }
}
