package it.lismove.app.android.general.network

import it.lismove.app.android.authentication.apiService.UserApi
import it.lismove.app.android.car.apiService.CarDataApi
import it.lismove.app.android.deviceConfiguration.apiService.SensorApi
import it.lismove.app.android.gaming.apiService.AchievementApi
import it.lismove.app.android.initiative.apiService.OrganizationApi
import it.lismove.app.android.gaming.apiService.RankingApi
import it.lismove.app.android.logWall.apiService.LogWallApi
import it.lismove.app.android.session.apiService.SensorHistoryApi
import it.lismove.app.android.session.apiService.SessionApi
import okhttp3.Authenticator
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import retrofit2.Retrofit

object NetworkKoinModule{
    fun getModule() = module {

        factory { AuthInterceptor(get()) }
        factory<Authenticator> { LisMoveAuthenticator(get()) }
        factory { AppDebugInterceptor(androidContext()) }
        factory { OkHttpClientProvider.getOkHttpClient(get(),get(), get()) }
        single { RetrofitProvider.getRetrofit(get()) }

        factory { provideUserService(get()) }
        factory { provideSessionService(get()) }
        factory { provideSensorService(get()) }
        factory { provideOrganizationService(get()) }
        factory { provideSensorHistoryService(get()) }
        factory { provideRankingService(get()) }
        factory { provideLogWallService(get()) }
        factory { provideAchievementService(get()) }
        factory { provideCarService(get()) }


    }

    private fun provideSensorHistoryService(retrofit: Retrofit): SensorHistoryApi = retrofit.create(SensorHistoryApi::class.java)
    private fun provideUserService(retrofit: Retrofit): UserApi = retrofit.create(UserApi::class.java)
    private fun provideSessionService(retrofit: Retrofit): SessionApi = retrofit.create(SessionApi::class.java)
    private fun provideSensorService(retrofit: Retrofit): SensorApi = retrofit.create(SensorApi::class.java)
    private fun provideOrganizationService(retrofit: Retrofit): OrganizationApi = retrofit.create(OrganizationApi::class.java)
    private fun provideRankingService(retrofit: Retrofit): RankingApi = retrofit.create(RankingApi::class.java)
    private fun provideLogWallService(retrofit: Retrofit): LogWallApi = retrofit.create(LogWallApi::class.java)
    private fun provideCarService(retrofit: Retrofit): CarDataApi = retrofit.create(CarDataApi::class.java)
    private fun provideAchievementService(retrofit: Retrofit): AchievementApi = retrofit.create(
        AchievementApi::class.java
    )


}