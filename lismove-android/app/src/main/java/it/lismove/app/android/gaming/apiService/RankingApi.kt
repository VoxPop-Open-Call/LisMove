package it.lismove.app.android.gaming.apiService

import it.lismove.app.android.awards.data.AwardRanking
import it.lismove.app.android.gaming.apiService.data.Ranking
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface RankingApi {
    @GET("/rankings/global")
    suspend fun getGlobal(): Ranking

    @GET("/rankings/{rid}")
    suspend fun getRanking(
        @Path(value = "rid") rid: Long,
        @Query(value = "withUsers") withUsers: Boolean
    ): Ranking

    @GET("/rankings/{rid}/awards")
    suspend fun getAwards(
        @Path(value = "rid") rid: Long
    ): List<AwardRanking>
}