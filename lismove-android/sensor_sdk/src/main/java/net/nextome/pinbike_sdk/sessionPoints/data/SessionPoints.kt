package net.nextome.lismove_sdk.sessionPoints.data

import it.lismove.app.room.entity.OrganizationSessionPointEntity

data class SessionPoints(
    var nationalKm: Double,
    var nationalPoints: Int,
    val initiativePointOrganizations: ArrayList<OrganizationSessionPointEntity>
)