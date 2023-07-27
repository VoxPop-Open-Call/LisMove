package it.lismove.app.android.awards.repository

import it.lismove.app.android.awards.data.AwardAchievement
import it.lismove.app.android.awards.data.AwardRanking

interface AwardRepository {
    suspend fun getAwardsByRankingId(rid: Long): List<AwardRanking>
    suspend fun getAwardsByAchievementId(aid: Long): List<AwardAchievement>
}