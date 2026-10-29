package com.app.exoplanethunter.update

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class UpdatePolicyTest {

    private val now = 1_000_000_000_000L
    private val day = 24 * 60 * 60 * 1000L

    private fun choose(
        installed: Long = 41,
        min: Long = 0,
        priority: Int = 0,
        immediate: Boolean = true,
        flexible: Boolean = true,
        declined: Long = 0,
    ) = UpdatePolicy.choose(installed, min, priority, immediate, flexible, declined, now)

    @Test
    fun `an ordinary new version is offered as a flexible update`() {
        assertEquals(UpdateMode.FLEXIBLE, choose())
    }

    @Test
    fun `versions below the remote minimum must update immediately`() {
        assertEquals(UpdateMode.IMMEDIATE, choose(installed = 40, min = 41))
    }

    @Test
    fun `the minimum version is exclusive`() {
        assertEquals(UpdateMode.FLEXIBLE, choose(installed = 41, min = 41))
    }

    @Test
    fun `a high-priority release is immediate`() {
        assertEquals(UpdateMode.IMMEDIATE, choose(priority = 4))
        assertEquals(UpdateMode.FLEXIBLE, choose(priority = 3))
    }

    @Test
    fun `after a decline nothing is offered for three days`() {
        assertNull(choose(declined = now - 2 * day))
        assertEquals(UpdateMode.FLEXIBLE, choose(declined = now - 4 * day))
    }

    @Test
    fun `a required update is asked again even right after a decline`() {
        assertEquals(UpdateMode.IMMEDIATE, choose(installed = 40, min = 41, declined = now - day))
    }

    @Test
    fun `a decline also quiets high-priority releases that aren't required`() {
        assertNull(choose(priority = 5, declined = now - day))
    }

    @Test
    fun `falls back to what Play allows`() {
        assertEquals(UpdateMode.FLEXIBLE, choose(priority = 5, immediate = false))
        assertNull(choose(flexible = false))
        assertNull(choose(immediate = false, flexible = false))
    }
}
