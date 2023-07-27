package it.lismove.app.room.entity

import androidx.room.Entity

@Entity(primaryKeys = ["sessionId", "organizationId"])
data class OrganizationSessionPointEntity (
    var distance: Double= 0.0,
    val multiplier: Double,
    val organizationId: Long,
    var points: Int = 0,
    var multiplierDistance: Double = 0.0,
    var multiplierPoints: Int = 0,
    val euro: Double = 0.0,
    var refundStatus: Int? = null,
    val sessionId: String  // Server generated
){

}
