package it.lismove.app.android.session.repository

import it.lismove.app.android.session.apiService.SensorHistoryApi
import it.lismove.app.android.session.apiService.asOfflineDataRequest
import net.nextome.lismove_sdk.models.LisMoveOfflineHistoryData
import net.nextome.lismove_sdk.models.LisMoveSensorHistoryElement

class SensorHistoryRepositoryImpl(
    val api: SensorHistoryApi,
): SensorHistoryRepository {
    override suspend fun sendSessionHistory(history: List<LisMoveSensorHistoryElement>, userId: String?, sensorName: String?) {
        api.sendSensorHistory(history.map { it.asOfflineDataRequest(userId, sensorName) })
    }
}