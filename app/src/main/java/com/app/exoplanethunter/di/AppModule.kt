package com.app.exoplanethunter.di

import com.app.exoplanethunter.analytics.di.analyticsModule
import com.app.exoplanethunter.exoplanet.data.local.csv.CsvParser
import com.app.exoplanethunter.exoplanet.data.local.db.ExoplanetDatabase
import com.app.exoplanethunter.exoplanet.data.repository.ExoplanetRepositoryImpl
import com.app.exoplanethunter.exoplanet.domain.repository.ExoplanetRepository
import com.app.exoplanethunter.exoplanet.domain.usecase.FilterPlanetsUseCase
import com.app.exoplanethunter.exoplanet.domain.usecase.GetAllPlanetsUseCase
import com.app.exoplanethunter.exoplanet.domain.usecase.GetAllStarSystemsUseCase
import com.app.exoplanethunter.exoplanet.domain.usecase.GetDiscoveryMethodsUseCase
import com.app.exoplanethunter.exoplanet.domain.usecase.GetMultiPlanetSystemsUseCase
import com.app.exoplanethunter.exoplanet.domain.usecase.GetPlanetByIdUseCase
import com.app.exoplanethunter.exoplanet.domain.usecase.GetStarSystemUseCase
import com.app.exoplanethunter.exoplanet.domain.usecase.GetStarSystemsByStarCountUseCase
import com.app.exoplanethunter.exoplanet.domain.usecase.LoadDataUseCase
import com.app.exoplanethunter.exoplanet.domain.usecase.SearchPlanetsUseCase
import com.app.exoplanethunter.exoplanet.domain.usecase.SearchStarSystemsUseCase
import com.app.exoplanethunter.ml.ExoplanetClassifier
import com.app.exoplanethunter.ml.GetHabitabilityInsightUseCase
import com.app.exoplanethunter.presentation.screens.planetdetail.PlanetDetailViewModel
import com.app.exoplanethunter.presentation.screens.planetlist.PlanetListViewModel
import com.app.exoplanethunter.presentation.screens.splash.SplashViewModel
import com.app.exoplanethunter.presentation.screens.starsystem.StarSystemDetailViewModel
import com.app.exoplanethunter.presentation.screens.starsystem.StarSystemListViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val databaseModule = module {
    single { ExoplanetDatabase.getInstance(androidContext()) }
    single { get<ExoplanetDatabase>().exoplanetDao() }
}

val dataModule = module {
    single { CsvParser(androidContext()) }
    single<ExoplanetRepository> { ExoplanetRepositoryImpl(get(), get()) }
}

val mlModule = module {
    single { ExoplanetClassifier(androidContext()) }
}

val useCaseModule = module {
    factory { GetAllPlanetsUseCase(get()) }
    factory { SearchPlanetsUseCase(get()) }
    factory { GetPlanetByIdUseCase(get()) }
    factory { GetDiscoveryMethodsUseCase(get()) }
    factory { LoadDataUseCase(get()) }
    factory { FilterPlanetsUseCase(get()) }
    factory { GetAllStarSystemsUseCase(get()) }
    factory { GetStarSystemUseCase(get()) }
    factory { SearchStarSystemsUseCase(get()) }
    factory { GetMultiPlanetSystemsUseCase(get()) }
    factory { GetStarSystemsByStarCountUseCase(get()) }
    factory { GetHabitabilityInsightUseCase(get()) }
}

val viewModelModule = module {
    viewModel { PlanetListViewModel(get(), get(), get(), get(), get()) }
    viewModel { PlanetDetailViewModel(get(), get(), get()) }
    viewModel { StarSystemListViewModel(get(), get(), get(), get(), get()) }
    viewModel { StarSystemDetailViewModel(get(), get()) }
    viewModel { SplashViewModel(get()) }
}

val appModules = listOf(
    analyticsModule,
    databaseModule,
    dataModule,
    mlModule,
    useCaseModule,
    viewModelModule
)
