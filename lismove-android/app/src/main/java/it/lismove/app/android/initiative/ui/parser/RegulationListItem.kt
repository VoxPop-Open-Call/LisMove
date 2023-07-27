package it.lismove.app.android.initiative.ui.parser

import it.lismove.app.android.initiative.ui.data.RegulationListItem
import it.lismove.app.room.entity.OrganizationEntity

fun OrganizationEntity.asRegulationListItem(): RegulationListItem{
    return RegulationListItem(title, getSanitizedRegulation() ?: "Nessun regolamento attivo")
}