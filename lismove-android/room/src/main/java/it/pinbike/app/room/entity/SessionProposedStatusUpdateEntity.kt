package it.lismove.app.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity


@Entity
data class SessionProposedStatusUpdateEntity(
    @ColumnInfo(name = "id")
    var id: String = "",
    @ColumnInfo(name = "proposedStatus")
    val proposedStatus: Int? = 0,
)