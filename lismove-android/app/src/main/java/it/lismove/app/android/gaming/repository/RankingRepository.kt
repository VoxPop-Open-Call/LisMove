package it.lismove.app.android.gaming.repository

import it.lismove.app.android.gaming.apiService.data.Ranking

interface RankingRepository {
    suspend fun getMainRanking(): Ranking
    suspend fun getRanking(rid: Long): Ranking
    suspend fun getUserRankings(uid: String): List<Ranking>
}