package it.lismove.app.android.initiative.ui.parser

import it.lismove.app.android.initiative.ui.data.AddressItemUI
import it.lismove.app.room.entity.SeatEntity

fun SeatEntity.asAddressItemUI(): AddressItemUI {
    return AddressItemUI(id!!, name ?: "", getAddressString())
}