package com.app.exoplanethunter.exoplanet.domain.usecase

import com.app.exoplanethunter.exoplanet.domain.repository.ExoplanetRepository
import com.app.exoplanethunter.exoplanet.domain.repository.SyncStatus
import kotlinx.coroutines.flow.Flow

class SyncExoplanetsUseCase(private val repository: ExoplanetRepository) {
    suspend operator fun invoke(): Flow<SyncStatus> {
        return repository.syncExoplanets()
    }
}
