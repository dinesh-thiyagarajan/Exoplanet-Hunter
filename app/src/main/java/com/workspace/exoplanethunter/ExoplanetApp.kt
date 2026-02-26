package com.workspace.exoplanethunter

import android.app.Application
import com.workspace.exoplanethunter.data.local.csv.CsvParser
import com.workspace.exoplanethunter.data.local.db.ExoplanetDatabase
import com.workspace.exoplanethunter.data.local.ml.HabitabilityClassifier
import com.workspace.exoplanethunter.data.repository.ExoplanetRepositoryImpl
import com.workspace.exoplanethunter.domain.repository.ExoplanetRepository
import com.workspace.exoplanethunter.domain.usecase.FilterPlanetsUseCase
import com.workspace.exoplanethunter.domain.usecase.GetAllPlanetsUseCase
import com.workspace.exoplanethunter.domain.usecase.GetDiscoveryMethodsUseCase
import com.workspace.exoplanethunter.domain.usecase.GetHabitabilityInsightUseCase
import com.workspace.exoplanethunter.domain.usecase.GetPlanetByIdUseCase
import com.workspace.exoplanethunter.domain.usecase.LoadDataUseCase
import com.workspace.exoplanethunter.domain.usecase.SearchPlanetsUseCase

class ExoplanetApp : Application() {

    private val database by lazy { ExoplanetDatabase.getInstance(this) }
    private val csvParser by lazy { CsvParser(this) }
    private val classifier by lazy { HabitabilityClassifier() }

    private val repository: ExoplanetRepository by lazy {
        ExoplanetRepositoryImpl(database.exoplanetDao(), csvParser)
    }

    val getAllPlanetsUseCase by lazy { GetAllPlanetsUseCase(repository) }
    val searchPlanetsUseCase by lazy { SearchPlanetsUseCase(repository) }
    val getPlanetByIdUseCase by lazy { GetPlanetByIdUseCase(repository) }
    val getHabitabilityInsightUseCase by lazy { GetHabitabilityInsightUseCase(classifier) }
    val getDiscoveryMethodsUseCase by lazy { GetDiscoveryMethodsUseCase(repository) }
    val loadDataUseCase by lazy { LoadDataUseCase(repository) }
    val filterPlanetsUseCase by lazy { FilterPlanetsUseCase(repository) }
}
