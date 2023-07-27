package it.lismove.app.android.gaming.apiService

import it.lismove.app.android.awards.data.AwardAchievement
import retrofit2.http.GET
import retrofit2.http.Path

interface AchievementApi {
    @GET("/achievements/{aid}/awards")
    suspend fun getAwards(@Path(value = "aid")aid: Long): List<AwardAchievement>
}