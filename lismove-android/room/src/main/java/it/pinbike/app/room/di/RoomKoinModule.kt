package it.lismove.app.room.di

import it.lismove.app.room.LisMoveDatabase
import org.koin.dsl.module

class RoomKoinModule {
    companion object {

        fun getModule() = module {

            single { LisMoveDatabase.getInstance(get()) }
            single { get<LisMoveDatabase>().getSessionDataDao() }
            single { get<LisMoveDatabase>().getPartialSessionDataDao() }
            single { get<LisMoveDatabase>().getLisMoveUserDao() }
            single { get<LisMoveDatabase>().getSensorDao() }
            single { get<LisMoveDatabase>().getOrganizationDao() }
            single { get<LisMoveDatabase>().getEnrollmentDao() }
            single { get<LisMoveDatabase>().getDashboardDao() }
            single { get<LisMoveDatabase>().getOrganizationSessionPointsDao() }
            single { get<LisMoveDatabase>().getDashboardPositionDao() }
            single { get<LisMoveDatabase>().getSettingsDao() }
            single { get<LisMoveDatabase>().getDebugLogDao() }

        }
    }
}