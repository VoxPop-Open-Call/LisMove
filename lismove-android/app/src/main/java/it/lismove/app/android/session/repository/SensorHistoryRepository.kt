package it.lismove.app.android.session.repository

import net.nextome.lismove_sdk.models.LisMoveOfflineHistoryData
import net.nextome.lismove_sdk.models.LisMoveSensorHistoryElement

interface SensorHistoryRepository {
    suspend fun sendSessionHistory(history: List<LisMoveSensorHistoryElement>, userId: String?, sensorName: String?)
}