package it.lismove.app.android.awards.repository

import it.lismove.app.android.awards.data.AwardAchievement
import it.lismove.app.android.awards.data.AwardRanking
import it.lismove.app.android.gaming.apiService.AchievementApi
import it.lismove.app.android.gaming.apiService.RankingApi

class AwardRepositoryImpl(
    val rankingApi: RankingApi,
    val achievementApi: AchievementApi
): AwardRepository {
    override suspend fun getAwardsByRankingId(rid: Long): List<AwardRanking> {
        return rankingApi.getAwards(rid)
    }

    override suspend fun getAwardsByAchievementId(aid: Long): List<AwardAchievement> {
        return achievementApi.getAwards(aid)
    }
}