package it.lismove.app.android.initiative.ui.parser

import it.lismove.app.android.initiative.ui.adapter.ExpandableAddress
import it.lismove.app.android.initiative.ui.data.WorkAddress
import it.lismove.app.room.entity.LisMoveUser

fun LisMoveUser.getHomeAddress(): WorkAddress = WorkAddress(address = homeAddress, number = homeNumber, city = homeCity,  cityExtended = homeCityExtended, showName = false, lat = homeLatitude, lng = homeLongitude)

fun LisMoveUser.asExpandableAddress() = ExpandableAddress(getHomeAddress(), showName = false, isInitiallyOpen = false, showMapsButton = true)