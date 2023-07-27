package it.lismove.app.android.session.parser

import it.lismove.app.android.session.data.SessionPoint
import it.lismove.app.room.entity.OrganizationSessionPointEntity

fun OrganizationSessionPointEntity.asSessionPoint(): SessionPoint{
    return SessionPoint(
        distance = distance,
        multiplier = multiplier,
        organizationId = organizationId,
        points = points,
        sessionId = null,
        euro = euro,
        refundStatus = refundStatus
    )
}

fun SessionPoint.asOrganizationSessionPointEntity(): OrganizationSessionPointEntity{
    return OrganizationSessionPointEntity(
        distance = distance,
        multiplier = multiplier,
        organizationId = organizationId,
        points = points,
        sessionId = sessionId ?: "",
        euro = euro ?: 0.0,
        refundStatus = refundStatus
    )
}