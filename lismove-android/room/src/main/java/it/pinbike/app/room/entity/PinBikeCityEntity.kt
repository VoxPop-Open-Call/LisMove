package it.lismove.app.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class LisMoveCityEntity (
    @ColumnInfo(name = "lismoveCityId")
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "lismoveCityName")
    val name: String,
    val province: String
){
    fun getFullName(): String{
        return "$name, $province"
    }
}