package it.lismove.app.android.session.parser

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import it.lismove.app.android.R
import it.lismove.app.common.DateTimeUtils
import it.lismove.app.android.session.data.Session
import it.lismove.app.android.session.data.SessionDashBoardData
import it.lismove.app.room.entity.PartialSessionDataEntity
import it.lismove.app.room.entity.QueryData.EnrollmentWithOrganizationAndSettings
import it.lismove.app.room.entity.QueryData.SessionWithPoints
import it.lismove.app.room.entity.SessionDataEntity


fun PartialSessionDataEntity.asSessionDashBoardData(initiatives: List<EnrollmentWithOrganizationAndSettings>): SessionDashBoardData {
    val date = DateTimeUtils.getCurrentDate()
    val multiplier = initiatives.map { it.settings.getActiveMultiplier(date) }.maxOrNull() ?: 1

    return SessionDashBoardData(
            time = this.getReadableElapsedTime(),
            distance = this.getTotalDistanceReadable(),
            speed = getSpeedReadable(),
            avgSpeed = getAvgSpeedReadable(),
            nationalPoints = nationalPoints.toString(),
            initiativePoints = initiativePoints.toString(),
            sensorBatteryIcon = batteryLevel?.getBatteryLevelImage(),
            isSensorBatteryAvailable = batteryLevel != -1,
            isGps = isGpsPartial,
            activeInitiatives = initiatives.size,
            urban = urban,
            multiplierValue = "x$multiplier",
            multiplierLabelEnd = "(x${initiatives.size})",
            showMultiplier = multiplier != 1
    )
}
fun SessionWithPoints.asSession(): Session{
    val points = pointOrganizations.map { it.asSessionPoint() }
    return this.session.asSession().apply {
        sessionPoints = points
    }
}
fun SessionDataEntity.asSession(): Session{
    return Session(
        id = id,
        uid = userId,
        startBattery = startBattery,
        endBattery = endBattery,
        firmwareVersion = firmwareVersion,
        phoneStartBattery = phoneStartBattery ?: 0,
        phoneEndBattery = phoneEndBattery ?: 0,
        startTime = startTime ?: 0,
        endTime = endTime ?: 0,
        gpsOnlyDistance = gpsOnlyDistance,
        gyroDistance = gyroDistance,
        partials = listOf(), //TODO: Check this
        totalKm = totalKm ?: 0.0,
        description = description,
        type = type,
        polyline = Gson().fromJson(polyline, object : TypeToken<List<String?>?>() {}.type),
        nationalPoints = nationalPoints,
        sessionPoints = listOf(),
        valid = valid ?: false,
        euro = euro,
        certificated = certificated,
        homeWorkPath = homeWorkPath,
        status = statusDescription,
        co2 = co2,
        gmapsDistance = gmapsDistance,
        uploaded = status == SessionDataEntity.SESSION_STATUS_UPLOADED,
        nationalKm = nationalKm,
        duration = duration,
        verificationRequired = verificationRequired,
        verificationRequiredNote = verificationRequiredNote
    )
}
fun Int.getBatteryLevelImage(): Int{
    if(this > 75){
        return R.drawable.battery_full
    }else if(this in 51..75){
        return R.drawable.battery_mid
    } else{
        return R.drawable.battery_low
    }
}

//0-50; 50-75 ; > 75


fun Session.asSessionDataEntity(): SessionDataEntity {
    return SessionDataEntity(
        id!!,
        SessionDataEntity.SESSION_STATUS_UPLOADED,
        SessionDataEntity.SESSION_STATUS_UPLOADED,
        this.uid,
        firmwareVersion = firmwareVersion,
        startBattery = startBattery,
        endBattery = endBattery,
        phoneStartBattery = phoneStartBattery ?: 0,
        phoneEndBattery = phoneEndBattery ?: 0,
        startTime = startTime ?: 0,
        endTime = endTime ?: 0,
        gpsOnlyDistance = gpsOnlyDistance,
        gyroDistance = gyroDistance,
        totalKm = totalKm ?: 0.0,
        description = description,
        type = type,
        polyline = Gson().toJson(polyline),
        nationalPoints = nationalPoints,
        nationalKm = nationalKm,
        valid = valid ?: false,
        euro = euro,
        certificated = certificated,
        homeWorkPath = homeWorkPath,
        statusDescription = status ?: null,
        co2 = co2,
        gmapsDistance = gmapsDistance,
        verificationRequired = verificationRequired,
        verificationRequiredNote = verificationRequiredNote
    )
}