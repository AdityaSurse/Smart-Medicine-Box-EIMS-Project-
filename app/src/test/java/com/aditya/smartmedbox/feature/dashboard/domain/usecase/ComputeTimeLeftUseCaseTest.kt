package com.aditya.smartmedbox.feature.dashboard.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Test

class ComputeTimeLeftUseCaseTest {

    private val useCase = ComputeTimeLeftUseCase()

    @Test
    fun `invoke with time within 2 minutes returns Due now`() {
        val now = 1000000000L
        val scheduled = now + 60 * 1000L // 1 min in future

        val result = useCase(scheduledTimeMillis = scheduled, currentTimeMillis = now)
        assertEquals("Due now", result)
    }

    @Test
    fun `invoke with future minutes returns in Xm`() {
        val now = 1000000000L
        val scheduled = now + 45 * 60 * 1000L // 45 min in future

        val result = useCase(scheduledTimeMillis = scheduled, currentTimeMillis = now)
        assertEquals("in 45m", result)
    }

    @Test
    fun `invoke with future hours and minutes returns in Xh Ym`() {
        val now = 1000000000L
        val scheduled = now + (2 * 60 + 15) * 60 * 1000L // 2h 15m in future

        val result = useCase(scheduledTimeMillis = scheduled, currentTimeMillis = now)
        assertEquals("in 2h 15m", result)
    }

    @Test
    fun `invoke with past time returns overdue duration`() {
        val now = 1000000000L
        val scheduled = now - 42 * 60 * 1000L // 42 min ago

        val result = useCase(scheduledTimeMillis = scheduled, currentTimeMillis = now)
        assertEquals("42m overdue", result)
    }
}
