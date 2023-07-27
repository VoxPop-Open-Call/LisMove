package it.lismove.app.android.gaming.repository.impl

import it.lismove.app.android.authentication.apiService.UserApi
import it.lismove.app.android.gaming.apiService.RankingApi
import it.lismove.app.android.gaming.apiService.data.Ranking
import it.lismove.app.android.gaming.repository.RankingRepository

class RankingRepositoryImpl(
    val rankingApi: RankingApi,
    val userApi: UserApi
): RankingRepository {
    override suspend fun getMainRanking(): Ranking {
        return rankingApi.getGlobal()
    }

    override suspend fun getRanking(rid: Long): Ranking {
        return rankingApi.getRanking(rid, true)
    }

    override suspend fun getUserRankings(uid: String): List<Ranking> {
        return userApi.getRankings(uid)
    }

}