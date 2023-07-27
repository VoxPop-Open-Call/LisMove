package it.lismove.app.room.entity.QueryData

import androidx.room.Embedded
import androidx.room.Relation
import it.lismove.app.room.entity.SessionDataEntity
import it.lismove.app.room.entity.OrganizationSessionPointEntity

data class SessionWithPoints (
    @Embedded
    val session: SessionDataEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "sessionId"
    )
    var pointOrganizations: List<OrganizationSessionPointEntity>
)