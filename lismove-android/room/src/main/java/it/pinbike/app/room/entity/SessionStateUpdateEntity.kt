package it.lismove.app.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity


@Entity
data class SessionStateUpdateEntity(
    @ColumnInfo(name = "id")
    var id: String = "",
    @ColumnInfo(name = "status")
    val status: Int = 0,
    @ColumnInfo(name = "proposedStatus")
    val proposedStatus: Int? = null,
)