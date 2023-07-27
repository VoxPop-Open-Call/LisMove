package it.lismove.app.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import it.lismove.app.room.entity.LisMoveUserEntity
import kotlinx.coroutines.flow.Flow
@Dao
interface LisMoveUserEntityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addOrUpdate(entity: LisMoveUserEntity)

    @Query("SELECT * FROM lismoveuserentity WHERE uid = :uid")
    suspend fun getUser(uid: String): LisMoveUserEntity

    @Query("SELECT * FROM lismoveuserentity WHERE uid = :uid")
    fun getUserObservable(uid: String): Flow<LisMoveUserEntity>

    @Query("DELETE FROM lismoveuserentity WHERE uid = :uid")
    fun deleteUser(uid: String)
}