package com.aditya.smartmedbox.feature.history.domain.usecase

import app.cash.turbine.test
import com.aditya.smartmedbox.feature.history.domain.repository.DoseEventRepository
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class GetWeeklyMissedCountUseCaseTest {

    private val doseEventRepository: DoseEventRepository = mockk()
    private lateinit var useCase: GetWeeklyMissedCountUseCase

    @Before
    fun setUp() {
        useCase = GetWeeklyMissedCountUseCase(doseEventRepository)
    }

    @Test
    fun `invoke queries repository with 7-days ago threshold`() = runTest {
        val now = 1700000000000L
        val expectedSevenDaysAgo = now - 7L * 24 * 60 * 60 * 1000L

        every { doseEventRepository.getMissedEventsCount(expectedSevenDaysAgo) } returns flowOf(3)

        useCase(now).test {
            val count = awaitItem()
            assertEquals(3, count)
            awaitComplete()
        }
    }
}
