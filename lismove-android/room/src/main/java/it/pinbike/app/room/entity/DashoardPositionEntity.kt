package it.lismove.app.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.jetbrains.annotations.PropertyKey

@Entity
data class DashoardPositionEntity (
    @PrimaryKey val dashboardItemId: Int,
    val dashboardPosition: Int,
){
    companion object{
        val PROFILE = 0
        val MESSAGES = 1
        val EUROS = 2
        val PROJECTS = 3
        val KM_DONE = 4
        val CO2 = 5
        val POINTS = 6
        val USAGE = 7
        val SENSOR = 8

    }
}