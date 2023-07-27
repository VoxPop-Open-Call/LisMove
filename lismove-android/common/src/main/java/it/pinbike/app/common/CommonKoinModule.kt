package it.lismove.app.common

import it.lismove.app.common.background_sensor_detection.SensorDetectionRepository
import it.lismove.app.common.background_sensor_detection.SensorDetectionRepositoryImpl
import org.koin.dsl.module

class CommonKoinModule {
    companion object {

        fun getModule() = module {
            single<SensorDetectionRepository> { SensorDetectionRepositoryImpl(get()) }
        }
    }
}