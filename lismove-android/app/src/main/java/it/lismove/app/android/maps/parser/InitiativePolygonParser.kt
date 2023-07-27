package it.lismove.app.android.maps.parser

import it.lismove.app.android.initiative.parser.asLatLngLists
import it.lismove.app.android.maps.data.InitiativePolygon
import it.lismove.app.room.entity.QueryData.EnrollmentWithOrganization

fun EnrollmentWithOrganization.asInitiativePolygon(): InitiativePolygon {
    return InitiativePolygon(
        id = organization.id.toString(),
        initiative = organization.title,
        polygon = organization.getGeoJsonCoordinates().asLatLngLists()
    )
}