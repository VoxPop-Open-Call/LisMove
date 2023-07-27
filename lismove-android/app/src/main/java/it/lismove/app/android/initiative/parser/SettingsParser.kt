package it.lismove.app.android.initiative.parser

import it.lismove.app.android.initiative.apiService.data.SettingsResponse
import it.lismove.app.room.entity.SettingsEntity

fun List<SettingsResponse>.asSettingsDao(id: Long): SettingsEntity {

    val isActiveUrbanPoints= getValue("isActiveUrbanPoints")?.toBoolean() ?: false
    val isActiveTimeSlotBonus= getValue("isActiveTimeSlotBonus")?.toBoolean() ?: false
    val exclusiveCustomField= getValue("exclusiveCustomField")?.toBoolean() ?: false
    val ibanRequirement = getValue("ibanRequirement")?.toBoolean() ?: false
    val homeWorkRefund = getValue("isActiveHomeWorkRefunds")?.toBoolean() ?: false
    val initiativeRefund = getValue("isActiveUrbanPathRefunds")?.toBoolean() ?: false

    val multiplier = getValue("multiplier")?.toInt() ?: 1
    return SettingsEntity(
        organizationId = id,
        isActiveUrbanPoints = isActiveUrbanPoints,
        startDateUrbanPoints = getValue("startDateUrbanPoints"),
        endDateUrbanPoints = getValue("endDateUrbanPoints"),
        startDateBonus = getValue("startDateBonus"),
        endDateBonus = getValue("endDateBonus"),
        startTimeBonus = getValue("startTimeBonus"),
        endTimeBonus = getValue("endTimeBonus"),
        multiplier = multiplier,
        isActiveTimeSlotBonus = isActiveTimeSlotBonus,
        exclusiveCustomField = exclusiveCustomField,
        ibanRequirement = ibanRequirement,
        homeWorkRefund = homeWorkRefund,
        initiativeRefund = initiativeRefund
    )
}


private fun  List<SettingsResponse>.getValue(key: String): String?{
    val data = firstOrNull { it.organizationSetting == key }
    return data?.value
}