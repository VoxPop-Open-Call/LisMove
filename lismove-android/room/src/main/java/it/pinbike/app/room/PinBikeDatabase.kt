package it.lismove.app.room

import android.content.Context
import androidx.room.*
import it.lismove.app.room.dao.*
import it.lismove.app.room.entity.*

@Database(
    entities = [PartialSessionDataEntity::class, SessionDataEntity::class,
        LisMoveUserEntity::class, LisMoveCityEntity::class,
                SensorEntity::class, OrganizationEntity::class, SeatEntity::class,
               EnrollmentEntity::class, OrganizationSessionPointEntity::class, DashoardPositionEntity::class,
               DashboardEntity::class, DashboardUserDistanceStatsEntity::class, SettingsEntity::class,
               DebugLogEntity::class],
    version = 73,
    exportSchema = false)
abstract class LisMoveDatabase : RoomDatabase() {

    abstract fun getPartialSessionDataDao(): PartialSessionDataDao
    abstract fun getSessionDataDao(): SessionDataDao
    abstract fun getLisMoveUserDao(): LisMoveUserEntityDao
    abstract fun getSensorDao(): SensorDao
    abstract fun getOrganizationDao(): OrganizationDao
    abstract fun getEnrollmentDao(): EnrollmentDao
    abstract fun getDashboardDao(): DashboardDao
    abstract fun getOrganizationSessionPointsDao(): OrganizationSessionPointDao
    abstract fun getDashboardPositionDao(): DashboardPositionDao
    abstract fun getSettingsDao(): SettingsDao
    abstract fun getDebugLogDao(): DebugLogDao

    companion object {

        @Volatile
        private var instance: LisMoveDatabase? = null

        fun getInstance(context: Context): LisMoveDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context) = Room
            .databaseBuilder(context, LisMoveDatabase::class.java, "lismove_db")
            .fallbackToDestructiveMigration()
            .build()
    }
}