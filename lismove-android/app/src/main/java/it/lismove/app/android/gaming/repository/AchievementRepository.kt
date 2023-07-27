package it.lismove.app.android.gaming.repository

import it.lismove.app.android.gaming.apiService.data.Achievement
import it.lismove.app.android.gaming.data.AchievementWithOrganization

interface AchievementRepository {
    suspend fun getAchievements(uid: String): List<Achievement>
    suspend fun hasActiveAchievement(uid: String): Boolean
}