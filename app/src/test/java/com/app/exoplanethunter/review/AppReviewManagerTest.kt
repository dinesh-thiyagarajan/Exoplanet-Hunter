package com.app.exoplanethunter.review

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppReviewManagerTest {

    private val day = 24 * 60 * 60 * 1000L
    private val now = 1_000_000_000_000L

    private fun due(launches: Int = 5, firstDaysAgo: Long = 90, lastDaysAgo: Long? = null) =
        AppReviewManager.isReviewDue(
            launchCount = launches,
            firstLaunchMillis = now - firstDaysAgo * day,
            lastRequestedMillis = lastDaysAgo?.let { now - it * day } ?: 0L,
            nowMillis = now,
            intervalDays = 60,
        )

    @Test
    fun `a fresh install isn't asked until 60 days of use`() {
        assertFalse(due(firstDaysAgo = 0))
        assertFalse(due(firstDaysAgo = 59))
        assertTrue(due(firstDaysAgo = 60))
        assertFalse(due(firstDaysAgo = 90, launches = 2))
    }

    @Test
    fun `asked again only after 60 days`() {
        assertFalse(due(lastDaysAgo = 10))
        assertFalse(due(lastDaysAgo = 59))
        assertTrue(due(lastDaysAgo = 60))
        assertTrue(due(lastDaysAgo = 200))
    }
}
