package net.nextome.lismove_sdk

import it.lismove.app.room.entity.PartialSessionDataEntity
import it.lismove.app.room.entity.PartialSessionDataEntity.Companion.PARTIAL_TYPE_BLE
import net.nextome.lismove_sdk.utils.SensorDataManager
import org.junit.Assert.*

import org.junit.After
import org.junit.Before
import org.junit.Test

class SensorDataManagerTest {
    val wheelCircunference: Double = 700.0
    val sessionId: String = "mock_session"
    val hubCoefficient: Double = 1.0

    val manager = SensorDataManager(wheelCircunference, sessionId, hubCoefficient)

    @Before
    fun setUp() {
    }

    @After
    fun tearDown() {
    }

    @Test
    fun `recover session when last partial is debug`() {
        val totalGyroDistanceInKmToRecover = 10.0
        val totalGpsOnlyDistanceInKmToRecover = 20.0
        val totalGpsCacheDistanceInKmToRecover = 30.0
        val sessionElapsedTimeInSecToRecover = 42

        val recoveredPartialsWithValidLastPartial = listOf<PartialSessionDataEntity>(
            PartialSessionDataEntity.getEmpty(sessionId).apply {
                timestamp = 0
                totalGyroDistanceInKm = 1.0
                totalGpsOnlyDistanceInKm = 2.0
                totalGpsCacheDistanceInKm = 3.0
                sessionElapsedTimeInSec = 39
            },
            PartialSessionDataEntity.getEmpty(sessionId).apply {
                timestamp = 1
            },
            // Partial to be recovered
            PartialSessionDataEntity.getEmpty(sessionId).apply {
                timestamp = 2
                totalGyroDistanceInKm = totalGyroDistanceInKmToRecover
                totalGpsOnlyDistanceInKm = totalGpsOnlyDistanceInKmToRecover
                totalGpsCacheDistanceInKm = totalGpsCacheDistanceInKmToRecover
                sessionElapsedTimeInSec = sessionElapsedTimeInSecToRecover
            },
            // Other debug partial
            PartialSessionDataEntity.getEmpty(sessionId).apply {
                isDebugPartial = true
                type = PARTIAL_TYPE_BLE
                extra = "Test Debug partial"
            })

        val result = manager.recoverFromSessionPartials(recoveredPartialsWithValidLastPartial)

        assertEquals(result, true)
        assertEquals(manager.totalGpsOnlyDistanceInKm, totalGpsOnlyDistanceInKmToRecover, 0.0)
        assertEquals(manager.totalGyroDistanceInKm, totalGyroDistanceInKmToRecover, 0.0)
        assertEquals(manager.totalGpsCacheDistanceInKm, totalGpsCacheDistanceInKmToRecover, 0.0)
        assertEquals(manager.sessionElapsedTimeInSec, sessionElapsedTimeInSecToRecover)
    }

    @Test
    fun `recover session when last partial is valid`() {
        val totalGyroDistanceInKmToRecover = 10.0
        val totalGpsOnlyDistanceInKmToRecover = 20.0
        val totalGpsCacheDistanceInKmToRecover = 30.0
        val sessionElapsedTimeInSecToRecover = 42

        val recoveredPartialsWithValidLastPartial = listOf<PartialSessionDataEntity>(
            PartialSessionDataEntity.getEmpty(sessionId).apply {
                timestamp = 0
                totalGyroDistanceInKm = 1.0
                totalGpsOnlyDistanceInKm = 2.0
                totalGpsCacheDistanceInKm = 3.0
                sessionElapsedTimeInSec = 39
            },
            PartialSessionDataEntity.getEmpty(sessionId).apply {
                timestamp = 1
                totalGyroDistanceInKm = 1.1
                totalGpsOnlyDistanceInKm = 2.1
                totalGpsCacheDistanceInKm = 3.1
                sessionElapsedTimeInSec = 40
            },
            // Partial to be recovered
            PartialSessionDataEntity.getEmpty(sessionId).apply {
                timestamp = 2
                totalGyroDistanceInKm = totalGyroDistanceInKmToRecover
                totalGpsOnlyDistanceInKm = totalGpsOnlyDistanceInKmToRecover
                totalGpsCacheDistanceInKm = totalGpsCacheDistanceInKmToRecover
                sessionElapsedTimeInSec = sessionElapsedTimeInSecToRecover
            })

        val result = manager.recoverFromSessionPartials(recoveredPartialsWithValidLastPartial)

        assertEquals(result, true)
        assertEquals(manager.totalGpsOnlyDistanceInKm, totalGpsOnlyDistanceInKmToRecover, 0.0)
        assertEquals(manager.totalGyroDistanceInKm, totalGyroDistanceInKmToRecover, 0.0)
        assertEquals(manager.totalGpsCacheDistanceInKm, totalGpsCacheDistanceInKmToRecover, 0.0)
        assertEquals(manager.sessionElapsedTimeInSec, sessionElapsedTimeInSecToRecover)
    }

    @Test
    fun `recover session return false when there are not valid partials`() {
        val recoveredPartialsWithValidLastPartial = listOf<PartialSessionDataEntity>()

        val result = manager.recoverFromSessionPartials(recoveredPartialsWithValidLastPartial)

        assertEquals(result, false)
    }
}