package it.lismove.app.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class SensorEntity (
        @PrimaryKey var userId: String,
        var uuid: String,
        var name: String,
        var wheelDiameter: Int,
        var bikeType: String? = null,
        var endAssociation: Long? = null,
        var firmware: String? = null,
        var history: Long? = null,
        var startAssociation: Long? = null,
        var stolen: Boolean = false ,
        var hubCoefficient: Double = 1.0
){
        companion object{
                val SENSOR_ENTITY_NORMAL = "Tradizionale (muscolare)"
                val SENSOR_ENTITY_ELECTRIC = "Elettrica (assistita)"

                val SENSOR_ENTINTY_DEFAUTL_WHEEL_LABEL = "28\""
                val SENSOR_ENTINTY_DEFAUTL_WHEEL_DIAMETER = 700
        }
}

