package it.lismove.app.android.logWall.repository

import it.lismove.app.android.logWall.apiService.LogWallApi

class LogWallRepositoryImpl(
    val api: LogWallApi
): LogWallRepository {
    override suspend fun getLogWallEvents(): List<String> {
        return  api.getLogWallItems()
    }
}