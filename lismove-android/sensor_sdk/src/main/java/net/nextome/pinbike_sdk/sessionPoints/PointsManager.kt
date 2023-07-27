package net.nextome.lismove_sdk.sessionPoints

import net.nextome.lismove_sdk.sessionPoints.data.PointsSummary

interface PointsManager {
    suspend fun initManager(userId: String, sessionId: String)
    suspend fun updatePoints(totalDistance: Double,
                             lat: Double?,
                             lng: Double?,
                             timestamp: Long): PointsSummary
    suspend fun isLocationUrban(lat: Double?, lng: Double?): Boolean
}

