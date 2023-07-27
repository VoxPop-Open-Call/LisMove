package net.nextome.lismove_sdk.sessionPoints

import it.lismove.app.room.entity.OrganizationSessionPointEntity
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.runBlocking
import net.nextome.lismove_sdk.sessionPoints.data.SessionPoints
import net.nextome.lismove_sdk.sessionPoints.useCase.mock.PointsManagerUseCaseRestoredMockImpl
import net.nextome.lismove_sdk.sessionPoints.useCase.mock.PointsManagerUseCaseDayBonusMockImpl
import net.nextome.lismove_sdk.sessionPoints.useCase.mock.PointsManagerUseCaseRestoredWithBonusMockImpl
import net.nextome.lismove_sdk.sessionPoints.useCase.mock.PointsManagerUseCaseSimpleMockImpl
import org.joda.time.DateTime
import org.junit.Test

class PointsManagerTest{

    //NEW SESSION

    @Test
    fun `new session is initialized correctly`(){
        runBlocking {
            val pointsManager = PointsManagerImpl(PointsManagerUseCaseSimpleMockImpl())
            pointsManager.initManager("123", "1234")
            val initialPoint = pointsManager.startingPoint
            assertEquals(SessionPoints(0.0, 0, arrayListOf(
                OrganizationSessionPointEntity(
                    0.0,
                    2.0,
                    1L,
                    0,
                    0.0,
                    0,
                    sessionId = "1234"
                ),

            )), initialPoint)
        }
    }

    @Test
    fun `national and initiative points are assigned`() {
        runBlocking {
            val pointsManager = PointsManagerImpl(PointsManagerUseCaseSimpleMockImpl())
            pointsManager.initManager("123", "1234")

            pointsManager.updatePoints(1.12,41.042866, 16.884391, System.currentTimeMillis()  )
            pointsManager.updatePoints(1.45,41.042866, 16.884391 , System.currentTimeMillis()  )
            pointsManager.updatePoints(3.0, 41.893951, 12.511277,  System.currentTimeMillis() )
            var total = pointsManager.updatePoints(3.2,41.042866, 16.884391, System.currentTimeMillis()  )
            assertEquals(16, total.initiativePoints)
            assertEquals(32, total.nationalPoints)
        }
    }

    @Test
    fun `national and initiative points are assigned with decimal`() {
        runBlocking {
            val pointsManager = PointsManagerImpl(PointsManagerUseCaseSimpleMockImpl())
            pointsManager.initManager("123", "1234")

            pointsManager.updatePoints(1.12,41.042866, 16.884391, System.currentTimeMillis()  )
            pointsManager.updatePoints(1.95,41.042866, 16.884391 , System.currentTimeMillis()  )
            pointsManager.updatePoints(3.0, 41.893951, 12.511277,  System.currentTimeMillis() )
            var total = pointsManager.updatePoints(3.26,41.042866, 16.884391, System.currentTimeMillis()  )
            assertEquals(22, total.initiativePoints)
            assertEquals(32, total.nationalPoints)
        }
    }

    // RESTORED SESSION

    @Test
    fun `restored session is initialized correctly`(){
        runBlocking {
            val pointsManager = PointsManagerImpl(PointsManagerUseCaseRestoredMockImpl())
            pointsManager.initManager("123", "1234")
            val initialPoint = pointsManager.startingPoint
            assertEquals(SessionPoints(1.1, 11, arrayListOf(
                OrganizationSessionPointEntity(
                    0.8,
                    1.0,
                    1L,
                    8,
                    0.0,
                    0,
                    sessionId = "1234"
                ),

                )), initialPoint)
        }
    }

