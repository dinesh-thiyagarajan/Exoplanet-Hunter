package com.app.exoplanethunter.presentation.screens.starsystem

import com.app.exoplanethunter.analytics.domain.usecase.TrackEventUseCase
import com.app.exoplanethunter.exoplanet.domain.usecase.GetAllStarSystemsUseCase
import com.app.exoplanethunter.exoplanet.domain.usecase.GetMultiPlanetSystemsUseCase
import com.app.exoplanethunter.exoplanet.domain.usecase.GetStarSystemCountUseCase
import com.app.exoplanethunter.exoplanet.domain.usecase.GetStarSystemsByStarCountUseCase
import com.app.exoplanethunter.exoplanet.domain.usecase.SearchStarSystemsUseCase
import com.app.exoplanethunter.testing.FakeAnalyticsRepository
import com.app.exoplanethunter.testing.FakeExoplanetRepository
import com.app.exoplanethunter.testing.MainDispatcherRule
import com.app.exoplanethunter.testing.system
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StarSystemListViewModelTest {

    @get:Rule
    val main = MainDispatcherRule()

    private val repo = FakeExoplanetRepository()

    private fun viewModel() = StarSystemListViewModel(
        GetAllStarSystemsUseCase(repo),
        GetStarSystemCountUseCase(repo),
        SearchStarSystemsUseCase(repo),
        GetMultiPlanetSystemsUseCase(repo),
        GetStarSystemsByStarCountUseCase(repo),
        TrackEventUseCase(FakeAnalyticsRepository()),
    )

    @Test
    fun `initial load shows every system and the total`() = runTest(main.dispatcher) {
        repo.allSystems.value = listOf(system(1), system(2))
        repo.systemCount.value = 4554

        val vm = viewModel()
        advanceUntilIdle()

        assertEquals(listOf(1L, 2L), vm.starSystems.map { it.id })
        assertEquals(4554, vm.totalSystemCount)
        assertFalse(vm.isLoading)
        assertEquals(0, vm.scrollResetCount)
    }

    @Test
    fun `binary filter queries two-star systems and starts at the top`() = runTest(main.dispatcher) {
        repo.allSystems.value = listOf(system(1), system(2, stars = 2))
        repo.getStarSystemsByStarCount(2).value = listOf(system(2, stars = 2))
        val vm = viewModel()
        advanceUntilIdle()

        vm.onFilterSelected(StarSystemFilter.Binary)
        advanceUntilIdle()

        assertEquals(listOf(2L), vm.starSystems.map { it.id })
        assertEquals(1, vm.scrollResetCount)
    }

    @Test
    fun `the previous filter's updates no longer overwrite results`() = runTest(main.dispatcher) {
        repo.allSystems.value = listOf(system(1))
        repo.getStarSystemsByStarCount(2).value = listOf(system(2, stars = 2))
        val vm = viewModel()
        advanceUntilIdle()
        vm.onFilterSelected(StarSystemFilter.Binary)
        advanceUntilIdle()

        repo.allSystems.value = listOf(system(1), system(3))
        advanceUntilIdle()

        assertEquals(listOf(2L), vm.starSystems.map { it.id })
    }

    @Test
    fun `search is debounced, and a stale search can't overwrite a newer one`() = runTest(main.dispatcher) {
        repo.allSystems.value = listOf(system(1), system(2))
        repo.systemSearchResults["tra"] = listOf(system(7))
        repo.systemSearchResults["trappist"] = listOf(system(8))
        val vm = viewModel()
        advanceUntilIdle()

        vm.onSearchQueryChanged("tra")
        advanceTimeBy(200)
        vm.onSearchQueryChanged("trappist")
        advanceTimeBy(301)
        runCurrent()

        assertEquals(listOf(8L), vm.starSystems.map { it.id })
        assertEquals(1, vm.scrollResetCount)
    }

    @Test
    fun `clearing search restores the active filter`() = runTest(main.dispatcher) {
        repo.getStarSystemsByStarCount(2).value = listOf(system(2, stars = 2))
        repo.systemSearchResults["x"] = listOf(system(9))
        val vm = viewModel()
        advanceUntilIdle()
        vm.onFilterSelected(StarSystemFilter.Binary)
        advanceUntilIdle()
        vm.onSearchQueryChanged("x")
        advanceUntilIdle()

        vm.onSearchQueryChanged("")
        advanceUntilIdle()

        assertEquals(listOf(2L), vm.starSystems.map { it.id })
    }
}
