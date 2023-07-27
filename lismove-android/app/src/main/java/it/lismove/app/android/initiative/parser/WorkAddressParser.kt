package it.lismove.app.android.initiative.parser

import it.lismove.app.android.initiative.ui.data.WorkAddress
import it.lismove.app.room.entity.SeatEntity

fun WorkAddress.asSeatEntity(organization: Long): SeatEntity {

    return SeatEntity(
        id = id,
        address = address ?: "",
        number = number,
        cityExtended = cityExtended,
        city = city,
        name = name ?: "",
        organization = organization,
        cityName = cityExtended?.name,
        latitude = lat,
        longitude = lng
    )
}