    @Test
    fun `in restored national and all initiative points are assigned`() {
        runBlocking {
            val pointsManager = PointsManagerImpl(PointsManagerUseCaseRestoredMockImpl())
            pointsManager.initManager("123", "1234")
            val startingKm = pointsManager.startingPoint.nationalKm
            val initiativeKm = pointsManager.startingPoint.initiativePointOrganizations.first().distance
            pointsManager.updatePoints(1.12,41.042866, 16.884391, System.currentTimeMillis()  )
            pointsManager.updatePoints(1.45,41.042866, 16.884391 , System.currentTimeMillis()  )
            pointsManager.updatePoints(3.0, 41.893951, 12.511277,  System.currentTimeMillis() )
            var total = pointsManager.updatePoints(3.2,41.042866, 16.884391, System.currentTimeMillis()  )
            assertEquals(32, total.nationalPoints)
            assertEquals((1.45 - startingKm).plus(0.2).plus(initiativeKm).times(10).toInt() , total.initiativePoints)
        }
    }

    @Test
    fun `national and all initiative points are assigned with day bonus`() {
        runBlocking {
            val pointsManager = PointsManagerImpl(PointsManagerUseCaseDayBonusMockImpl())
            pointsManager.initManager("123", "1234")

            pointsManager.updatePoints(1.12,41.042866, 16.884391, System.currentTimeMillis()  )
            pointsManager.updatePoints(1.45,41.042866, 16.884391 , System.currentTimeMillis()  )
            pointsManager.updatePoints(3.0, 41.893951, 12.511277,  System.currentTimeMillis() )
            var total = pointsManager.updatePoints(3.2,41.042866, 16.884391, System.currentTimeMillis()  )
            assertEquals(32, total.nationalPoints)
            assertEquals(48, total.initiativePoints)


        }
    }

    @Test
    fun `national and some initiative points are assigned with day bonus`() {
        runBlocking {
            val pointsManager = PointsManagerImpl(PointsManagerUseCaseDayBonusMockImpl())
            pointsManager.initManager("123", "1234")

            pointsManager.updatePoints(1.12,41.042866, 16.884391, DateTime.now().minusDays(2).millis )
            pointsManager.updatePoints(1.45,41.042866, 16.884391 , System.currentTimeMillis()  )
            pointsManager.updatePoints(3.0, 41.893951, 12.511277,  System.currentTimeMillis() )
            var total = pointsManager.updatePoints(3.2,41.042866, 16.884391, System.currentTimeMillis()  )
            assertEquals(11+5*3, total.initiativePoints)
            assertEquals(32, total.nationalPoints)
        }
    }

    @Test
    fun `restored with bonus session is initialized correctly`(){
        runBlocking {
            val pointsManager = PointsManagerImpl(PointsManagerUseCaseRestoredWithBonusMockImpl())
            pointsManager.initManager("123", "1234")
            val initialPoint = pointsManager.startingPoint
            assertEquals(SessionPoints(1.1, 11, arrayListOf(
                OrganizationSessionPointEntity(
                    0.8,
                    3.0,
                    1L,
                    22,
                    0.7,
                    21,
                    sessionId = "1234",
                ),

                )), initialPoint)
        }
    }

    @Test
    fun `in restored national and all initiative points are assigned with day bonus`() {
        runBlocking {
            val pointsManager = PointsManagerImpl(PointsManagerUseCaseRestoredWithBonusMockImpl())
            pointsManager.initManager("123", "1234")

            val x = 1.45.minus(1.1).plus(0.2).plus(0.7).times(10).toInt().times(3).plus(1)
            pointsManager.updatePoints(1.12,41.042866, 16.884391, System.currentTimeMillis()  )
            pointsManager.updatePoints(1.45,41.042866, 16.884391 , System.currentTimeMillis()  )
            pointsManager.updatePoints(3.0, 41.893951, 12.511277,  System.currentTimeMillis() )
            var total = pointsManager.updatePoints(3.2,41.042866, 16.884391, System.currentTimeMillis()  )

            assertEquals(32, total.nationalPoints)
            assertEquals(x, total.initiativePoints)


        }
    }

}