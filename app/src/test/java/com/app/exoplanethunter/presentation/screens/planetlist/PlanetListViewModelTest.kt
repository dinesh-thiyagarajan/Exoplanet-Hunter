package com.app.exoplanethunter.presentation.screens.planetlist

import com.app.exoplanethunter.analytics.domain.model.AnalyticsEvent
import com.app.exoplanethunter.analytics.domain.usecase.TrackEventUseCase
import com.app.exoplanethunter.exoplanet.domain.model.Exoplanet
import com.app.exoplanethunter.exoplanet.domain.usecase.FilterPlanetsUseCase
import com.app.exoplanethunter.exoplanet.domain.usecase.GetAllPlanetsUseCase
import com.app.exoplanethunter.exoplanet.domain.usecase.GetDiscoveryMethodsUseCase
import com.app.exoplanethunter.exoplanet.domain.usecase.GetFavoriteNamesUseCase
import com.app.exoplanethunter.exoplanet.domain.usecase.GetPlanetCountUseCase
import com.app.exoplanethunter.exoplanet.domain.usecase.SearchPlanetsUseCase
import com.app.exoplanethunter.exoplanet.domain.usecase.ToggleFavoriteUseCase
import com.app.exoplanethunter.testing.FakeAnalyticsRepository
import com.app.exoplanethunter.testing.FakeExoplanetRepository
import com.app.exoplanethunter.testing.MainDispatcherRule
import com.app.exoplanethunter.testing.planet
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class PlanetListViewModelTest {

    @get:Rule
    val main = MainDispatcherRule()

    private val repo = FakeExoplanetRepository()
    private val analytics = FakeAnalyticsRepository()

    private val a = planet(id = 1, name = "A b", distanceParsec = 30.0)
    private val b = planet(id = 2, name = "B b", distanceParsec = 10.0)
    private val c = planet(id = 3, name = "C b", distanceParsec = null)

    private fun viewModel() = PlanetListViewModel(
        GetAllPlanetsUseCase(repo),
        GetPlanetCountUseCase(repo),
        SearchPlanetsUseCase(repo),
        FilterPlanetsUseCase(repo),
        GetDiscoveryMethodsUseCase(repo),
        GetFavoriteNamesUseCase(repo),
        ToggleFavoriteUseCase(repo),
        TrackEventUseCase(analytics),
    )

    private fun ids(list: List<Exoplanet>) = list.map { it.id }

    @Test
    fun `initial load shows the catalogue and its total without jumping`() = runTest(main.dispatcher) {
        repo.planetsAll.value = listOf(a, b)
        repo.planetTotal.value = 6107

        val vm = viewModel()
        advanceUntilIdle()

        assertEquals(listOf(1L, 2L), ids(vm.planets))
        assertFalse(vm.isLoading)
        assertEquals(6107, vm.totalPlanetCount)
        assertEquals(0, vm.scrollResetCount)
    }

    @Test
    fun `a new filter's results start at the top`() = runTest(main.dispatcher) {
        repo.planetsAll.value = listOf(a, b)
        repo.getPlanetsByDiscoveryMethod("Transit").value = listOf(c)
        val vm = viewModel()
        advanceUntilIdle()

        vm.onFilterSelected("Transit")
        advanceUntilIdle()

        assertEquals(listOf(3L), ids(vm.planets))
        assertEquals(1, vm.scrollResetCount)
    }

    @Test
    fun `an old filter's database updates no longer overwrite the current results`() = runTest(main.dispatcher) {
        repo.planetsAll.value = listOf(a, b)
        repo.getPlanetsByDiscoveryMethod("Transit").value = listOf(c)
        val vm = viewModel()
        advanceUntilIdle()
        vm.onFilterSelected("Transit")
        advanceUntilIdle()

        // The "All" query re-emits (e.g. after a catalogue sync) while "Transit" is showing.
        repo.planetsAll.value = listOf(a, b, planet(id = 9))
        advanceUntilIdle()

        assertEquals(listOf(3L), ids(vm.planets))
    }

    @Test
    fun `a data refresh of the current filter updates in place without jumping to the top`() = runTest(main.dispatcher) {
        repo.getPlanetsByDiscoveryMethod("Transit").value = listOf(c)
        val vm = viewModel()
        advanceUntilIdle()
        vm.onFilterSelected("Transit")
        advanceUntilIdle()
        val resets = vm.scrollResetCount

        repo.getPlanetsByDiscoveryMethod("Transit").value = listOf(c, a)
        advanceUntilIdle()

        assertEquals(listOf(3L, 1L), ids(vm.planets))
        assertEquals(resets, vm.scrollResetCount)
    }

    @Test
    fun `switching filters keeps the current list instead of flashing a spinner`() = runTest(main.dispatcher) {
        repo.planetsAll.value = listOf(a, b)
        repo.habitableOverride = MutableSharedFlow() // results still on their way
        val vm = viewModel()
        advanceUntilIdle()

        vm.onToggleHabitable()
        advanceUntilIdle()

        assertFalse(vm.isLoading)
        assertEquals(listOf(1L, 2L), ids(vm.planets))
    }

    @Test
    fun `search waits for typing to pause and tracks only the final query`() = runTest(main.dispatcher) {
        repo.planetsAll.value = listOf(a, b)
        repo.planetSearchResults["kepler"] = listOf(c)
        val vm = viewModel()
        advanceUntilIdle()

        vm.onSearchQueryChanged("k")
        advanceTimeBy(100)
        vm.onSearchQueryChanged("kepler")
        advanceTimeBy(299)
        runCurrent()
        assertEquals("still debouncing", listOf(1L, 2L), ids(vm.planets))

        advanceTimeBy(2)
        runCurrent()
        assertEquals(listOf(3L), ids(vm.planets))
        assertEquals(1, vm.scrollResetCount)
        val searches = analytics.events.filterIsInstance<AnalyticsEvent.PlanetSearched>()
        assertEquals(1, searches.size)
    }

    @Test
    fun `clearing the search goes back to the current filter`() = runTest(main.dispatcher) {
        repo.planetsAll.value = listOf(a, b)
        repo.planetSearchResults["kepler"] = listOf(c)
        val vm = viewModel()
        advanceUntilIdle()
        vm.onSearchQueryChanged("kepler")
        advanceUntilIdle()

        vm.onSearchQueryChanged("")
        advanceUntilIdle()

        assertEquals(listOf(1L, 2L), ids(vm.planets))
    }

    @Test
    fun `sorting by nearest puts unknown distances last and starts at the top`() = runTest(main.dispatcher) {
        repo.planetsAll.value = listOf(a, c, b)
        val vm = viewModel()
        advanceUntilIdle()

        vm.onSortSelected(SortOption.NEAREST)

        assertEquals(listOf(2L, 1L, 3L), ids(vm.planets))
        assertEquals(1, vm.scrollResetCount)
    }

    @Test
    fun `selecting the active filter again does nothing`() = runTest(main.dispatcher) {
        repo.planetsAll.value = listOf(a)
        val vm = viewModel()
        advanceUntilIdle()

        vm.onFilterSelected(null) // "All" is already active
        advanceUntilIdle()

        assertEquals(0, vm.scrollResetCount)
    }

    @Test
    fun `favoriting tracks what the toggle actually did`() = runTest(main.dispatcher) {
        repo.planetsAll.value = listOf(a)
        val vm = viewModel()
        advanceUntilIdle()

        vm.toggleFavorite(a)
        advanceUntilIdle()
        vm.toggleFavorite(a)
        advanceUntilIdle()

        assertTrue(a.planetName !in vm.favoriteNames)
        assertEquals(1, analytics.events.filterIsInstance<AnalyticsEvent.PlanetFavorited>().size)
        assertEquals(1, analytics.events.filterIsInstance<AnalyticsEvent.PlanetUnfavorited>().size)
    }
}
