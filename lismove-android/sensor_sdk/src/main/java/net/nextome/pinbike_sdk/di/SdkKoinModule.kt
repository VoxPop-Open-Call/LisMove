package net.nextome.lismove_sdk.di

import net.nextome.lismove_sdk.LismoveBleManager
import net.nextome.lismove_sdk.database.DebugLogRepository
import net.nextome.lismove_sdk.database.DebugLogRepositoryImpl
import net.nextome.lismove_sdk.database.SessionSdkRepository
import net.nextome.lismove_sdk.database.SessionSdkRepositoryImpl
import net.nextome.lismove_sdk.location.LisMoveLocationManager
import net.nextome.lismove_sdk.sensorUpgrade.DeviceUpgradeViewModel
import net.nextome.lismove_sdk.sensorUpgrade.SensorUpgradeManager
import net.nextome.lismove_sdk.sensorUpgrade.SensorUpgradeRepo
import net.nextome.lismove_sdk.sensorUpgrade.SensorUpgradeRepoImpl
import net.nextome.lismove_sdk.sessionPoints.PointsManager
import net.nextome.lismove_sdk.sessionPoints.PointsManagerImpl
import net.nextome.lismove_sdk.sessionPoints.useCase.PointsManagerUseCase
import net.nextome.lismove_sdk.sessionPoints.useCase.PointsManagerUseCaseImpl

import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

class SdkKoinModule {
    companion object {
        fun getModule() = module {
            //Repository
            single { LismoveBleManager(get()) }
            single<SessionSdkRepository> { SessionSdkRepositoryImpl(get(), get(), get()) }
            single<SensorUpgradeRepo> { SensorUpgradeRepoImpl(get()) }
            single<DebugLogRepository> { DebugLogRepositoryImpl(get()) }
            single{ LisMoveLocationManager(get()) }
            single<PointsManager> { PointsManagerImpl(get()) }

            single<SensorUpgradeManager>{ SensorUpgradeManager(get(), get(), get())}
            single<PointsManagerUseCase>{ PointsManagerUseCaseImpl(get(), get(),get()) }
            viewModel{ DeviceUpgradeViewModel(get(), get(), get()) }
        }
    }
}