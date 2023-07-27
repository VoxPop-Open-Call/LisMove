package net.nextome.lismove_sdk.sessionPoints

import android.util.Log
import com.google.maps.android.PolyUtil
import it.lismove.app.room.dao.EnrollmentDao
import it.lismove.app.room.dao.OrganizationSessionPointDao
import it.lismove.app.room.entity.EnrollmentEntity
import it.lismove.app.room.entity.OrganizationEntity
import it.lismove.app.room.entity.QueryData.EnrollmentWithOrganization
import it.lismove.app.room.entity.OrganizationSessionPointEntity
import it.lismove.app.room.entity.QueryData.EnrollmentWithOrganizationAndSettings
import it.lismove.app.room.entity.SettingsEntity
import net.nextome.lismove_sdk.sessionPoints.data.PointsSummary
import net.nextome.lismove_sdk.sessionPoints.data.SessionPoints
import net.nextome.lismove_sdk.sessionPoints.parser.asPointsSummary
import net.nextome.lismove_sdk.sessionPoints.useCase.PointsManagerUseCase
import net.nextome.lismove_sdk.utils.asLatLngLists
import java.util.*
import kotlin.time.milliseconds
class PointsManagerImpl(
    private val pointsManagerUseCase: PointsManagerUseCase
): PointsManager {
    var initiatives: List<EnrollmentWithOrganizationAndSettings> = listOf()
    var startingPoint = SessionPoints(0.0, 0, arrayListOf())
    var lastDistanceInKm = 0.0

    override suspend fun initManager(userId: String, sessionId: String) {
        Log.d("PointsManagerImpl","Initializing session userId: $userId ; sessionId: $sessionId")
        val date = System.currentTimeMillis()
        val totalDistance = pointsManagerUseCase.getSessionTotalDistance(sessionId)
        val nationalPoints = pointsManagerUseCase.getSessionNationalPoints(sessionId)
        val points = pointsManagerUseCase.getInitiativePoints(sessionId)

        startingPoint = SessionPoints(0.0, 0, arrayListOf())
        startingPoint.nationalKm = totalDistance
        startingPoint.nationalPoints = nationalPoints

        lastDistanceInKm = startingPoint.nationalKm
        initiatives = pointsManagerUseCase.getActiveInitiative(userId, date)

        for (initiative in initiatives){
            val savedPoint = points.firstOrNull {  it.organizationId == initiative.organization.id }
            Log.d("PointsManagerImpl"," savedPoint ${savedPoint?.sessionId}")
            startingPoint.initiativePointOrganizations.add(OrganizationSessionPointEntity(
                organizationId = initiative.organization.id,
                multiplier = initiative.settings.multiplier.toDouble() ,
                sessionId = sessionId,
                distance = savedPoint?.distance ?: 0.0,
                points = savedPoint?.points ?: 0,
                multiplierPoints = savedPoint?.multiplierPoints ?: 0,
                multiplierDistance = savedPoint?.multiplierDistance ?: 0.0
            ))
        }

        Log.d("PointsManagerImpl","Init: ${startingPoint.nationalPoints} initiative: ${startingPoint.initiativePointOrganizations.sumOf { it.points }}")


    }

    //distanceInKm are the totals
    override suspend fun updatePoints(totalDistance: Double, lat: Double?, lng: Double?, timestamp: Long): PointsSummary {

            val deltaDistance =  totalDistance - lastDistanceInKm
            lastDistanceInKm = totalDistance
            if(deltaDistance == 0.0){return getPointSummary() }
            addNationalKm(deltaDistance)

            if(lat != null && lng != null){
                initiatives.filter { it.organization.geojson.isNullOrEmpty().not() }.forEach { initiative ->
                    initiative.organization.getGeoJsonCoordinates().asLatLngLists().forEach {
                        if(PolyUtil.containsLocation(lat, lng, it, false)){
                            addInitiativeKm(deltaDistance, initiative, timestamp)
                            return@forEach
                        }
                    }
                }
            }

        saveOnRoom()
        return getPointSummary()
    }


    //The location is urban if it is in an initiative polygon
    override suspend fun isLocationUrban(lat: Double?, lng: Double?): Boolean {
        if(lat == null || lng == null) return false
        val flag = false
        initiatives.filter { it.organization.geojson.isNullOrEmpty().not() }.forEach { initiative ->
            initiative.organization.getGeoJsonCoordinates().asLatLngLists().forEach {
                if(PolyUtil.containsLocation(lat, lng, it, false)){
                    return true
                }
            }
        }
        return flag
    }

    private suspend fun saveOnRoom(){
        //Log.d("PointsManagerImp", "Saving data on room")
        pointsManagerUseCase.savePoints(startingPoint.initiativePointOrganizations)
    }

    private fun getPointSummary(): PointsSummary {
        val summary =  startingPoint.asPointsSummary()
        //Log.d("PointsManagerImpl", "summary is $summary")
        return summary
    }

    private fun addNationalKm(distanceInKm: Double){
        startingPoint.nationalKm += distanceInKm
        startingPoint.nationalPoints = startingPoint.nationalKm.times(10).toInt()
    }

    private fun addInitiativeKm(distanceInKm: Double,
                                initiative: EnrollmentWithOrganizationAndSettings,
                                timestamp: Long){
        //Log.d("PointsManagerImpl", "add is $distanceInKm")

        startingPoint.initiativePointOrganizations.first { it.organizationId == initiative.organization.id }.apply {
            val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }.time
            val multiplier = initiative.settings.getActiveMultiplier(calendar)
            distance += distanceInKm
            if(multiplier != 1){
                multiplierDistance += distanceInKm
            }
            multiplierPoints = this.multiplierDistance.times(10).toInt().times(multiplier)
            points = distance.minus(multiplierDistance).times(10).toInt() + multiplierPoints
           // Log.d("PointsManagerImpl", " distance: ${distance} multiplier distance ${multiplierDistance} points = $points multiplierPoints $multiplierPoints")

        }
    }

}

