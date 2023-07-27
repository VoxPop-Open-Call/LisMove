package it.lismove.app.android.logWall.repository

interface LogWallRepository {
    suspend fun getLogWallEvents(): List<String>
}