package it.lismove.app.android.session

import it.lismove.app.android.session.apiService.PartialRequest
import it.lismove.app.android.session.apiService.SessionRequest
import it.lismove.app.android.session.parser.asSessionPoint
import it.lismove.app.room.entity.OrganizationSessionPointEntity
import it.lismove.app.room.entity.PartialSessionDataEntity
import it.lismove.app.room.entity.SessionDataEntity


object SessionHelper {
    fun buildSessionRequest(userUid: String,
                            sessionDataEntity: SessionDataEntity,
                            partials: List<PartialSessionDataEntity>,
                            points: List<OrganizationSessionPointEntity>
    ) : SessionRequest {

        val partialsList = partials.map {
            return@map PartialRequest(
                    timestamp = it.timestamp,
                    altitude = it.altitude,
                    latitude = it.latitude,
                    longitude = it.longitude,
                    deltaRevs = it.deltaRevs,
                    type = it.type,
                    sensorDistance = it.gyroDeltaDistance,
                    urban = it.urban,
                    rawData_ts = it.rawData_ts,
                    rawData_wheel = it.rawData_wheel,
                    extra = it.extra,
            )
        }.sortedBy { it.timestamp }

        val sessionPoints = points.map { it.asSessionPoint() }
/*
        partialsList.lastOrNull()?.let { lastPartial ->
            if( lastPartial.type != PartialSessionDataEntity.PARTIAL_TYPE_END){
                val lastPartialWithPosition = partialsList.lastOrNull{
                    hasNotNullOrZeroCoordinates(it.latitude, it.longitude)
                }

                val lastPartialNotDebug = partialsList.lastOrNull{
                    it.type in (PartialSessionDataEntity.PARTIAL_TYPE_START..PartialSessionDataEntity.PARTIAL_TYPE_RESUME)
                }

                lastPartialNotDebug?.let {
                    partialsList.add(lastPartialNotDebug.copy(
                        timestamp = lastPartial.timestamp + 1,
                        type = PartialSessionDataEntity.PARTIAL_TYPE_END,
                        latitude = lastPartialWithPosition?.latitude ?: 0.0,
                        longitude = lastPartialWithPosition?.longitude ?: 0.0
                    ))
                }
            }
        }
         private fun hasNotNullOrZeroCoordinates(latitude: Double?, longitude: Double?): Boolean{
        return latitude != null && latitude != 0.0 && longitude != null && longitude != 0.0

    }

 */

        val session =  SessionRequest(
            uid = userUid,
            startBattery = sessionDataEntity.startBattery ?: 0,
            endBattery = sessionDataEntity.endBattery ?: 0,
            startTime = sessionDataEntity.startTime!!,
            endTime = sessionDataEntity.endTime!!,
            phoneStartBattery = sessionDataEntity.phoneStartBattery,
            phoneEndBattery = sessionDataEntity.phoneEndBattery,
            gyroDistance = sessionDataEntity.gyroDistance ?: 0.0,
            partials = partialsList,
            description = "Sessione di test del ${sessionDataEntity.startTime}",
            nationalPoints = sessionDataEntity.nationalPoints ?: 0,
            sessionPoints =  sessionPoints,
            firmware = sessionDataEntity.firmwareVersion,
            gpsOnlyDistance = sessionDataEntity.gpsOnlyDistance,
            duration = sessionDataEntity.duration,
            hubCoefficient = sessionDataEntity.hubCoefficient
        )

        return session
    }


}