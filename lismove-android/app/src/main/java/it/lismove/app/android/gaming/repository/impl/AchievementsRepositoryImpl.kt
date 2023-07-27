package it.lismove.app.android.gaming.repository.impl

import it.lismove.app.android.authentication.apiService.UserApi
import it.lismove.app.android.authentication.repository.UserRepository
import it.lismove.app.android.gaming.apiService.data.Achievement
import it.lismove.app.android.gaming.repository.AchievementRepository

class AchievementsRepositoryImpl(
    private val userApi: UserApi,
    private val userRepository: UserRepository
): AchievementRepository {
    override suspend fun getAchievements(uid: String): List<Achievement> {
       /* val initiatives = userRepository.getInitiatives(uid)
        val achievements = userApi.getAchievements(uid)
        return achievements.map { achievement ->
            val initiative = initiatives.firstOrNull { it.organization.id == achievement.organization }
             AchievementWithOrganization(achievement, initiative?.organization)
        }*/
        return userApi.getAchievements(uid)
    }

    //TODO: Change this
    override suspend fun hasActiveAchievement(uid: String): Boolean {
        val achievements = userApi.getAchievements(uid)
        return achievements.isNotEmpty()

    }
}