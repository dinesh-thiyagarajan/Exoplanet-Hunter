package com.workspace.exoplanethunter

import android.app.Application
import com.workspace.exoplanethunter.exoplanet.data.local.csv.CsvParser
import com.workspace.exoplanethunter.exoplanet.data.local.db.ExoplanetDatabase
import com.workspace.exoplanethunter.exoplanet.data.repository.ExoplanetRepositoryImpl
import com.workspace.exoplanethunter.exoplanet.domain.repository.ExoplanetRepository
import com.workspace.exoplanethunter.exoplanet.domain.usecase.FilterPlanetsUseCase
import com.workspace.exoplanethunter.exoplanet.domain.usecase.GetAllPlanetsUseCase
import com.workspace.exoplanethunter.exoplanet.domain.usecase.GetAllStarSystemsUseCase
import com.workspace.exoplanethunter.exoplanet.domain.usecase.GetDiscoveryMethodsUseCase
import com.workspace.exoplanethunter.exoplanet.domain.usecase.GetMultiPlanetSystemsUseCase
import com.workspace.exoplanethunter.exoplanet.domain.usecase.GetPlanetByIdUseCase
import com.workspace.exoplanethunter.exoplanet.domain.usecase.GetStarSystemUseCase
import com.workspace.exoplanethunter.exoplanet.domain.usecase.LoadDataUseCase
import com.workspace.exoplanethunter.exoplanet.domain.usecase.SearchPlanetsUseCase
import com.workspace.exoplanethunter.exoplanet.domain.usecase.SearchStarSystemsUseCase
import com.workspace.exoplanethunter.ml.ExoplanetClassifier
import com.workspace.exoplanethunter.ml.GetHabitabilityInsightUseCase

class ExoplanetApp : Application() {

    private val database by lazy { ExoplanetDatabase.getInstance(this) }
    private val csvParser by lazy { CsvParser(this) }
    val classifier by lazy { ExoplanetClassifier(this) }

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
    val getAllStarSystemsUseCase by lazy { GetAllStarSystemsUseCase(repository) }
    val getStarSystemUseCase by lazy { GetStarSystemUseCase(repository) }
    val searchStarSystemsUseCase by lazy { SearchStarSystemsUseCase(repository) }
    val getMultiPlanetSystemsUseCase by lazy { GetMultiPlanetSystemsUseCase(repository) }
}
