package com.app.exoplanethunter.presentation.screens.starsystem

import com.app.exoplanethunter.analytics.domain.model.AnalyticsEvent
import com.app.exoplanethunter.analytics.domain.model.OrreryToggle
import com.app.exoplanethunter.analytics.domain.usecase.TrackEventUseCase
import com.app.exoplanethunter.exoplanet.domain.usecase.GetStarSystemUseCase
import com.app.exoplanethunter.testing.FakeAnalyticsRepository
import com.app.exoplanethunter.testing.FakeExoplanetRepository
import com.app.exoplanethunter.testing.MainDispatcherRule
import com.app.exoplanethunter.testing.starSystem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StarSystemDetailViewModelTest {

    @get:Rule
    val main = MainDispatcherRule()

    private val repo = FakeExoplanetRepository()
    private val analytics = FakeAnalyticsRepository()

    @Test
    fun `orrery toggles are tracked with the system and the new state`() = runTest(main.dispatcher) {
        repo.starSystems[7L] = starSystem(host = "HD 189733")
        val vm = StarSystemDetailViewModel(GetStarSystemUseCase(repo), TrackEventUseCase(analytics))
        vm.loadSystem(7L)
        advanceUntilIdle()

        vm.onOrreryToggle(OrreryToggle.HABITABLE_ZONE, enabled = true)
        vm.onOrreryToggle(OrreryToggle.TILT_3D, enabled = false)

        assertEquals(
            listOf(
                AnalyticsEvent.OrreryToggleChanged("HD 189733", OrreryToggle.HABITABLE_ZONE, true),
                AnalyticsEvent.OrreryToggleChanged("HD 189733", OrreryToggle.TILT_3D, false),
            ),
            analytics.events.filterIsInstance<AnalyticsEvent.OrreryToggleChanged>(),
        )
    }
}
